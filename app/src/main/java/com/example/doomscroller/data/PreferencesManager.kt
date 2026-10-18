package com.example.doomscroller.data

import android.content.Context
import android.content.SharedPreferences

data class AppRule(
    val packageName: String,
    val limitMinutes: Int,
    val isEnabled: Boolean
)

data class PenaltyInfo(
    val packageName: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val warningCount: Int,
    val isAcknowledged: Boolean = false
) {
    val isExpired: Boolean get() = System.currentTimeMillis() >= endTimeMillis
    val remainingMillis: Long get() = (endTimeMillis - System.currentTimeMillis()).coerceAtLeast(0L)
}

enum class EnforcementState {
    MONITORING,
    REMINDER_DUE,
    REMINDER_SENT,
    ESCALATION_DUE,
    ENFORCING,
    COOLDOWN,
    RECOVERED
}

data class ReminderSession(
    val packageName: String,
    val qualifyingUsageMillis: Long = 0L,
    val reminderCount: Int = 0,
    val ignoredCount: Int = 0,
    val lastReminderTimeMillis: Long = 0L,
    val isAcknowledged: Boolean = false,
    val isEnforcementActive: Boolean = false,
    val enforcementStartTimeMillis: Long = 0L,
    val enforcementEndTimeMillis: Long = 0L,
    val currentState: EnforcementState = EnforcementState.MONITORING,
    val hasEnforcedThisEpisode: Boolean = false
) {
    val isPenaltyExpired: Boolean
        get() = isEnforcementActive && System.currentTimeMillis() >= enforcementEndTimeMillis

    val remainingCooldownMillis: Long
        get() = if (isEnforcementActive) (enforcementEndTimeMillis - System.currentTimeMillis()).coerceAtLeast(0L) else 0L
}

class PreferencesManager(context: Context) : com.example.doomscroller.domain.NotificationPolicyPreferences {
    private val prefs: SharedPreferences = context.getSharedPreferences("doomscroller_rules", Context.MODE_PRIVATE)

    fun getRules(): List<AppRule> {
        val rules = mutableListOf<AppRule>()
        val allEntries = prefs.all
        for ((key, value) in allEntries) {
            if (key.startsWith("warning_") || key.startsWith("active_penalty") ||
                key.startsWith("setting_") || key.startsWith("pref_") ||
                key.startsWith("history_") || key.startsWith("last_active_") ||
                key.startsWith("session_")) continue
            if (value is String) {
                // simple serialization: limitMinutes,isEnabled
                val parts = value.split(",")
                if (parts.size == 2) {
                    val limit = parts[0].toIntOrNull() ?: 60
                    val enabled = parts[1].toBooleanStrictOrNull() ?: true
                    rules.add(AppRule(key, limit, enabled))
                }
            }
        }
        return rules
    }

    fun getRule(packageName: String): AppRule? {
        val value = prefs.getString(packageName, null) ?: return null
        val parts = value.split(",")
        if (parts.size == 2) {
            val limit = parts[0].toIntOrNull() ?: 60
            val enabled = parts[1].toBooleanStrictOrNull() ?: true
            return AppRule(packageName, limit, enabled)
        }
        return null
    }

    fun saveRule(rule: AppRule) {
        prefs.edit().putString(rule.packageName, "${rule.limitMinutes},${rule.isEnabled}").apply()
    }

    fun removeRule(packageName: String) {
        prefs.edit()
            .remove(packageName)
            .remove("active_penalty_$packageName")
            .remove("active_penalty")
            .remove("warning_$packageName")
            .remove("session_$packageName")
            .remove("history_$packageName")
            .remove("last_active_$packageName")
            .apply()
    }

    fun getWarningState(packageName: String): com.example.doomscroller.domain.WarningState? {
        val str = prefs.getString("warning_$packageName", null) ?: return null
        val parts = str.split(";")
        if (parts.size >= 4) {
            val date = try { java.time.LocalDate.parse(parts[1]) } catch (e: Exception) { return null }
            val count = parts[2].toIntOrNull() ?: return null
            val time = parts[3].toLongOrNull() ?: return null
            val penaltyEnd = if (parts.size >= 5) parts[4].toLongOrNull() ?: 0L else 0L
            return com.example.doomscroller.domain.WarningState(parts[0], date, count, time, penaltyEnd)
        }
        return null
    }

