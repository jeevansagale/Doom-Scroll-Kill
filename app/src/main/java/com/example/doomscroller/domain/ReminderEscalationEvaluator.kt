package com.example.doomscroller.domain

import android.util.Log
import com.example.doomscroller.data.EnforcementState
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
    data class PenaltyExpired(
        val packageName: String
    ) : EscalationDecision()
}

class ReminderEscalationEvaluator {
    companion object {
        private const val TAG = "EnforcementStateMachine"
        const val PENALTY_COOLDOWN_MILLIS = 5 * 60 * 1000L // Fixed 5-minute penalty duration
    }

    private fun logTransition(
        packageName: String,
        fromState: EnforcementState,
        toState: EnforcementState,
        reason: String
    ) {
        Log.i(TAG, "[$packageName] TRANSITION: $fromState -> $toState | Reason: $reason")
    }

    /**
     * Evaluates the reminder and escalation state machine for a monitored package.
     *
     * State Machine Lifecycle:
     * MONITORING -> REMINDER_DUE -> REMINDER_SENT -> ESCALATION_DUE -> ENFORCING -> COOLDOWN -> RECOVERED -> MONITORING
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

        // 1. If currently in COOLDOWN or enforcement penalty is active
        if (session.currentState == EnforcementState.COOLDOWN || session.isEnforcementActive) {
            if (now >= session.enforcementEndTimeMillis) {
                // Penalty cooldown completed: COOLDOWN -> RECOVERED -> MONITORING
                logTransition(
                    session.packageName,
                    session.currentState,
                    EnforcementState.RECOVERED,
                    "Enforcement penalty duration elapsed ($now >= ${session.enforcementEndTimeMillis})"
                )
                logTransition(
                    session.packageName,
                    EnforcementState.RECOVERED,
                    EnforcementState.MONITORING,
                    "Resetting baseline to current usage ($currentUsageMillis ms) and resuming monitoring cycle"
                )
                val resetSession = ReminderSession(
                    packageName = session.packageName,
                    qualifyingUsageMillis = currentUsageMillis, // Set new baseline after penalty
                    reminderCount = 0,
                    ignoredCount = 0,
                    lastReminderTimeMillis = 0L,
                    isAcknowledged = false,
                    isEnforcementActive = false,
                    enforcementStartTimeMillis = 0L,
                    enforcementEndTimeMillis = 0L,
                    currentState = EnforcementState.MONITORING,
                    hasEnforcedThisEpisode = false
                )
                return resetSession to EscalationDecision.PenaltyExpired(session.packageName)
            } else {
                // Cooldown still active: prevent duplicate actions while the same enforcement event remains active
                val updatedSession = if (session.currentState != EnforcementState.COOLDOWN) {
                    session.copy(currentState = EnforcementState.COOLDOWN)
                } else session
                return updatedSession to EscalationDecision.NoAction
            }
        }

        // 2. If app is NOT in the foreground
        if (!isForeground) {
            // Timer and counters do not increment when app is in the background or user switches away
            return session.copy(qualifyingUsageMillis = currentUsageMillis) to EscalationDecision.NoAction
        }

        // 3. App is currently in the foreground
        // First Reminder check: has usage reached X minutes?
        val cycleUsage = (currentUsageMillis - session.qualifyingUsageMillis).coerceAtLeast(0L)
        if (session.reminderCount == 0) {
            if (cycleUsage >= intervalMillis || currentUsageMillis >= intervalMillis) {
                // First reminder reached: MONITORING -> REMINDER_DUE
                logTransition(
                    session.packageName,
                    session.currentState,
                    EnforcementState.REMINDER_DUE,
                    "Foreground usage reached configured interval of $intervalMinutes min ($currentUsageMillis ms)"
                )
                val newSession = session.copy(
                    reminderCount = 1,
                    ignoredCount = 0,
                    lastReminderTimeMillis = now,
                    isAcknowledged = false,
                    currentState = EnforcementState.REMINDER_DUE
                )
                return newSession to EscalationDecision.SendReminder(
                    packageName = session.packageName,
                    reminderNumber = 1,
                    ignoredCount = 0,
                    limitCount = ignoredLimit,
                    intervalMinutes = intervalMinutes
                )
            } else {
                return session to EscalationDecision.NoAction
            }
        }

        // Subsequent Reminder & Ignored Limit check:
        // Has another interval elapsed while continuing to use the monitored app without acknowledging?
        val elapsedSinceLastReminder = now - session.lastReminderTimeMillis
        if (elapsedSinceLastReminder >= intervalMillis) {
            // Sustained use beyond the next interval without acknowledging -> previous reminder was IGNORED
            val newIgnoredCount = session.ignoredCount + 1

            if (newIgnoredCount >= ignoredLimit) {
                // Escalation limit reached: REMINDER_SENT -> ESCALATION_DUE -> ENFORCING
                logTransition(
                    session.packageName,
                    session.currentState,
                    EnforcementState.ESCALATION_DUE,
                    "Ignored reminder limit reached: $newIgnoredCount >= $ignoredLimit"
                )
                logTransition(
                    session.packageName,
                    EnforcementState.ESCALATION_DUE,
                    EnforcementState.ENFORCING,
                    "Activating penalty enforcement"
                )
                val penaltyEnd = now + PENALTY_COOLDOWN_MILLIS
                val escalatedSession = session.copy(
                    ignoredCount = newIgnoredCount,
                    isEnforcementActive = true,
                    enforcementStartTimeMillis = now,
                    enforcementEndTimeMillis = penaltyEnd,
                    currentState = EnforcementState.ENFORCING,
                    hasEnforcedThisEpisode = false
                )
                return escalatedSession to EscalationDecision.TriggerEnforcement(
                    packageName = session.packageName,
                    ignoredCount = newIgnoredCount,
                    penaltyEndTimeMillis = penaltyEnd
                )
            } else {
                // Next repeat reminder: REMINDER_SENT -> REMINDER_DUE
                val nextReminderNumber = session.reminderCount + 1
                logTransition(
                    session.packageName,
                    session.currentState,
                    EnforcementState.REMINDER_DUE,
                    "Repeat interval elapsed: sending reminder #$nextReminderNumber (ignored=$newIgnoredCount/$ignoredLimit)"
                )
                val newSession = session.copy(
                    reminderCount = nextReminderNumber,
                    ignoredCount = newIgnoredCount,
                    lastReminderTimeMillis = now,
                    isAcknowledged = false,
                    currentState = EnforcementState.REMINDER_DUE
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
        return session to EscalationDecision.NoAction
    }

    /**
     * Called when a reminder notification was successfully submitted to Android's notification system.
     * Transitions: REMINDER_DUE -> REMINDER_SENT
     */
    fun markReminderSent(session: ReminderSession): ReminderSession {
        logTransition(
            session.packageName,
            session.currentState,
            EnforcementState.REMINDER_SENT,
            "Reminder #${session.reminderCount} successfully submitted to notification system"
        )
        return session.copy(currentState = EnforcementState.REMINDER_SENT)
    }

