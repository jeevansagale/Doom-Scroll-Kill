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
import com.example.doomscroller.domain.learning.QuestionBank
import com.example.doomscroller.domain.quotes.MotivationalQuoteEngine
import com.example.doomscroller.data.PendingIntervention
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

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

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val timeoutJobs = ConcurrentHashMap<String, Job>()

    private val _activePenalties = MutableStateFlow<Map<String, PenaltyInfo>>(emptyMap())
    val activePenalties: StateFlow<Map<String, PenaltyInfo>> = _activePenalties.asStateFlow()

    private val _activePenalty = MutableStateFlow<PenaltyInfo?>(null)
    val activePenalty: StateFlow<PenaltyInfo?> = _activePenalty.asStateFlow()

    private val _pendingInterventions = MutableStateFlow<Map<String, PendingIntervention>>(emptyMap())
    val pendingInterventions: StateFlow<Map<String, PendingIntervention>> = _pendingInterventions.asStateFlow()

    private val _enforcementEvents = MutableSharedFlow<EnforcementEvent>(extraBufferCapacity = 64)
    val enforcementEvents: SharedFlow<EnforcementEvent> = _enforcementEvents.asSharedFlow()

    init {
        // 1. Restore all active penalties on startup/process restart
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

        // 2. Restore pending interventions across process restarts and re-evaluate deadline
        val savedPending = prefs.getAllPendingInterventions()
        val now = System.currentTimeMillis()
        val activePendingMap = mutableMapOf<String, PendingIntervention>()
        for ((pkg, pending) in savedPending) {
            if (pending.isAcknowledged) {
                prefs.clearPendingIntervention(pkg)
            } else if (pending.isTimedOut || now >= pending.deadlineMillis) {
                Log.i(TAG, "Restored pending intervention for $pkg is overdue (${(now - pending.deadlineMillis) / 1000}s past deadline). Triggering NO_ACK.")
                triggerNoAckTimeout(pkg, pending.eventId, now)
            } else {
                Log.i(TAG, "Restored pending intervention for $pkg with ${(pending.deadlineMillis - now) / 1000}s remaining before NO_ACK.")
                activePendingMap[pkg] = pending
                scheduleTimeoutJob(pkg, pending.eventId, pending.deadlineMillis - now)
            }
        }
        _pendingInterventions.value = activePendingMap
    }

    fun hasActivePenaltiesOrInterventions(): Boolean {
        return _activePenalties.value.isNotEmpty() || _pendingInterventions.value.isNotEmpty()
    }

    fun getActivePenalty(packageName: String): PenaltyInfo? {
        return _activePenalties.value[packageName]
    }

    @Synchronized
    fun isPackageLaunchable(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return false
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            intent != null
        } catch (e: Exception) {
            false
        }
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

        // 0. Check existing pending interventions expiration and unmonitored cleanup
        val pendingMap = prefs.getAllPendingInterventions()
        for ((pkg, pending) in pendingMap) {
            if (pkg !in monitoredPackageSet) {
                prefs.clearPendingIntervention(pkg)
                timeoutJobs.remove(pkg)?.cancel()
                val updated = _pendingInterventions.value.toMutableMap()
                updated.remove(pkg)
                _pendingInterventions.value = updated
            } else if (now >= pending.deadlineMillis) {
                Log.w(TAG, "evaluateRules: pending intervention for $pkg reached deadline (${(now - pending.deadlineMillis) / 1000}s overdue). Triggering NO_ACK.")
                triggerNoAckTimeout(pkg, pending.eventId, now)
            }
        }

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

            val session = prefs.getReminderSession(pkg)
            val isCooldownElapsed = now >= penalty.endTimeMillis
            val isLearningCompleted = session.isLearningGateCompleted

            if (isCooldownElapsed && isLearningCompleted) {
                // Both conditions met: release penalty
                expiredPackages.add(pkg)
                prefs.clearActivePenalty(pkg)
                notificationCoordinator.cancelWarningNotification(pkg)
                rateLimiter.onConditionEnded(pkg)

                // Recover reminder session to MONITORING state with new baseline
                val usedMillis = usageMap[pkg] ?: 0L
                val (resetSession, _) = reminderEscalationEvaluator.evaluate(
                    session = session,
                    isForeground = (pkg == foregroundPackage),
                    currentUsageMillis = usedMillis,
                    intervalMinutes = monitoredRules.find { it.packageName == pkg }?.limitMinutes ?: 5,
                    ignoredLimit = prefs.getIgnoredReminderLimit(),
                    now = now
                )
                prefs.saveReminderSession(resetSession)

                _enforcementEvents.tryEmit(EnforcementEvent.PenaltyExpired(pkg))
                Log.i(TAG, "Both conditions met for $pkg: cooldown elapsed and learning gate completed. Cleared active penalty and resumed monitoring.")
            } else if (foregroundPackage == pkg) {
                // Restriction remains active (cooldown counting down or learning gate pending)
                // If overlay permission granted, launch/re-launch Learning Gate directly over restricted app
                if (permissionManager.hasOverlayPermission()) {
                    try {
                        val gateIntent = Intent(context, MainActivity::class.java).apply {
                            action = "com.example.doomscroller.ACTION_LEARNING_GATE"
                            putExtra("destination", "learning_gate")
                            putExtra("packageName", pkg)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(gateIntent)
                        Log.i(TAG, "Re-launched Learning Gate directly over restricted app $pkg")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to re-launch Learning Gate overlay for $pkg", e)
                    }
                }

                // User is actively in this penalized package; check rate limiter before repeating notification!
                val appName = getAppName(pkg)
                val usedMinutes = ((usageMap[pkg] ?: 0L) / 60000L).toInt()
                val rule = monitoredRules.find { it.packageName == pkg }
                val limitMinutes = rule?.limitMinutes ?: 5
                val remainingSeconds = ((penalty.endTimeMillis - now).coerceAtLeast(0L) / 1000).toInt()

                val rateLimit = rateLimiter.evaluateNotification(
                    packageName = pkg,
                    now = now,
                    isNewEpisode = false,
                    recordIfAllowed = true
                )
                if (rateLimit.allowed) {
                    val destPkg = prefs.getDestinationPackage()
                    val isDestValid = isPackageLaunchable(destPkg)
                    val destAppName = if (destPkg != null && isDestValid) getAppName(destPkg) else null

                    notificationCoordinator.showWarningNotification(
                        packageName = pkg,
                        appName = appName,
                        usedMinutes = usedMinutes,
                        limitMinutes = limitMinutes,
                        warningCount = penalty.warningCount,
                        isPenaltyActive = true,
                        remainingSeconds = remainingSeconds,
                        ignoredRemindersCount = penalty.warningCount,
                        destinationPackage = if (isDestValid) destPkg else null,
                        destinationAppName = destAppName
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

        val restrictionDuration = prefs.getRestrictionDurationMinutes()
        val pendingIntervention = prefs.getPendingIntervention(foregroundPackage)

        val (updatedSession, decision) = reminderEscalationEvaluator.evaluate(
            session = session,
            isForeground = true,
            currentUsageMillis = usedMillis,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = now,
            restrictionDurationMinutes = restrictionDuration,
            pendingIntervention = pendingIntervention
        )

        val appName = getAppName(foregroundPackage)

        when (decision) {
            is EscalationDecision.SendReminder -> {
                Log.i(TAG, "Reminder #${decision.reminderNumber} triggered for $foregroundPackage (ignored=${decision.ignoredCount}/$ignoredLimit, interval=${intervalMinutes}m)")

                // 1. If an acknowledgment deadline is specified, persist PendingIntervention and schedule in-process timeout
                if (decision.eventId != null && decision.deadlineMillis != null) {
                    val pending = PendingIntervention(
                        eventId = decision.eventId,
                        packageName = foregroundPackage,
                        startTimeMillis = now,
                        deadlineMillis = decision.deadlineMillis,
                        isAcknowledged = false,
                        isTimedOut = false
                    )
                    prefs.savePendingIntervention(pending)
                    val updatedMap = _pendingInterventions.value.toMutableMap()
                    updatedMap[foregroundPackage] = pending
                    _pendingInterventions.value = updatedMap

                    scheduleTimeoutJob(foregroundPackage, decision.eventId, decision.deadlineMillis - now)
                    Log.i(TAG, "Started 60s acknowledgment deadline for $foregroundPackage (eventId=${decision.eventId}, deadline=${decision.deadlineMillis})")
                }

                val rateLimit = rateLimiter.evaluateNotification(
                    packageName = foregroundPackage,
                    now = now,
                    isNewEpisode = (decision.reminderNumber == 1),
                    recordIfAllowed = true
                )
                if (rateLimit.allowed) {
                    val delivered = notificationCoordinator.showReminderNotification(
                        packageName = foregroundPackage,
                        appName = appName,
                        reminderNumber = decision.reminderNumber,
                        ignoredCount = decision.ignoredCount,
                        limitCount = decision.limitCount,
                        intervalMinutes = decision.intervalMinutes,
                        eventId = decision.eventId
                    )
                    if (delivered) {
                        val sentSession = reminderEscalationEvaluator.markReminderSent(updatedSession)
                        prefs.saveReminderSession(sentSession)
                    } else {
                        prefs.saveReminderSession(updatedSession)
                    }
                } else {
                    Log.w(TAG, "Reminder notification suppressed for $foregroundPackage: ${rateLimit.reason}")
                    prefs.saveReminderSession(updatedSession)
                }
            }
            is EscalationDecision.TriggerEnforcement -> {
                Log.w(TAG, "ESCALATION ENFORCEMENT triggered for $foregroundPackage: ${decision.ignoredCount} reminders ignored (reason=${decision.reason}). Cooldown ends at ${decision.penaltyEndTimeMillis}")
                val cooldownSession = reminderEscalationEvaluator.markEnforced(updatedSession)
                prefs.saveReminderSession(cooldownSession)

                executeEnforcementAction(
                    packageName = foregroundPackage,
                    endTimeMillis = decision.penaltyEndTimeMillis,
                    warningCount = decision.ignoredCount,
                    now = now
                )
            }
            is EscalationDecision.PenaltyExpired -> {
                Log.i(TAG, "Evaluator detected penalty expired for $foregroundPackage. Resuming monitoring.")
                prefs.clearActivePenalty(foregroundPackage)
                val updated = _activePenalties.value.toMutableMap()
                updated.remove(foregroundPackage)
                _activePenalties.value = updated
                _activePenalty.value = updated.values.firstOrNull()
                notificationCoordinator.cancelWarningNotification(foregroundPackage)
                rateLimiter.onConditionEnded(foregroundPackage)
                prefs.saveReminderSession(updatedSession)
                _enforcementEvents.tryEmit(EnforcementEvent.PenaltyExpired(foregroundPackage))
            }
            is EscalationDecision.NoAction -> {
                prefs.saveReminderSession(updatedSession)
            }
        }

        return results
    }

    private fun scheduleTimeoutJob(packageName: String, eventId: String, delayMillis: Long) {
        timeoutJobs[packageName]?.cancel()
        val job = managerScope.launch {
            if (delayMillis > 0) {
                delay(delayMillis)
            }
            triggerNoAckTimeout(packageName, eventId, System.currentTimeMillis())
        }
        timeoutJobs[packageName] = job
    }

    @Synchronized
    fun acknowledgeIntervention(
        packageName: String,
        eventId: String? = null,
        now: Long = System.currentTimeMillis()
    ): Boolean {
        val pending = prefs.getPendingIntervention(packageName)
        if (pending == null) {
            // Fallback for direct reminder session acknowledge
            val session = prefs.getReminderSession(packageName)
            val updated = reminderEscalationEvaluator.acknowledge(session)
            prefs.saveReminderSession(updated)
            notificationCoordinator.cancelWarningNotification(packageName)
            rateLimiter.onConditionEnded(packageName)
            Log.i(TAG, "Direct reminder acknowledge for $packageName without pending intervention record.")
            return true
        }

        if (eventId != null && pending.eventId != eventId) {
            Log.w(TAG, "Stale acknowledge attempt for $packageName: eventId mismatch ($eventId vs ${pending.eventId})")
            return false
        }

        if (pending.isAcknowledged || pending.isTimedOut) {
            Log.i(TAG, "Intervention already resolved for $packageName (ack=${pending.isAcknowledged}, timedOut=${pending.isTimedOut})")
            return false
        }

        // Cancel in-memory timeout job
        timeoutJobs.remove(packageName)?.cancel()

        // Atomically mark acknowledged before clearing to prevent any concurrent timeout winning
        val updatedPending = pending.copy(isAcknowledged = true)
        prefs.savePendingIntervention(updatedPending)
        prefs.clearPendingIntervention(packageName)

        val currentPendingMap = _pendingInterventions.value.toMutableMap()
        currentPendingMap.remove(packageName)
        _pendingInterventions.value = currentPendingMap

        // Update reminder session
        val session = prefs.getReminderSession(packageName)
        val updatedSession = reminderEscalationEvaluator.acknowledge(session)
        prefs.saveReminderSession(updatedSession)

        notificationCoordinator.cancelWarningNotification(packageName)
        rateLimiter.onConditionEnded(packageName)
        Log.i(TAG, "User successfully acknowledged intervention for $packageName (eventId=${pending.eventId})")
        return true
    }

    @Synchronized
    fun triggerNoAckTimeout(
        packageName: String,
        eventId: String? = null,
        now: Long = System.currentTimeMillis()
    ): Boolean {
        val pending = prefs.getPendingIntervention(packageName) ?: return false

        if (eventId != null && pending.eventId != eventId) {
            Log.w(TAG, "Stale timeout trigger for $packageName: eventId mismatch ($eventId vs ${pending.eventId})")
            return false
        }

        if (pending.isAcknowledged || pending.isTimedOut) {
            Log.i(TAG, "Intervention already resolved for $packageName (ack=${pending.isAcknowledged}, timedOut=${pending.isTimedOut})")
            return false
        }

        // Cancel in-memory timeout job
        timeoutJobs.remove(packageName)?.cancel()

        // Atomically mark timed out before clearing
        val updatedPending = pending.copy(isTimedOut = true)
        prefs.savePendingIntervention(updatedPending)
        prefs.clearPendingIntervention(packageName)

        val currentPendingMap = _pendingInterventions.value.toMutableMap()
        currentPendingMap.remove(packageName)
        _pendingInterventions.value = currentPendingMap

        val durationMinutes = prefs.getRestrictionDurationMinutes()
        val session = prefs.getReminderSession(packageName)
        val (timedOutSession, decision) = reminderEscalationEvaluator.transitionToNoAck(session, pending.eventId, now, durationMinutes)
        prefs.saveReminderSession(timedOutSession)

        Log.w(TAG, "NO_ACK timeout triggered for $packageName (eventId=${pending.eventId}). Activating restriction for ${durationMinutes}m.")
        executeEnforcementAction(
            packageName = packageName,
            endTimeMillis = decision.penaltyEndTimeMillis,
            warningCount = decision.ignoredCount,
            now = now
        )
        return true
    }

    private fun executeEnforcementAction(
        packageName: String,
        endTimeMillis: Long,
        warningCount: Int,
        now: Long
    ) {
        notificationCoordinator.cancelWarningNotification(packageName)

        val penalty = PenaltyInfo(
            packageName = packageName,
            startTimeMillis = now,
            endTimeMillis = endTimeMillis,
            warningCount = warningCount,
            isAcknowledged = false
        )
        prefs.saveActivePenalty(penalty)
        val updated = _activePenalties.value.toMutableMap()
        updated[penalty.packageName] = penalty
        _activePenalties.value = updated
        _activePenalty.value = penalty

        rateLimiter.evaluateNotification(
            packageName = packageName,
            now = now,
            isNewEpisode = true,
            recordIfAllowed = true
        )

        val destPkg = prefs.getDestinationPackage()
        val isDestValid = isPackageLaunchable(destPkg)
        val destAppName = if (destPkg != null && isDestValid) getAppName(destPkg) else null
        val appName = getAppName(packageName)

        Log.i(TAG, "Executing enforcement action: destPkg=$destPkg, valid=$isDestValid, destAppName=$destAppName")

        // 1. Direct launch attempt if destination app configured, valid, and permitted
        if (destPkg != null && isDestValid && destPkg != context.packageName) {
            if (permissionManager.hasOverlayPermission()) {
                try {
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(destPkg)?.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    if (launchIntent != null) {
                        context.startActivity(launchIntent)
                        Log.i(TAG, "Direct launch SUCCESS: Dispatched launch intent for destination app $destPkg ($destAppName)")
                    } else {
                        Log.w(TAG, "Direct launch FAILED: No launch intent found for $destPkg")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Direct launch EXCEPTION for destination app $destPkg", e)
                }
            } else {
                Log.w(TAG, "Direct background launch blocked: overlay permission not granted. Relying on high-priority notification.")
            }
        } else if (destPkg != null && !isDestValid) {
            Log.w(TAG, "Configured destination app $destPkg is uninstalled or disabled. Showing warning fallback.")
        } else {
            // No destination app configured: fallback to Learning Gate activity if overlay granted
            if (permissionManager.hasOverlayPermission()) {
                try {
                    val gateIntent = Intent(context, MainActivity::class.java).apply {
                        action = "com.example.doomscroller.ACTION_LEARNING_GATE"
                        putExtra("destination", "learning_gate")
                        putExtra("packageName", packageName)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(gateIntent)
                    Log.i(TAG, "Overlay permission granted: launched Learning Gate screen directly over $packageName")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to launch Learning Gate screen directly with overlay permission", e)
                }
            }
        }

        val rule = prefs.getRule(packageName)
        val intervalMinutes = rule?.limitMinutes ?: 5
        val session = prefs.getReminderSession(packageName)

        // 2. High-priority Warning Notification (fallback and persistent indicator)
        notificationCoordinator.showWarningNotification(
            packageName = packageName,
            appName = appName,
            usedMinutes = (session.qualifyingUsageMillis / 60000L).toInt(),
            limitMinutes = intervalMinutes,
            warningCount = warningCount,
            isPenaltyActive = true,
            remainingSeconds = ((endTimeMillis - now).coerceAtLeast(0L) / 1000).toInt(),
            ignoredRemindersCount = warningCount,
            destinationPackage = if (isDestValid) destPkg else null,
            destinationAppName = destAppName
        )

        _enforcementEvents.tryEmit(
            EnforcementEvent.WarningTriggered(
                packageName = packageName,
                appName = appName,
                warningCount = warningCount,
                isPenalty = true
            )
        )
    }

    fun acknowledgeReminder(packageName: String) {
        acknowledgeIntervention(packageName)
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

    fun getReminderSession(packageName: String): ReminderSession {
        return prefs.getReminderSession(packageName)
    }

    fun getOrInitLearningSession(packageName: String): ReminderSession {
        var session = prefs.getReminderSession(packageName)
        var modified = false
        if (session.learningQuestions.isEmpty()) {
            val qList = QuestionBank.selectDistinctQuestions(2).map { it.id }
            session = session.copy(learningQuestions = qList)
            modified = true
        }
        if (session.activeQuoteId == null) {
            val lastId = prefs.getLastDisplayedQuoteId()
            val quote = MotivationalQuoteEngine.getQuoteForPackage(packageName, excludeId = lastId)
            prefs.saveLastDisplayedQuoteId(quote.id)
            session = session.copy(activeQuoteId = quote.id)
            modified = true
        }
        if (modified) {
            prefs.saveReminderSession(session)
        }
        return session
    }

    @Synchronized
    fun submitLearningAnswer(
        packageName: String,
        questionId: String,
        selectedOption: String
    ): AnswerResult {
        val session = getOrInitLearningSession(packageName)
        val (updatedSession, result) = reminderEscalationEvaluator.submitAnswer(
            session = session,
            questionId = questionId,
            selectedOption = selectedOption
        )
        prefs.saveReminderSession(updatedSession)
        Log.i(TAG, "Answer submitted for $packageName: correct=${result.isCorrect}, completed=${result.isCompleted}")
        return result
    }

    @Synchronized
    fun checkAndReleaseRestriction(packageName: String): Boolean {
        val session = prefs.getReminderSession(packageName)
        val now = System.currentTimeMillis()
        if (session.isRestrictionSatisfied) {
            val rule = prefs.getRule(packageName)
            val intervalMinutes = rule?.limitMinutes ?: 5
            val (resetSession, _) = reminderEscalationEvaluator.evaluate(
                session = session,
                isForeground = false,
                currentUsageMillis = session.qualifyingUsageMillis,
                intervalMinutes = intervalMinutes,
                ignoredLimit = prefs.getIgnoredReminderLimit(),
                now = now,
                restrictionDurationMinutes = prefs.getRestrictionDurationMinutes()
            )
            prefs.saveReminderSession(resetSession)
            prefs.clearActivePenalty(packageName)
            val updatedMap = _activePenalties.value.toMutableMap()
            updatedMap.remove(packageName)
            _activePenalties.value = updatedMap
            _activePenalty.value = updatedMap.values.firstOrNull()
            notificationCoordinator.cancelWarningNotification(packageName)
            rateLimiter.onConditionEnded(packageName)
            _enforcementEvents.tryEmit(EnforcementEvent.PenaltyExpired(packageName))
            Log.i(TAG, "Dual condition satisfied: Released restriction for $packageName and resumed monitoring.")
            return true
        }
        Log.i(TAG, "Cannot release restriction for $packageName yet (learningGateCompleted=${session.isLearningGateCompleted}, penaltyExpired=${session.isPenaltyExpired})")
        return false
    }
}