    fun saveWarningState(state: com.example.doomscroller.domain.WarningState) {
        prefs.edit().putString(
            "warning_${state.packageName}",
            "${state.packageName};${state.date};${state.warningCount};${state.lastWarningTimeMillis};${state.penaltyEndTimeMillis}"
        ).apply()
    }

    fun clearWarningState(packageName: String) {
        prefs.edit().remove("warning_$packageName").apply()
    }

    private fun parsePenaltyString(str: String): PenaltyInfo? {
        val parts = str.split(";")
        if (parts.size >= 5) {
            val pkg = parts[0]
            val start = parts[1].toLongOrNull() ?: return null
            val end = parts[2].toLongOrNull() ?: return null
            val count = parts[3].toIntOrNull() ?: return null
            val ack = parts[4].toBooleanStrictOrNull() ?: false
            return PenaltyInfo(pkg, start, end, count, ack)
        }
        return null
    }

    fun getActivePenalty(packageName: String): PenaltyInfo? {
        val str = prefs.getString("active_penalty_$packageName", null)
            ?: prefs.getString("active_penalty", null)
            ?: return null
        val parsed = parsePenaltyString(str)
        return if (parsed?.packageName == packageName) parsed else null
    }

    fun getAllActivePenalties(): Map<String, PenaltyInfo> {
        val result = mutableMapOf<String, PenaltyInfo>()
        for ((key, value) in prefs.all) {
            if (key.startsWith("active_penalty_") && value is String) {
                parsePenaltyString(value)?.let { penalty ->
                    result[penalty.packageName] = penalty
                }
            }
        }
        // Check legacy key if empty
        if (result.isEmpty()) {
            val legacy = prefs.getString("active_penalty", null)
            if (legacy != null) {
                parsePenaltyString(legacy)?.let { result[it.packageName] = it }
            }
        }
        return result
    }

    fun getActivePenalty(): PenaltyInfo? {
        return getAllActivePenalties().values.firstOrNull()
    }

    fun saveActivePenalty(penalty: PenaltyInfo) {
        prefs.edit()
            .putString(
                "active_penalty_${penalty.packageName}",
                "${penalty.packageName};${penalty.startTimeMillis};${penalty.endTimeMillis};${penalty.warningCount};${penalty.isAcknowledged}"
            )
            .remove("active_penalty") // Clear legacy key
            .apply()
    }

    fun clearActivePenalty(packageName: String) {
        prefs.edit()
            .remove("active_penalty_$packageName")
            .remove("active_penalty")
            .apply()
    }

    fun clearActivePenalty() {
        val editor = prefs.edit().remove("active_penalty")
        for ((key, _) in prefs.all) {
            if (key.startsWith("active_penalty_")) {
                editor.remove(key)
            }
        }
        editor.apply()
    }

    // --- Onboarding Completion ---
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean("pref_onboarding_completed", false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("pref_onboarding_completed", completed).apply()
    }

    // --- Exact Reminder Policy Configuration ---
    // Default 5 mins, bounds: 1 min to 60 mins
    override fun getReminderIntervalMinutes(): Int {
        val value = prefs.getInt("setting_reminder_interval_minutes", 5)
        return value.coerceIn(1, 60)
    }