    /**
     * Called when the configured enforcement action has been executed.
     * Transitions: ENFORCING -> COOLDOWN
     */
    fun markEnforced(session: ReminderSession): ReminderSession {
        logTransition(
            session.packageName,
            session.currentState,
            EnforcementState.COOLDOWN,
            "Enforcement action dispatched; entering COOLDOWN until ${session.enforcementEndTimeMillis}"
        )
        return session.copy(
            currentState = EnforcementState.COOLDOWN,
            hasEnforcedThisEpisode = true
        )
    }

    /**
     * User explicitly acknowledges a reminder.
     * Resets the consecutive ignored count to 0 and resets the interval timer.
     * Transitions: REMINDER_DUE / REMINDER_SENT -> MONITORING
     */
    fun acknowledge(session: ReminderSession, now: Long = System.currentTimeMillis()): ReminderSession {
        logTransition(
            session.packageName,
            session.currentState,
            EnforcementState.MONITORING,
            "User acknowledged reminder. Resetting ignored count to 0."
        )
        return session.copy(
            ignoredCount = 0,
            isAcknowledged = true,
            lastReminderTimeMillis = now,
            currentState = EnforcementState.MONITORING
        )
    }

    /**
     * Resets active session when rule disabled or app removed.
     */
    fun reset(packageName: String): ReminderSession {
        return ReminderSession(packageName = packageName, currentState = EnforcementState.MONITORING)
    }
}
