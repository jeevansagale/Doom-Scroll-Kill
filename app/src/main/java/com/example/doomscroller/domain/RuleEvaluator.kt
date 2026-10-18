package com.example.doomscroller.domain

import com.example.doomscroller.data.AppRule
import java.time.LocalDate

data class WarningState(
    val packageName: String,
    val date: LocalDate,
    val warningCount: Int,
    val lastWarningTimeMillis: Long,
    val penaltyEndTimeMillis: Long = 0L
)

sealed class EvaluationResult {
    object NoAction : EvaluationResult()
    data class ShowWarning(val newWarningState: WarningState) : EvaluationResult()
    data class Escalate(val newWarningState: WarningState) : EvaluationResult()
}

class RuleEvaluator {
    companion object {
        const val MINIMUM_PENALTY_MILLIS = 5 * 60 * 1000L // 5 minutes minimum penalty
        const val MAX_WARNINGS = 3
    }

    fun evaluate(
        rule: AppRule,
        usedMinutes: Int,
        currentTimeMillis: Long,
        currentDate: LocalDate,
        previousState: WarningState?
    ): EvaluationResult {
        if (!rule.isEnabled) return EvaluationResult.NoAction
        if (usedMinutes < rule.limitMinutes) return EvaluationResult.NoAction

        val state = if (previousState == null || previousState.date != currentDate) {
            WarningState(rule.packageName, currentDate, 0, 0, 0L)
        } else {
            previousState
        }

        if (state.warningCount == 0) {
            val newState = state.copy(
                warningCount = 1,
                lastWarningTimeMillis = currentTimeMillis,
                penaltyEndTimeMillis = currentTimeMillis + MINIMUM_PENALTY_MILLIS
            )
            return EvaluationResult.ShowWarning(newState)
        }

        if (state.warningCount >= MAX_WARNINGS) {
            val newState = state.copy(
                warningCount = state.warningCount + 1,
                lastWarningTimeMillis = currentTimeMillis,
                penaltyEndTimeMillis = currentTimeMillis + MINIMUM_PENALTY_MILLIS
            )
            return EvaluationResult.Escalate(newState)
        }

        // Static 5 minute warning interval for MVP
        val warningIntervalMillis = MINIMUM_PENALTY_MILLIS
        if (currentTimeMillis - state.lastWarningTimeMillis >= warningIntervalMillis) {
            val nextCount = state.warningCount + 1
            val newState = state.copy(
                warningCount = nextCount,
                lastWarningTimeMillis = currentTimeMillis,
                penaltyEndTimeMillis = currentTimeMillis + MINIMUM_PENALTY_MILLIS
            )
            return if (newState.warningCount >= MAX_WARNINGS) {
                EvaluationResult.Escalate(newState)
            } else {
                EvaluationResult.ShowWarning(newState)
            }
        }

        return EvaluationResult.NoAction
    }
}