    override fun setReminderIntervalMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(1, 60)
        prefs.edit().putInt("setting_reminder_interval_minutes", clamped).apply()
    }

    // Default 3 per 15 min, bounds: 1 to 6 per 15 min
    override fun getReminderCapPer15Min(): Int {
        val value = prefs.getInt("setting_reminder_cap_per_15m", 3)
        return value.coerceIn(1, 6)
    }

    override fun setReminderCapPer15Min(cap: Int) {
        val clamped = cap.coerceIn(1, 6)
        prefs.edit().putInt("setting_reminder_cap_per_15m", clamped).apply()
    }

    // --- Notification History for 15-Minute Rolling Window ---
    override fun getNotificationHistory(packageName: String): List<Long> {
        val str = prefs.getString("history_$packageName", null) ?: return emptyList()
        return str.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .sorted()
    }

    override fun saveNotificationHistory(packageName: String, timestamps: List<Long>) {
        if (timestamps.isEmpty()) {
            clearNotificationHistory(packageName)
        } else {
            val serialized = timestamps.sorted().joinToString(",")
            prefs.edit().putString("history_$packageName", serialized).apply()
        }
    }

    override fun clearNotificationHistory(packageName: String) {
        prefs.edit().remove("history_$packageName").apply()
    }

    override fun getLastActiveNotificationTime(packageName: String): Long {
        return prefs.getLong("last_active_$packageName", 0L)
    }

    override fun setLastActiveNotificationTime(packageName: String, time: Long) {
        prefs.edit().putLong("last_active_$packageName", time).apply()
    }

    override fun clearActiveReminderState(packageName: String) {
        prefs.edit().remove("last_active_$packageName").apply()
    }

    // --- Z - Ignored Reminder Limit (1..10, default: 3) ---
    fun getIgnoredReminderLimit(): Int {
        val value = prefs.getInt("setting_ignored_reminder_limit", 3)
        return value.coerceIn(1, 10)
    }

    fun setIgnoredReminderLimit(limit: Int) {
        val clamped = limit.coerceIn(1, 10)
        prefs.edit().putInt("setting_ignored_reminder_limit", clamped).apply()
    }

    // --- Configurable Destination App Setting ---
    fun getDestinationPackage(): String? {
        val pkg = prefs.getString("setting_destination_package", null)
        return if (pkg.isNullOrBlank()) null else pkg
    }

    fun setDestinationPackage(packageName: String?) {
        if (packageName.isNullOrBlank()) {
            prefs.edit().remove("setting_destination_package").apply()
        } else {
            prefs.edit().putString("setting_destination_package", packageName).apply()
        }
    }

    // --- ReminderSession State Persistence ---
    private fun parseReminderSession(str: String): ReminderSession? {
        val parts = str.split(";")
        if (parts.size >= 9) {
            val pkg = parts[0]
            val usage = parts[1].toLongOrNull() ?: return null
            val count = parts[2].toIntOrNull() ?: return null
            val ignored = parts[3].toIntOrNull() ?: return null
            val lastTime = parts[4].toLongOrNull() ?: return null
            val ack = parts[5].toBooleanStrictOrNull() ?: false
            val active = parts[6].toBooleanStrictOrNull() ?: false
            val start = parts[7].toLongOrNull() ?: return null
            val end = parts[8].toLongOrNull() ?: return null
            val state = if (parts.size >= 10) {
                try {
                    EnforcementState.valueOf(parts[9])
                } catch (e: Exception) {
                    if (active) EnforcementState.COOLDOWN else EnforcementState.MONITORING
                }
            } else {
                if (active) EnforcementState.COOLDOWN else EnforcementState.MONITORING
            }
            val hasEnforced = if (parts.size >= 11) {
                parts[10].toBooleanStrictOrNull() ?: active
            } else {
                active
            }
            return ReminderSession(pkg, usage, count, ignored, lastTime, ack, active, start, end, state, hasEnforced)
        }
        return null
    }

    fun getReminderSession(packageName: String): ReminderSession {
        val str = prefs.getString("session_$packageName", null) ?: return ReminderSession(packageName = packageName)
        return parseReminderSession(str) ?: ReminderSession(packageName = packageName)
    }

    fun saveReminderSession(session: ReminderSession) {
        val serialized = "${session.packageName};${session.qualifyingUsageMillis};${session.reminderCount};" +
                "${session.ignoredCount};${session.lastReminderTimeMillis};${session.isAcknowledged};" +
                "${session.isEnforcementActive};${session.enforcementStartTimeMillis};${session.enforcementEndTimeMillis};" +
                "${session.currentState.name};${session.hasEnforcedThisEpisode}"
        prefs.edit().putString("session_${session.packageName}", serialized).apply()
    }

    fun clearReminderSession(packageName: String) {
        prefs.edit().remove("session_$packageName").apply()
    }

    fun getAllReminderSessions(): Map<String, ReminderSession> {
        val map = mutableMapOf<String, ReminderSession>()
        for ((key, value) in prefs.all) {
            if (key.startsWith("session_") && value is String) {
                parseReminderSession(value)?.let { session ->
                    map[session.packageName] = session
                }
            }
        }
        return map
    }
}
