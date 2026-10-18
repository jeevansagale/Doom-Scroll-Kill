package com.example.doomscroller.domain

import android.util.Log
import com.example.doomscroller.data.EnforcementState
import com.example.doomscroller.data.ReminderSession
import com.example.doomscroller.domain.learning.QuestionBank
import com.example.doomscroller.domain.quotes.MotivationalQuoteEngine

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

data class AnswerResult(
    val isCorrect: Boolean,
    val explanation: String,
    val isCompleted: Boolean,
    val correctCount: Int,
    val totalRequired: Int = 2
)

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
     * MONITORING -> REMINDER_DUE -> REMINDER_SENT -> ESCALATION_DUE -> ENFORCING ->
     * LEARNING_GATE_REQUIRED -> LEARNING_GATE_IN_PROGRESS -> LEARNING_GATE_COMPLETED ->
     * RESTRICTION_RELEASED -> RECOVERED -> MONITORING
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

        // 1. If currently in enforcement / cooldown / learning gate
        if (session.isEnforcementActive || session.currentState == EnforcementState.COOLDOWN ||
            session.currentState == EnforcementState.LEARNING_GATE_REQUIRED ||
            session.currentState == EnforcementState.LEARNING_GATE_IN_PROGRESS ||
            session.currentState == EnforcementState.LEARNING_GATE_COMPLETED) {

            val isTimerExpired = now >= session.enforcementEndTimeMillis
            val isLearningCompleted = session.isLearningGateCompleted

            // The restriction remains active until BOTH the configured penalty duration has elapsed
            // AND the learning gate is successfully completed.
            if (isTimerExpired && isLearningCompleted) {
                logTransition(
                    session.packageName,
                    session.currentState,
                    EnforcementState.RESTRICTION_RELEASED,
                    "Dual conditions satisfied: penalty cooldown elapsed ($now >= ${session.enforcementEndTimeMillis}) and learning gate completed"
                )
                logTransition(
                    session.packageName,
                    EnforcementState.RESTRICTION_RELEASED,
                    EnforcementState.RECOVERED,
                    "Resetting baseline to current usage ($currentUsageMillis ms) and resuming monitoring cycle"
                )
                logTransition(
                    session.packageName,
                    EnforcementState.RECOVERED,
                    EnforcementState.MONITORING,
                    "Resuming active monitoring"
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
                    hasEnforcedThisEpisode = false,
                    activeQuoteId = null,
                    learningQuestions = emptyList(),
                    answeredQuestions = emptyList(),
                    isLearningGateCompleted = false
                )
                return resetSession to EscalationDecision.PenaltyExpired(session.packageName)
            } else {
                // Restriction remains active: update state reflection if needed
                val targetState = when {
                    session.isLearningGateCompleted -> EnforcementState.LEARNING_GATE_COMPLETED
                    session.answeredQuestions.isNotEmpty() -> EnforcementState.LEARNING_GATE_IN_PROGRESS
                    session.hasEnforcedThisEpisode -> EnforcementState.LEARNING_GATE_REQUIRED
                    else -> session.currentState
                }
                val updatedSession = if (session.currentState != targetState) {
                    session.copy(currentState = targetState)
                } else session
                return updatedSession to EscalationDecision.NoAction
            }
        }

        // 2. If app is NOT in the foreground
        if (!isForeground) {
            // Timer and counters do not increment when app is in the background or user switches away
            return session to EscalationDecision.NoAction
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
                    "Activating penalty enforcement and selecting learning questions"
                )
                val penaltyEnd = now + PENALTY_COOLDOWN_MILLIS
                val selectedQuote = MotivationalQuoteEngine.getQuoteForPackage(session.packageName).id
                val selectedQuestions = QuestionBank.selectDistinctQuestions(2).map { it.id }

                val escalatedSession = session.copy(
                    ignoredCount = newIgnoredCount,
                    isEnforcementActive = true,
                    enforcementStartTimeMillis = now,
                    enforcementEndTimeMillis = penaltyEnd,
                    currentState = EnforcementState.ENFORCING,
                    hasEnforcedThisEpisode = false,
                    activeQuoteId = selectedQuote,
                    learningQuestions = selectedQuestions,
                    answeredQuestions = emptyList(),
                    isLearningGateCompleted = false
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
     * Transitions: ENFORCING -> LEARNING_GATE_REQUIRED
     */
    fun markEnforced(session: ReminderSession): ReminderSession {
        logTransition(
            session.packageName,
            session.currentState,
            EnforcementState.LEARNING_GATE_REQUIRED,
            "Enforcement action dispatched; learning gate required until 2 questions completed and cooldown elapses (${session.enforcementEndTimeMillis})"
        )
        return session.copy(
            currentState = EnforcementState.LEARNING_GATE_REQUIRED,
            hasEnforcedThisEpisode = true
        )
    }

    /**
     * Validates and submits an answer to a learning gate question.
     * Exactly 2 distinct questions must be answered correctly.
     * Incorrect answers do NOT advance the counter and return the concise explanation.
     */
    fun submitAnswer(
        session: ReminderSession,
        questionId: String,
        selectedOption: String
    ): Pair<ReminderSession, AnswerResult> {
        val question = QuestionBank.getQuestionById(questionId)
        if (question == null) {
            return session to AnswerResult(
                isCorrect = false,
                explanation = "Invalid question reference.",
                isCompleted = session.isLearningGateCompleted,
                correctCount = session.answeredQuestions.size
            )
        }

        val isCorrect = question.validateAnswer(selectedOption)
        if (!isCorrect) {
            // Incorrect answer: do not advance count, show explanation
            val updated = session.copy(currentState = EnforcementState.LEARNING_GATE_IN_PROGRESS)
            return updated to AnswerResult(
                isCorrect = false,
                explanation = question.explanation,
                isCompleted = false,
                correctCount = session.answeredQuestions.size
            )
        }

        // Correct answer
        val updatedAnswered = if (session.answeredQuestions.contains(questionId)) {
            session.answeredQuestions
        } else {
            session.answeredQuestions + questionId
        }

        val isCompleted = updatedAnswered.size >= 2
        val newState = if (isCompleted) {
            EnforcementState.LEARNING_GATE_COMPLETED
        } else {
            EnforcementState.LEARNING_GATE_IN_PROGRESS
        }

        logTransition(
            session.packageName,
            session.currentState,
            newState,
            "Answered question $questionId correctly (${updatedAnswered.size}/2 completed)"
        )

        val updatedSession = session.copy(
            answeredQuestions = updatedAnswered,
            isLearningGateCompleted = isCompleted,
            currentState = newState
        )

        return updatedSession to AnswerResult(
            isCorrect = true,
            explanation = question.explanation,
            isCompleted = isCompleted,
            correctCount = updatedAnswered.size
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
