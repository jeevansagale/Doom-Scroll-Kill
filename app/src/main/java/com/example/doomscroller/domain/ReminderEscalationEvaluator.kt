package com.example.doomscroller.domain

import android.util.Log
import com.example.doomscroller.data.ReminderSession

sealed class EscalationDecision {
    object NoAction : EscalationDecision()
    data class SendReminder(
        val packageName: String,
        val reminderNumber: Int,
        val ignoredCount: Int,
        val limitCount: Int,
        val intervalMinutes: Int
    ) : EscalationDecision()
    data class TriggerEnforcement(
        val packageName: String,
        val ignoredCount: Int,
        val penaltyEndTimeMillis: Long
    ) : EscalationDecision()
}

class ReminderEscalationEvaluator {
    companion object {
        private const val TAG = "ReminderEscalation"
        const val PENALTY_COOLDOWN_MILLIS = 5 * 60 * 1000L // Fixed 5-minute penalty duration
    }

    /**
     * Evaluates the reminder and escalation state for a monitored package.
     *
     * @param session Current persisted session state for the package.
     * @param isForeground Whether the package is currently in the foreground.
     * @param currentUsageMillis Total daily or qualifying foreground usage in milliseconds.
     * @param intervalMinutes X — Configured reminder interval in minutes.
     * @param ignoredLimit Z — Configured consecutive ignored reminder limit before escalation.
     * @param now Current timestamp in milliseconds.
     */
    fun evaluate(
        session: ReminderSession,
        isForeground: Boolean,
        currentUsageMillis: Long,
        intervalMinutes: Int,
        ignoredLimit: Int,
        now: Long = System.currentTimeMillis()
    ): Pair<ReminderSession, EscalationDecision> {
        val intervalMillis = intervalMinutes * 60 * 1000L

        // 1. If enforcement penalty is active
        if (session.isEnforcementActive) {
            if (now >= session.enforcementEndTimeMillis) {
                // Penalty cooldown expired! Reset enforcement state
                Log.i(TAG, "[${session.packageName}] Enforcement cooldown completed. Resetting session.")
                val resetSession = ReminderSession(
                    packageName = session.packageName,
                    qualifyingUsageMillis = currentUsageMillis,
                    reminderCount = 0,
                    ignoredCount = 0,
                    lastReminderTimeMillis = 0L,
                    isAcknowledged = false,
                    isEnforcementActive = false,
                    enforcementStartTimeMillis = 0L,
                    enforcementEndTimeMillis = 0L
                )
                return resetSession to EscalationDecision.NoAction
            } else {
                // Penalty cooldown still in progress
                return session to EscalationDecision.NoAction
            }
        }

        // 2. If app is NOT in the foreground
        if (!isForeground) {
            // Timer and counters do not increment when app is in the background or user switches away
            return session.copy(qualifyingUsageMillis = currentUsageMillis) to EscalationDecision.NoAction
        }

        // 3. App is currently in the foreground
        // First Reminder check: has usage reached X minutes?
        if (session.reminderCount == 0) {
            if (currentUsageMillis >= intervalMillis) {
                // First reminder reached!
                Log.i(TAG, "[${session.packageName}] Threshold X reached ($intervalMinutes min). Sending Reminder #1.")
                val newSession = session.copy(
                    qualifyingUsageMillis = currentUsageMillis,
                    reminderCount = 1,
                    ignoredCount = 0,
                    lastReminderTimeMillis = now,
                    isAcknowledged = false
                )
                return newSession to EscalationDecision.SendReminder(
                    packageName = session.packageName,
                    reminderNumber = 1,
                    ignoredCount = 0,
                    limitCount = ignoredLimit,
                    intervalMinutes = intervalMinutes
                )
            } else {
                return session.copy(qualifyingUsageMillis = currentUsageMillis) to EscalationDecision.NoAction
            }
        }

        // Subsequent Reminder & Ignored Limit check:
        // Has another interval elapsed while continuing to use the monitored app without acknowledging?
        val elapsedSinceLastReminder = now - session.lastReminderTimeMillis
        if (elapsedSinceLastReminder >= intervalMillis) {
            // Sustained use beyond the next interval without acknowledging -> previous reminder was IGNORED
            val newIgnoredCount = session.ignoredCount + 1
            Log.w(TAG, "[${session.packageName}] Sustained use without acknowledgement: ignoredCount=$newIgnoredCount/$ignoredLimit.")

            if (newIgnoredCount >= ignoredLimit) {
                // Escalation limit reached! Trigger enforcement exactly once
                val penaltyEnd = now + PENALTY_COOLDOWN_MILLIS
                Log.w(TAG, "[${session.packageName}] ESCALATION ACTIVATED: $newIgnoredCount reminders ignored (>= $ignoredLimit).")
                val escalatedSession = session.copy(
                    qualifyingUsageMillis = currentUsageMillis,
                    ignoredCount = newIgnoredCount,
                    isEnforcementActive = true,
                    enforcementStartTimeMillis = now,
                    enforcementEndTimeMillis = penaltyEnd
                )
                return escalatedSession to EscalationDecision.TriggerEnforcement(
                    packageName = session.packageName,
                    ignoredCount = newIgnoredCount,
                    penaltyEndTimeMillis = penaltyEnd
                )
            } else {
                // Next repeat reminder
                val nextReminderNumber = session.reminderCount + 1
                Log.i(TAG, "[${session.packageName}] Sending Repeat Reminder #$nextReminderNumber (ignored=$newIgnoredCount).")
                val newSession = session.copy(
                    qualifyingUsageMillis = currentUsageMillis,
                    reminderCount = nextReminderNumber,
                    ignoredCount = newIgnoredCount,
                    lastReminderTimeMillis = now,
                    isAcknowledged = false
                )
                return newSession to EscalationDecision.SendReminder(
                    packageName = session.packageName,
                    reminderNumber = nextReminderNumber,
                    ignoredCount = newIgnoredCount,
                    limitCount = ignoredLimit,
                    intervalMinutes = intervalMinutes
                )
            }
        }

        // Within interval, no action needed
        return session.copy(qualifyingUsageMillis = currentUsageMillis) to EscalationDecision.NoAction
    }

    /**
     * User explicitly acknowledges a reminder.
     * Resets the consecutive ignored count to 0 and resets the interval timer.
     */
    fun acknowledge(session: ReminderSession, now: Long = System.currentTimeMillis()): ReminderSession {
        Log.i(TAG, "[${session.packageName}] User acknowledged reminder. Resetting ignored count.")
        return session.copy(
            ignoredCount = 0,
            isAcknowledged = true,
            lastReminderTimeMillis = now
        )
    }

    /**
     * Resets active session when rule disabled or app removed.
     */
    fun reset(packageName: String): ReminderSession {
        return ReminderSession(packageName = packageName)
    }
}
