package com.example.doomscroller.domain

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.doomscroller.MainActivity
import com.example.doomscroller.data.AppCatalog
import com.example.doomscroller.data.AppRule
import com.example.doomscroller.data.PenaltyInfo
import com.example.doomscroller.data.PreferencesManager
import com.example.doomscroller.data.ReminderSession
import com.example.doomscroller.platform.NotificationCoordinator
import com.example.doomscroller.platform.PermissionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

sealed class EnforcementEvent {
    data class WarningTriggered(val packageName: String, val appName: String, val warningCount: Int, val isPenalty: Boolean) : EnforcementEvent()
    data class PenaltyExpired(val packageName: String) : EnforcementEvent()
}

class EnforcementManager private constructor(private val context: Context) {
    companion object {
        private const val TAG = "EnforcementManager"

        @Volatile
        private var INSTANCE: EnforcementManager? = null

        fun getInstance(context: Context): EnforcementManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EnforcementManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs = PreferencesManager(context)
    private val ruleEvaluator = RuleEvaluator()
    private val reminderEscalationEvaluator = ReminderEscalationEvaluator()
    private val notificationCoordinator = NotificationCoordinator(context)
    private val permissionManager = PermissionManager(context)
    private val appCatalog = AppCatalog(context)
    val rateLimiter = NotificationRateLimiter(prefs)

    private val _activePenalties = MutableStateFlow<Map<String, PenaltyInfo>>(emptyMap())
    val activePenalties: StateFlow<Map<String, PenaltyInfo>> = _activePenalties.asStateFlow()

    private val _activePenalty = MutableStateFlow<PenaltyInfo?>(null)
    val activePenalty: StateFlow<PenaltyInfo?> = _activePenalty.asStateFlow()

    private val _enforcementEvents = MutableSharedFlow<EnforcementEvent>(extraBufferCapacity = 64)
    val enforcementEvents: SharedFlow<EnforcementEvent> = _enforcementEvents.asSharedFlow()

    init {
        // Restore all active penalties on startup/process restart
        val savedPenalties = prefs.getAllActivePenalties()
        val validPenalties = mutableMapOf<String, PenaltyInfo>()
        for ((pkg, penalty) in savedPenalties) {
            if (penalty.isExpired) {
                Log.i(TAG, "Restored penalty for $pkg has already expired. Clearing.")
                prefs.clearActivePenalty(pkg)
            } else {
                Log.i(TAG, "Restored active penalty for $pkg: ${penalty.remainingMillis / 1000}s remaining.")
                validPenalties[pkg] = penalty
            }
        }
        _activePenalties.value = validPenalties
        _activePenalty.value = validPenalties.values.firstOrNull()
    }

    fun getActivePenalty(packageName: String): PenaltyInfo? {
        return _activePenalties.value[packageName]
    }

    @Synchronized
    fun evaluateRules(
        usageMap: Map<String, Long>,
        rules: List<AppRule>,
        foregroundPackage: String? = null,
        currentDate: LocalDate = LocalDate.now()
    ): List<EvaluationResult> {
        val now = System.currentTimeMillis()
        val results = mutableListOf<EvaluationResult>()
        val monitoredRules = rules.filter { it.isEnabled }
        val monitoredPackageSet = monitoredRules.map { it.packageName }.toSet()

        // 1. Check existing active penalties expiration and unmonitored cleanup
        val currentPenalties = _activePenalties.value.toMutableMap()
        val expiredPackages = mutableListOf<String>()

        for ((pkg, penalty) in currentPenalties) {
            // If the package is no longer monitored, cancel its penalty and notification immediately
            if (pkg !in monitoredPackageSet) {
                expiredPackages.add(pkg)
                prefs.clearActivePenalty(pkg)
                prefs.clearReminderSession(pkg)
                notificationCoordinator.cancelWarningNotification(pkg)
                rateLimiter.resetAll(pkg)
                continue
            }

            if (penalty.isExpired) {
                expiredPackages.add(pkg)
                prefs.clearActivePenalty(pkg)
                notificationCoordinator.cancelWarningNotification(pkg)
                rateLimiter.onConditionEnded(pkg)
                _enforcementEvents.tryEmit(EnforcementEvent.PenaltyExpired(pkg))
                Log.i(TAG, "Active penalty for $pkg expired at $now. Cleared active reminder state.")
            } else if (foregroundPackage == pkg) {
                // User is actively in this penalized package; check rate limiter before repeating notification!
                val appName = getAppName(pkg)
                val usedMinutes = ((usageMap[pkg] ?: 0L) / 60000L).toInt()
                val rule = monitoredRules.find { it.packageName == pkg }
                val limitMinutes = rule?.limitMinutes ?: 5
                val remainingSeconds = (penalty.remainingMillis / 1000).toInt()

                val rateLimit = rateLimiter.evaluateNotification(
                    packageName = pkg,
                    now = now,
                    isNewEpisode = false,
                    recordIfAllowed = true
                )
                if (rateLimit.allowed) {
                    notificationCoordinator.showWarningNotification(
                        packageName = pkg,
                        appName = appName,
                        usedMinutes = usedMinutes,
                        limitMinutes = limitMinutes,
                        warningCount = penalty.warningCount,
                        isPenaltyActive = true,
                        remainingSeconds = remainingSeconds,
                        ignoredRemindersCount = penalty.warningCount
                    )
                } else {
                    Log.d(TAG, "Polling check: repeat notification for $pkg suppressed (${rateLimit.reason})")
                }
            }
        }

        for (pkg in expiredPackages) {
            currentPenalties.remove(pkg)
        }
        _activePenalties.value = currentPenalties
        _activePenalty.value = currentPenalties.values.firstOrNull()

        // 2. Clean up reminder sessions for unmonitored packages
        val allSessions = prefs.getAllReminderSessions()
        for ((pkg, _) in allSessions) {
            if (pkg !in monitoredPackageSet) {
                prefs.clearReminderSession(pkg)
                notificationCoordinator.cancelWarningNotification(pkg)
            }
        }

        // 3. Update background / paused states for monitored packages not currently in foreground
        for (rule in monitoredRules) {
            if (rule.packageName != foregroundPackage) {
                val session = prefs.getReminderSession(rule.packageName)
                if (session.reminderCount > 0 || session.qualifyingUsageMillis > 0L) {
                    val usedMillis = usageMap[rule.packageName] ?: 0L
                    val (pausedSession, _) = reminderEscalationEvaluator.evaluate(
                        session = session,
                        isForeground = false,
                        currentUsageMillis = usedMillis,
                        intervalMinutes = rule.limitMinutes,
                        ignoredLimit = prefs.getIgnoredReminderLimit(),
                        now = now
                    )
                    prefs.saveReminderSession(pausedSession)
                }
            }
        }

        // 4. Safe check: If no apps are monitored, or foregroundPackage is null,
        // or foregroundPackage is NOT in the monitored list:
        // Do NOT trigger any reminder evaluations or penalties!
        if (monitoredPackageSet.isEmpty() || foregroundPackage == null || foregroundPackage !in monitoredPackageSet) {
            return results
        }

        // 5. User is actively in an explicitly monitored application (foregroundPackage).
        // If an active penalty is already in effect for this package, do not send reminder notifications.
        if (_activePenalties.value.containsKey(foregroundPackage)) {
            return results
        }

        val activeRule = monitoredRules.find { it.packageName == foregroundPackage } ?: return results
        val usedMillis = usageMap[activeRule.packageName] ?: 0L
        val session = prefs.getReminderSession(foregroundPackage)
        val intervalMinutes = activeRule.limitMinutes
        val ignoredLimit = prefs.getIgnoredReminderLimit()

        val (updatedSession, decision) = reminderEscalationEvaluator.evaluate(
            session = session,
            isForeground = true,
            currentUsageMillis = usedMillis,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = now
        )
        prefs.saveReminderSession(updatedSession)
        val appName = getAppName(foregroundPackage)

        when (decision) {
            is EscalationDecision.SendReminder -> {
                Log.i(TAG, "Reminder #${decision.reminderNumber} triggered for $foregroundPackage (ignored=${decision.ignoredCount}/$ignoredLimit, interval=${intervalMinutes}m)")
                val rateLimit = rateLimiter.evaluateNotification(
                    packageName = foregroundPackage,
                    now = now,
                    isNewEpisode = (decision.reminderNumber == 1),
                    recordIfAllowed = true
                )
                if (rateLimit.allowed) {
                    notificationCoordinator.showReminderNotification(
                        packageName = foregroundPackage,
                        appName = appName,
                        reminderNumber = decision.reminderNumber,
                        ignoredCount = decision.ignoredCount,
                        limitCount = decision.limitCount,
                        intervalMinutes = decision.intervalMinutes
                    )
                } else {
                    Log.w(TAG, "Reminder notification suppressed for $foregroundPackage: ${rateLimit.reason}")
                }
            }
            is EscalationDecision.TriggerEnforcement -> {
                Log.w(TAG, "ESCALATION ENFORCEMENT triggered for $foregroundPackage: ${decision.ignoredCount} reminders ignored. Cooldown ends at ${decision.penaltyEndTimeMillis}")
                notificationCoordinator.cancelWarningNotification(foregroundPackage)

                val penalty = PenaltyInfo(
                    packageName = foregroundPackage,
                    startTimeMillis = now,
                    endTimeMillis = decision.penaltyEndTimeMillis,
                    warningCount = decision.ignoredCount,
                    isAcknowledged = false
                )
                prefs.saveActivePenalty(penalty)
                val updated = _activePenalties.value.toMutableMap()
                updated[penalty.packageName] = penalty
                _activePenalties.value = updated
                _activePenalty.value = penalty

                rateLimiter.evaluateNotification(
                    packageName = foregroundPackage,
                    now = now,
                    isNewEpisode = true,
                    recordIfAllowed = true
                )

                notificationCoordinator.showWarningNotification(
                    packageName = foregroundPackage,
                    appName = appName,
                    usedMinutes = (usedMillis / 60000L).toInt(),
                    limitMinutes = intervalMinutes,
                    warningCount = decision.ignoredCount,
                    isPenaltyActive = true,
                    remainingSeconds = ((decision.penaltyEndTimeMillis - now) / 1000).toInt(),
                    ignoredRemindersCount = decision.ignoredCount
                )

                _enforcementEvents.tryEmit(
                    EnforcementEvent.WarningTriggered(
                        packageName = foregroundPackage,
                        appName = appName,
                        warningCount = decision.ignoredCount,
                        isPenalty = true
                    )
                )

                if (permissionManager.hasOverlayPermission()) {
                    try {
                        val warningIntent = Intent(context, MainActivity::class.java).apply {
                            action = "com.example.doomscroller.ACTION_INTERRUPTION"
                            putExtra("destination", "interruption")
                            putExtra("packageName", foregroundPackage)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(warningIntent)
                        Log.i(TAG, "Overlay permission granted: launched warning screen directly over $foregroundPackage")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to launch warning screen directly with overlay permission", e)
                    }
                }
            }
            is EscalationDecision.NoAction -> {
                // Usage has not reached next reminder threshold
            }
        }

        return results
    }

    fun acknowledgeReminder(packageName: String) {
        val session = prefs.getReminderSession(packageName)
        val updated = reminderEscalationEvaluator.acknowledge(session)
        prefs.saveReminderSession(updated)
        notificationCoordinator.cancelWarningNotification(packageName)
        rateLimiter.onConditionEnded(packageName)
        Log.i(TAG, "User explicitly acknowledged reminder for $packageName. Ignored count reset.")
    }

    fun acknowledgePenalty(packageName: String) {
        val current = _activePenalties.value[packageName] ?: _activePenalty.value
        if (current != null && (current.packageName == packageName || packageName.isEmpty())) {
            val targetPkg = current.packageName
            val updated = current.copy(isAcknowledged = true)
            prefs.saveActivePenalty(updated)
            val updatedMap = _activePenalties.value.toMutableMap()
            updatedMap[targetPkg] = updated
            _activePenalties.value = updatedMap
            _activePenalty.value = updatedMap.values.firstOrNull()
            rateLimiter.onConditionEnded(targetPkg)
            Log.i(TAG, "User explicitly acknowledged warning for $targetPkg")
        }
    }

    fun expirePenaltyImmediately(packageName: String) {
        val current = _activePenalties.value[packageName] ?: _activePenalty.value
        if (current != null && (current.packageName == packageName || packageName.isEmpty())) {
            val targetPkg = current.packageName
            prefs.clearActivePenalty(targetPkg)
            prefs.clearReminderSession(targetPkg)
            val updatedMap = _activePenalties.value.toMutableMap()
            updatedMap.remove(targetPkg)
            _activePenalties.value = updatedMap
            _activePenalty.value = updatedMap.values.firstOrNull()
            notificationCoordinator.cancelWarningNotification(targetPkg)
            rateLimiter.onConditionEnded(targetPkg)
            _enforcementEvents.tryEmit(EnforcementEvent.PenaltyExpired(targetPkg))
            Log.i(TAG, "Manually expired penalty and cleared reminder session for $targetPkg")
        }
    }

    fun getAppName(packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }
    }
}
