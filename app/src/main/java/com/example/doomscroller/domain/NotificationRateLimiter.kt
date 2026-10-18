package com.example.doomscroller.domain

import android.util.Log
import com.example.doomscroller.data.PreferencesManager

interface NotificationPolicyPreferences {
    fun getReminderIntervalMinutes(): Int
    fun setReminderIntervalMinutes(minutes: Int)
    fun getReminderCapPer15Min(): Int
    fun setReminderCapPer15Min(cap: Int)
    fun getNotificationHistory(packageName: String): List<Long>
    fun saveNotificationHistory(packageName: String, timestamps: List<Long>)
    fun clearNotificationHistory(packageName: String)
    fun getLastActiveNotificationTime(packageName: String): Long
    fun setLastActiveNotificationTime(packageName: String, time: Long)
    fun clearActiveReminderState(packageName: String)
}

data class RateLimitResult(
    val allowed: Boolean,
    val countIn15mWindow: Int,
    val cap: Int,
    val intervalMinutes: Int,
    val reason: String
)

class NotificationRateLimiter(private val prefs: NotificationPolicyPreferences) {
    companion object {
        private const val TAG = "NotificationRateLimiter"
        const val WINDOW_MILLIS = 15 * 60 * 1000L // 15-minute rolling window
        const val DEFAULT_INTERVAL_MINUTES = 5
        const val MIN_INTERVAL_MINUTES = 1
        const val MAX_INTERVAL_MINUTES = 60
        const val DEFAULT_CAP = 3
        const val MIN_CAP = 1
        const val MAX_CAP = 6
    }

    /**
     * Evaluates whether a notification is permitted for [packageName] at [now].
     *
     * @param packageName The package identifier of the monitored application.
     * @param now Current timestamp in milliseconds.
     * @param isNewEpisode True if this is an initial threshold event or re-triggered event.
     *                     False if this is a repeat reminder or background poll check.
     * @param recordIfAllowed If true, records [now] to persisted history when permitted.
     */
    @Synchronized
    fun evaluateNotification(
        packageName: String,
        now: Long = System.currentTimeMillis(),
        isNewEpisode: Boolean = false,
        recordIfAllowed: Boolean = true
    ): RateLimitResult {
        val intervalMinutes = prefs.getReminderIntervalMinutes()
        val intervalMillis = intervalMinutes * 60 * 1000L
        val cap = prefs.getReminderCapPer15Min()

        val history = prefs.getNotificationHistory(packageName)
        val cutoff = now - WINDOW_MILLIS
        val rollingWindow = history.filter { it in cutoff..now }

        // Clean up pruned history if older timestamps have expired
        if (rollingWindow.size != history.size) {
            prefs.saveNotificationHistory(packageName, rollingWindow)
        }

        // Rule 3: Rolling 15-minute frequency cap (never exceed cap in any rolling 15m window)
        if (rollingWindow.size >= cap) {
            val reason = "Rolling 15-minute cap of $cap notifications reached (${rollingWindow.size}/$cap in window)"
            Log.w(TAG, "[$packageName] SUPPRESSED: $reason")
            return RateLimitResult(
                allowed = false,
                countIn15mWindow = rollingWindow.size,
                cap = cap,
                intervalMinutes = intervalMinutes,
                reason = reason
            )
        }

        // Rule 2 & 4: Repeat interval & Deduplication for existing active episodes
        if (!isNewEpisode) {
            val lastActive = prefs.getLastActiveNotificationTime(packageName)
            if (lastActive > 0L) {
                val elapsed = now - lastActive
                if (elapsed < intervalMillis) {
                    val remainingSec = (intervalMillis - elapsed) / 1000
                    val reason = "Reminder interval of ${intervalMinutes}m not elapsed (${elapsed / 1000}s elapsed, ${remainingSec}s remaining)"
                    Log.d(TAG, "[$packageName] SUPPRESSED: $reason")
                    return RateLimitResult(
                        allowed = false,
                        countIn15mWindow = rollingWindow.size,
                        cap = cap,
                        intervalMinutes = intervalMinutes,
                        reason = reason
                    )
                }
            }
        }

        // Permitted
        val newCount = rollingWindow.size + 1
        if (recordIfAllowed) {
            val updated = rollingWindow + now
            prefs.saveNotificationHistory(packageName, updated)
            prefs.setLastActiveNotificationTime(packageName, now)
        }

        val reason = "Notification permitted (isNewEpisode=$isNewEpisode). Window count: $newCount/$cap. Interval: ${intervalMinutes}m"
        Log.i(TAG, "[$packageName] PERMITTED: $reason")
        return RateLimitResult(
            allowed = true,
            countIn15mWindow = newCount,
            cap = cap,
            intervalMinutes = intervalMinutes,
            reason = reason
        )
    }

    /**
     * Resets active reminder state when quota/penalty condition ends (Rule 5).
     * Preserves rolling 15-minute history so re-triggering still respects the rolling cap (Rule 6).
     */
    @Synchronized
    fun onConditionEnded(packageName: String) {
        prefs.clearActiveReminderState(packageName)
        Log.i(TAG, "[$packageName] Cleared active episode reminder state (condition ended)")
    }

    /**
     * Complete reset of all history and active state (e.g. app removed from monitoring).
     */
    @Synchronized
    fun resetAll(packageName: String) {
        prefs.clearNotificationHistory(packageName)
        prefs.clearActiveReminderState(packageName)
        Log.i(TAG, "[$packageName] Cleared all notification history and active reminder state")
    }
}
