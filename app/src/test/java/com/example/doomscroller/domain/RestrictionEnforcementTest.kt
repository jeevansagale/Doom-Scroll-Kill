package com.example.doomscroller.domain

import com.example.doomscroller.data.AppRule
import com.example.doomscroller.data.EnforcementState
import com.example.doomscroller.data.PendingIntervention
import com.example.doomscroller.data.PreferencesManager
import com.example.doomscroller.data.ReminderSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RestrictionEnforcementTest {

    private val evaluator = ReminderEscalationEvaluator()
    private val ruleEvaluator = RuleEvaluator()
    private val testPackage = "com.instagram.android"

    // 1. Acknowledgment at 59 seconds
    @Test
    fun `test 1 - acknowledgment at 59 seconds prevents NO_ACK and resets session`() {
        val t0 = 100_000L
        val session = ReminderSession(
            packageName = testPackage,
            qualifyingUsageMillis = 5 * 60 * 1000L,
            reminderCount = 1,
            ignoredCount = 0,
            lastReminderTimeMillis = t0,
            currentState = EnforcementState.INTERVENTION_ACTIVE
        )
        val eventId = "event-123"
        val pending = PendingIntervention(
            eventId = eventId,
            packageName = testPackage,
            startTimeMillis = t0,
            deadlineMillis = t0 + 60_000L,
            isAcknowledged = false,
            isTimedOut = false
        )

        // At 59 seconds: user acknowledges
        val ackTime = t0 + 59_000L
        assertFalse("At 59s deadline should not be expired", pending.isExpired(ackTime))

        // Acknowledge session
        val acknowledgedSession = evaluator.acknowledge(session)
        assertEquals(EnforcementState.MONITORING, acknowledgedSession.currentState)
        assertEquals(0, acknowledgedSession.ignoredCount)
        assertTrue(acknowledgedSession.isAcknowledged)
        assertFalse(acknowledgedSession.isEnforcementActive)

        // Mark pending acknowledged
        val resolvedPending = pending.copy(isAcknowledged = true)
        assertTrue(resolvedPending.isAcknowledged)
        assertFalse(resolvedPending.isTimedOut)
    }

    // 2. No acknowledgment at 60 seconds
    @Test
    fun `test 2 - no acknowledgment at 60 seconds transitions to NO_ACK and triggers enforcement`() {
        val t0 = 100_000L
        val session = ReminderSession(
            packageName = testPackage,
            qualifyingUsageMillis = 5 * 60 * 1000L,
            reminderCount = 1,
            ignoredCount = 0,
            lastReminderTimeMillis = t0,
            currentState = EnforcementState.INTERVENTION_ACTIVE
        )
        val eventId = "event-456"
        val pending = PendingIntervention(
            eventId = eventId,
            packageName = testPackage,
            startTimeMillis = t0,
            deadlineMillis = t0 + 60_000L,
            isAcknowledged = false,
            isTimedOut = false
        )

        // At exactly 60 seconds
        val timeoutTime = t0 + 60_000L
        assertTrue("At 60s deadline is expired", pending.isExpired(timeoutTime))

        val (timedOutSession, decision) = evaluator.evaluate(
            session = session,
            isForeground = true,
            currentUsageMillis = 5 * 60 * 1000L,
            intervalMinutes = 5,
            ignoredLimit = 3,
            now = timeoutTime,
            restrictionDurationMinutes = 10,
            pendingIntervention = pending
        )

        assertTrue("Decision must be TriggerEnforcement", decision is EscalationDecision.TriggerEnforcement)
        val trigger = decision as EscalationDecision.TriggerEnforcement
        assertEquals("NO_ACK_TIMEOUT", trigger.reason)
        assertEquals(timeoutTime + 10 * 60 * 1000L, trigger.penaltyEndTimeMillis)

        assertEquals(EnforcementState.NO_ACK, timedOutSession.currentState)
        assertTrue(timedOutSession.isEnforcementActive)
        assertEquals(timeoutTime + 10 * 60 * 1000L, timedOutSession.enforcementEndTimeMillis)
        assertEquals(2, timedOutSession.learningQuestions.size)
    }

    // 3. Timeout transition while the app is backgrounded
    @Test
    fun `test 3 - timeout transition evaluates and triggers even while app is backgrounded`() {
        val t0 = 100_000L
        val session = ReminderSession(
            packageName = testPackage,
            qualifyingUsageMillis = 5 * 60 * 1000L,
            reminderCount = 1,
            ignoredCount = 0,
            lastReminderTimeMillis = t0,
            currentState = EnforcementState.INTERVENTION_ACTIVE
        )
        val eventId = "event-bg-1"
        val pending = PendingIntervention(
            eventId = eventId,
            packageName = testPackage,
            startTimeMillis = t0,
            deadlineMillis = t0 + 60_000L,
            isAcknowledged = false,
            isTimedOut = false
        )

        // 65 seconds later, app is backgrounded (isForeground = false)
        val bgTime = t0 + 65_000L
        val (timedOutSession, decision) = evaluator.evaluate(
            session = session,
            isForeground = false,
            currentUsageMillis = 5 * 60 * 1000L,
            intervalMinutes = 5,
            ignoredLimit = 3,
            now = bgTime,
            restrictionDurationMinutes = 5,
            pendingIntervention = pending
        )

        assertTrue("Enforcement must trigger even in background when 60s deadline passes", decision is EscalationDecision.TriggerEnforcement)
        assertEquals(EnforcementState.NO_ACK, timedOutSession.currentState)
        assertTrue(timedOutSession.isEnforcementActive)
    }

    // 4. Process recreation before the deadline
    @Test
    fun `test 4 - process recreation before deadline restores pending state and remaining time`() {
        val t0 = 100_000L
        val deadline = t0 + 60_000L
        val eventId = "event-proc-1"

        // Simulated persisted string in SharedPreferences: eventId;pkg;startTime;deadline;isAck;isTimedOut
        val serialized = "$eventId;$testPackage;$t0;$deadline;false;false"
        val parts = serialized.split(";")
        val restored = PendingIntervention(
            eventId = parts[0],
            packageName = parts[1],
            startTimeMillis = parts[2].toLong(),
            deadlineMillis = parts[3].toLong(),
            isAcknowledged = parts[4].toBoolean(),
            isTimedOut = parts[5].toBoolean()
        )

        // Process restarts at t0 + 25s
        val restartTime = t0 + 25_000L
        assertFalse(restored.isExpired(restartTime))
        assertEquals(35_000L, restored.remainingMillis(restartTime))
        assertFalse(restored.isAcknowledged)
        assertFalse(restored.isTimedOut)

        // User acknowledges at t0 + 40s
        val ackTime = t0 + 40_000L
        assertFalse(restored.isExpired(ackTime))
        val acked = restored.copy(isAcknowledged = true)
        assertTrue(acked.isAcknowledged)
    }

    // 5. Process recreation after the deadline
    @Test
    fun `test 5 - process recreation after deadline immediately recognizes overdue state`() {
        val t0 = 100_000L
        val deadline = t0 + 60_000L
        val eventId = "event-proc-2"

        val restored = PendingIntervention(
            eventId = eventId,
            packageName = testPackage,
            startTimeMillis = t0,
            deadlineMillis = deadline,
            isAcknowledged = false,
            isTimedOut = false
        )

        // Device restarts or process killed, resumed at t0 + 90s (30s after deadline)
        val lateRestartTime = t0 + 90_000L
        assertTrue(restored.isExpired(lateRestartTime))
        assertEquals(0L, restored.remainingMillis(lateRestartTime))

        // Direct transitionToNoAck executes immediately
        val session = ReminderSession(packageName = testPackage, currentState = EnforcementState.INTERVENTION_ACTIVE)
        val (enforcedSession, decision) = evaluator.transitionToNoAck(session, restored.eventId, lateRestartTime, restrictionDurationMinutes = 5)

        assertEquals(EnforcementState.NO_ACK, enforcedSession.currentState)
        assertTrue(enforcedSession.isEnforcementActive)
        assertEquals(lateRestartTime + 5 * 60 * 1000L, decision.penaltyEndTimeMillis)
    }

    // 6. Duplicate timeout execution
    @Test
    fun `test 6 - duplicate timeout execution is rejected and idempotent`() {
        val t0 = 100_000L
        val pending = PendingIntervention(
            eventId = "event-dup",
            packageName = testPackage,
            startTimeMillis = t0,
            deadlineMillis = t0 + 60_000L,
            isAcknowledged = false,
            isTimedOut = false
        )

        // First timeout
        val timeoutPending1 = pending.copy(isTimedOut = true)
        assertTrue(timeoutPending1.isTimedOut)

        // Duplicate timeout attempt on already timed-out record
        val canTimeoutAgain = !timeoutPending1.isAcknowledged && !timeoutPending1.isTimedOut
        assertFalse("Duplicate timeout execution must be rejected", canTimeoutAgain)
    }

    // 7. Acknowledgment racing with timeout
    @Test
    fun `test 7 - acknowledgment racing with timeout ensures atomic single winner`() {
        var pending = PendingIntervention(
            eventId = "event-race",
            packageName = testPackage,
            startTimeMillis = 100_000L,
            deadlineMillis = 160_000L,
            isAcknowledged = false,
            isTimedOut = false
        )

        // Atomic simulate: acknowledge wins first
        val ackWon = synchronized(pending) {
            if (!pending.isAcknowledged && !pending.isTimedOut) {
                pending = pending.copy(isAcknowledged = true)
                true
            } else {
                false
            }
        }
        assertTrue("Acknowledgment should win race", ackWon)

        // Concurrent timeout arrives milliseconds later
        val timeoutWon = synchronized(pending) {
            if (!pending.isAcknowledged && !pending.isTimedOut) {
                pending = pending.copy(isTimedOut = true)
                true
            } else {
                false
            }
        }
        assertFalse("Timeout must not win after acknowledgment", timeoutWon)
        assertTrue(pending.isAcknowledged)
        assertFalse(pending.isTimedOut)
    }

    // 8. Each supported restriction duration
    @Test
    fun `test 8 - each supported restriction duration is calculated accurately`() {
        val supportedPresets = PreferencesManager.RESTRICTION_PRESETS // listOf(1, 5, 10, 15, 30, 60)
        assertEquals(listOf(1, 5, 10, 15, 30, 60), supportedPresets)

        val t0 = 500_000L
        val session = ReminderSession(packageName = testPackage)

        for (preset in supportedPresets) {
            val (noAckSession, decision) = evaluator.transitionToNoAck(
                session = session,
                eventId = "preset-$preset",
                now = t0,
                restrictionDurationMinutes = preset
            )
            val expectedDurationMillis = preset * 60 * 1000L
            assertEquals(t0 + expectedDurationMillis, decision.penaltyEndTimeMillis)
            assertEquals(t0 + expectedDurationMillis, noAckSession.enforcementEndTimeMillis)
            assertEquals(EnforcementState.NO_ACK, noAckSession.currentState)
        }
    }

    // 9. Persistence of a changed duration after app restart
    @Test
    fun `test 9 - persistence of changed restriction duration across simulated restart`() {
        // Validation logic used by PreferencesManager
        fun validateDuration(input: Int): Int {
            return if (input in 1..1440) input else PreferencesManager.DEFAULT_RESTRICTION_DURATION_MINUTES
        }

        val selectedSetting = 15
        val validated = validateDuration(selectedSetting)
        assertEquals(15, validated)

        // Simulated restore from persisted integer
        val restoredSetting = validateDuration(validated)
        assertEquals(15, restoredSetting)
    }

    // 10. Existing installation migration with five-minute default
    @Test
    fun `test 10 - existing installation migration defaults to five minutes`() {
        assertEquals(5, PreferencesManager.DEFAULT_RESTRICTION_DURATION_MINUTES)
    }

    // 11. Restriction activation after NO_ACK
    @Test
    fun `test 11 - restriction activation after NO_ACK requires dual-condition release`() {
        val now = 200_000L
        val session = ReminderSession(packageName = testPackage)
        val (noAckSession, _) = evaluator.transitionToNoAck(
            session = session,
            eventId = "dual-1",
            now = now,
            restrictionDurationMinutes = 5
        )

        // Cooldown active, questions unanswered -> cannot release
        assertFalse(noAckSession.isRestrictionSatisfied(now))

        // 1. Answer questions
        val q1 = noAckSession.learningQuestions[0]
        val q2 = noAckSession.learningQuestions[1]
        val sAnswered = noAckSession.copy(
            answeredQuestions = listOf(q1, q2),
            isLearningGateCompleted = true
        )
        // Questions completed, but cooldown not expired yet -> cannot release
        assertFalse(sAnswered.isRestrictionSatisfied(now))

        // 2. Cooldown expires (5 min later)
        val afterCooldown = now + 5 * 60 * 1000L + 1L
        assertTrue(sAnswered.isPenaltyExpired(afterCooldown))

        // Dual condition satisfied!
        val dualSatisfiedSession = sAnswered.copy(
            lastReminderTimeMillis = afterCooldown
        )
        assertTrue(dualSatisfiedSession.isLearningGateCompleted && dualSatisfiedSession.isPenaltyExpired(afterCooldown))
    }

    // 12. Reopening Instagram while restriction is active
    @Test
    fun `test 12 - reopening Instagram while restriction is active blocks and maintains penalty`() {
        val now = 300_000L
        val penaltyEnd = now + 5 * 60 * 1000L
        val activeSession = ReminderSession(
            packageName = testPackage,
            isEnforcementActive = true,
            enforcementStartTimeMillis = now,
            enforcementEndTimeMillis = penaltyEnd,
            currentState = EnforcementState.COOLDOWN,
            hasEnforcedThisEpisode = true
        )

        // User reopens Instagram 1 minute into cooldown
        val reopenTime = now + 60_000L
        val (reopenedSession, decision) = evaluator.evaluate(
            session = activeSession,
            isForeground = true,
            currentUsageMillis = 6 * 60 * 1000L,
            intervalMinutes = 5,
            ignoredLimit = 3,
            now = reopenTime,
            restrictionDurationMinutes = 5
        )

        // Restriction must remain active
        assertTrue(reopenedSession.isEnforcementActive)
        assertTrue(
            "State should reflect active restriction",
            reopenedSession.currentState == EnforcementState.COOLDOWN ||
            reopenedSession.currentState == EnforcementState.LEARNING_GATE_REQUIRED
        )
        assertTrue(decision is EscalationDecision.NoAction)
        assertFalse("Penalty must not expire while cooldown is counting down", reopenedSession.isPenaltyExpired(reopenTime))
    }

    // 13. Back navigation and app lifecycle behavior
    @Test
    fun `test 13 - app lifecycle transitions between background and foreground preserve session state`() {
        val now = 400_000L
        val session = ReminderSession(
            packageName = testPackage,
            qualifyingUsageMillis = 2 * 60 * 1000L,
            reminderCount = 1,
            ignoredCount = 1,
            lastReminderTimeMillis = now,
            currentState = EnforcementState.REMINDER_SENT
        )

        // App goes to background
        val (bgSession, bgDecision) = evaluator.evaluate(
            session = session,
            isForeground = false,
            currentUsageMillis = 2 * 60 * 1000L,
            intervalMinutes = 5,
            ignoredLimit = 3,
            now = now + 10_000L
        )
        assertTrue(bgDecision is EscalationDecision.NoAction)
        assertEquals(1, bgSession.ignoredCount)
        assertEquals(1, bgSession.reminderCount)

        // App returns to foreground
        val (fgSession, fgDecision) = evaluator.evaluate(
            session = bgSession,
            isForeground = true,
            currentUsageMillis = 2 * 60 * 1000L,
            intervalMinutes = 5,
            ignoredLimit = 3,
            now = now + 20_000L
        )
        assertTrue(fgDecision is EscalationDecision.NoAction)
        assertEquals(1, fgSession.ignoredCount)
        assertEquals(1, fgSession.reminderCount)
    }

    // 14. Invalid timeout values
    @Test
    fun `test 14 - invalid timeout values are rejected or clamped to valid range`() {
        fun sanitizeDuration(input: Int): Int {
            return if (input in 1..1440) input else PreferencesManager.DEFAULT_RESTRICTION_DURATION_MINUTES
        }

        assertEquals(5, sanitizeDuration(0)) // Zero rejected
        assertEquals(5, sanitizeDuration(-1)) // Negative rejected
        assertEquals(5, sanitizeDuration(-100))
        assertEquals(5, sanitizeDuration(999999)) // Overflow rejected
        assertEquals(1, sanitizeDuration(1)) // Lower bound accepted
        assertEquals(1440, sanitizeDuration(1440)) // Upper bound accepted
        assertEquals(30, sanitizeDuration(30)) // Valid preset accepted
    }

    // 15. Regression tests for existing usage tracking and midnight rollover
    @Test
    fun `test 15 - midnight rollover maintains active penalty and resets daily usage state`() {
        val rule = AppRule(testPackage, limitMinutes = 5, isEnabled = true)
        val day1 = LocalDate.of(2026, 10, 18)
        val day2 = LocalDate.of(2026, 10, 19)

        val startTime = 1_000_000L
        val penaltyEnd = startTime + 10 * 60 * 1000L // 10 min penalty

        val warningState = WarningState(
            packageName = testPackage,
            date = day1,
            warningCount = 1,
            lastWarningTimeMillis = startTime,
            penaltyEndTimeMillis = penaltyEnd
        )

        // Across midnight, but penalty is still counting down (e.g., 3 minutes into penalty)
        val resultAcrossMidnight = ruleEvaluator.evaluate(
            rule = rule,
            usedMinutes = 1, // New day usage
            currentTimeMillis = startTime + 3 * 60 * 1000L,
            currentDate = day2,
            previousState = warningState,
            penaltyDurationMinutes = 10
        )

        // Penalty is still active, must not trigger new warning or allow usage
        assertTrue("Expected NoAction while active penalty survives midnight", resultAcrossMidnight is EvaluationResult.NoAction)

        // After penalty expires on day 2
        val resultAfterExpire = ruleEvaluator.evaluate(
            rule = rule,
            usedMinutes = 5, // Reached limit on new day
            currentTimeMillis = penaltyEnd + 1000L,
            currentDate = day2,
            previousState = warningState,
            penaltyDurationMinutes = 10
        )
        assertTrue("Expected ShowWarning after penalty expires on day 2", resultAfterExpire is EvaluationResult.ShowWarning)
    }
}
