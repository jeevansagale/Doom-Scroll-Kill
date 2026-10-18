package com.example.doomscroller.domain

import com.example.doomscroller.data.ReminderSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderEscalationEvaluatorTest {

    private val evaluator = ReminderEscalationEvaluator()

    @Test
    fun `scenario 1 - monitored app used for X minutes triggers first reminder`() {
        val packageName = "com.instagram.android"
        val session = ReminderSession(packageName = packageName)
        val intervalMinutes = 5
        val ignoredLimit = 3
        val now = 1000000L

        // Usage less than X minutes -> NoAction
        val (sessionUnder, decisionUnder) = evaluator.evaluate(
            session = session,
            isForeground = true,
            currentUsageMillis = 4 * 60 * 1000L,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = now
        )
        assertTrue(decisionUnder is EscalationDecision.NoAction)
        assertEquals(0, sessionUnder.reminderCount)

        // Usage reaches X minutes -> SendReminder #1
        val (sessionAtX, decisionAtX) = evaluator.evaluate(
            session = sessionUnder,
            isForeground = true,
            currentUsageMillis = 5 * 60 * 1000L,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = now
        )
        assertTrue("Expected SendReminder", decisionAtX is EscalationDecision.SendReminder)
        val reminder = decisionAtX as EscalationDecision.SendReminder
        assertEquals(1, reminder.reminderNumber)
        assertEquals(0, reminder.ignoredCount)
        assertEquals(3, reminder.limitCount)
        assertEquals(5, reminder.intervalMinutes)
        assertEquals(1, sessionAtX.reminderCount)
        assertEquals(now, sessionAtX.lastReminderTimeMillis)
        assertFalse(sessionAtX.isEnforcementActive)
    }

    @Test
    fun `scenario 2 - user continues using app and subsequent reminders follow configured interval`() {
        val packageName = "com.instagram.android"
        val intervalMinutes = 5
        val ignoredLimit = 3
        val startTime = 1000000L

        // Reminder #1 sent at startTime
        var session = ReminderSession(
            packageName = packageName,
            qualifyingUsageMillis = 5 * 60 * 1000L,
            reminderCount = 1,
            ignoredCount = 0,
            lastReminderTimeMillis = startTime,
            isAcknowledged = false
        )

        // Only 3 minutes elapsed -> NoAction
        val (_, midDecision) = evaluator.evaluate(
            session = session,
            isForeground = true,
            currentUsageMillis = 8 * 60 * 1000L,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = startTime + 3 * 60 * 1000L
        )
        assertTrue(midDecision is EscalationDecision.NoAction)

        // Exactly 5 minutes elapsed without acknowledgement -> ignoredCount increments to 1, SendReminder #2
        val (session2, decision2) = evaluator.evaluate(
            session = session,
            isForeground = true,
            currentUsageMillis = 10 * 60 * 1000L,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = startTime + 5 * 60 * 1000L
        )
        assertTrue(decision2 is EscalationDecision.SendReminder)
        val reminder2 = decision2 as EscalationDecision.SendReminder
        assertEquals(2, reminder2.reminderNumber)
        assertEquals(1, reminder2.ignoredCount)
        assertEquals(1, session2.ignoredCount)
        assertEquals(2, session2.reminderCount)
    }

    @Test
    fun `scenario 3 - user ignores Z reminders and escalation occurs exactly once`() {
        val packageName = "com.instagram.android"
        val intervalMinutes = 5
        val ignoredLimit = 3
        var currentTime = 1000000L

        // Start session
        var session = ReminderSession(packageName = packageName)

        // 1. Reaches 5 min -> Reminder #1 (ignored = 0)
        val (s1, d1) = evaluator.evaluate(
            session = session,
            isForeground = true,
            currentUsageMillis = 5 * 60 * 1000L,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = currentTime
        )
        assertTrue(d1 is EscalationDecision.SendReminder)
        session = s1

        // 2. Continues 5 min without acknowledging -> Reminder #2 (ignored = 1)
        currentTime += 5 * 60 * 1000L
        val (s2, d2) = evaluator.evaluate(
            session = session,
            isForeground = true,
            currentUsageMillis = 10 * 60 * 1000L,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = currentTime
        )
        assertTrue(d2 is EscalationDecision.SendReminder)
        assertEquals(1, (d2 as EscalationDecision.SendReminder).ignoredCount)
        session = s2

        // 3. Continues another 5 min without acknowledging -> Reminder #3 (ignored = 2)
        currentTime += 5 * 60 * 1000L
        val (s3, d3) = evaluator.evaluate(
            session = session,
            isForeground = true,
            currentUsageMillis = 15 * 60 * 1000L,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = currentTime
        )
        assertTrue(d3 is EscalationDecision.SendReminder)
        assertEquals(2, (d3 as EscalationDecision.SendReminder).ignoredCount)
        session = s3

        // 4. Continues another 5 min without acknowledging -> ignored reaches Z (3) -> TriggerEnforcement!
        currentTime += 5 * 60 * 1000L
        val (s4, d4) = evaluator.evaluate(
            session = session,
            isForeground = true,
            currentUsageMillis = 20 * 60 * 1000L,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = currentTime
        )
        assertTrue("Expected TriggerEnforcement at limit Z=3", d4 is EscalationDecision.TriggerEnforcement)
        val enforcement = d4 as EscalationDecision.TriggerEnforcement
        assertEquals(3, enforcement.ignoredCount)
        assertEquals(currentTime + 5 * 60 * 1000L, enforcement.penaltyEndTimeMillis)
        assertTrue(s4.isEnforcementActive)
        assertEquals(3, s4.ignoredCount)

        // 5. Subsequent evaluations during penalty cooldown do NOT trigger duplicate enforcement
        val (s5, d5) = evaluator.evaluate(
            session = s4,
            isForeground = true,
            currentUsageMillis = 22 * 60 * 1000L,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = currentTime + 2 * 60 * 1000L
        )
        assertTrue("Must not trigger duplicate enforcement", d5 is EscalationDecision.NoAction)
        assertTrue(s5.isEnforcementActive)
    }

    @Test
    fun `scenario 4 - switching to an unmonitored app pauses timer and does not increment ignored count`() {
        val packageName = "com.instagram.android"
        val intervalMinutes = 5
        val ignoredLimit = 3
        val startTime = 1000000L

        // Reminder #1 delivered
        val session = ReminderSession(
            packageName = packageName,
            qualifyingUsageMillis = 5 * 60 * 1000L,
            reminderCount = 1,
            ignoredCount = 0,
            lastReminderTimeMillis = startTime,
            isAcknowledged = false
        )

        // User switches to unmonitored app (e.g., Settings/Calculator) -> isForeground = false
        // Even after 10 minutes pass in unmonitored app:
        val (pausedSession, decision) = evaluator.evaluate(
            session = session,
            isForeground = false,
            currentUsageMillis = 5 * 60 * 1000L, // no usage increase in monitored app
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = startTime + 10 * 60 * 1000L
        )

        assertTrue("Decision must be NoAction when backgrounded", decision is EscalationDecision.NoAction)
        assertEquals("Ignored count must NOT increment while in another app", 0, pausedSession.ignoredCount)
        assertEquals(1, pausedSession.reminderCount)
        assertFalse(pausedSession.isEnforcementActive)
    }

    @Test
    fun `scenario 5 - switching between monitored apps maintains independent session counters`() {
        val intervalMinutes = 5
        val ignoredLimit = 3
        val startTime = 1000000L

        // App A (Instagram): 1 reminder already sent, 0 ignored
        var sessionA = ReminderSession(
            packageName = "com.instagram.android",
            qualifyingUsageMillis = 5 * 60 * 1000L,
            reminderCount = 1,
            ignoredCount = 0,
            lastReminderTimeMillis = startTime
        )

        // App B (TikTok): fresh session, 0 reminders sent
        var sessionB = ReminderSession(packageName = "com.zhiliaoapp.musically")

        // User switches to App B: App B is in foreground, App A is in background
        val (updatedA, decisionA) = evaluator.evaluate(
            session = sessionA,
            isForeground = false,
            currentUsageMillis = 5 * 60 * 1000L,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = startTime + 6 * 60 * 1000L
        )
        assertTrue(decisionA is EscalationDecision.NoAction)
        assertEquals(0, updatedA.ignoredCount)

        // App B has only 2 minutes of use -> NoAction
        val (updatedB, decisionB) = evaluator.evaluate(
            session = sessionB,
            isForeground = true,
            currentUsageMillis = 2 * 60 * 1000L,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = startTime + 6 * 60 * 1000L
        )
        assertTrue(decisionB is EscalationDecision.NoAction)
        assertEquals(0, updatedB.reminderCount)
        assertEquals(0, updatedB.ignoredCount)
    }

    @Test
    fun `scenario 6 - user acknowledges reminder resets consecutive ignored count and resets interval timer`() {
        val packageName = "com.instagram.android"
        val now = 1000000L

        val sessionWithIgnored = ReminderSession(
            packageName = packageName,
            qualifyingUsageMillis = 15 * 60 * 1000L,
            reminderCount = 3,
            ignoredCount = 2,
            lastReminderTimeMillis = now - 4 * 60 * 1000L,
            isAcknowledged = false
        )

        val acknowledgedSession = evaluator.acknowledge(sessionWithIgnored, now = now)

        assertEquals("Ignored count must reset to 0 upon acknowledgement", 0, acknowledgedSession.ignoredCount)
        assertTrue(acknowledgedSession.isAcknowledged)
        assertEquals(now, acknowledgedSession.lastReminderTimeMillis)
        assertEquals(3, acknowledgedSession.reminderCount) // Total reminders sent remains for history
    }

    @Test
    fun `scenario 7 - penalty cooldown expiration requires learning gate completion to release restriction`() {
        val packageName = "com.instagram.android"
        val penaltyStart = 1000000L
        val penaltyEnd = penaltyStart + 5 * 60 * 1000L

        // Penalty session where learning gate is NOT yet completed
        val penalizedSessionIncomplete = ReminderSession(
            packageName = packageName,
            qualifyingUsageMillis = 20 * 60 * 1000L,
            reminderCount = 4,
            ignoredCount = 3,
            lastReminderTimeMillis = penaltyStart,
            isEnforcementActive = true,
            enforcementStartTimeMillis = penaltyStart,
            enforcementEndTimeMillis = penaltyEnd,
            currentState = com.example.doomscroller.data.EnforcementState.LEARNING_GATE_REQUIRED,
            hasEnforcedThisEpisode = true,
            isLearningGateCompleted = false
        )

        // 1 second after penalty expires: learning gate is NOT completed -> restriction remains ACTIVE
        val (sessionStillActive, decisionStillActive) = evaluator.evaluate(
            session = penalizedSessionIncomplete,
            isForeground = true,
            currentUsageMillis = 20 * 60 * 1000L,
            intervalMinutes = 5,
            ignoredLimit = 3,
            now = penaltyEnd + 1000L
        )
        assertTrue("Decision must be NoAction because learning gate is incomplete", decisionStillActive is EscalationDecision.NoAction)
        assertTrue("Enforcement must remain active", sessionStillActive.isEnforcementActive)

        // Now user completes learning gate
        val penalizedSessionCompleted = penalizedSessionIncomplete.copy(
            isLearningGateCompleted = true,
            answeredQuestions = listOf("q1", "q2")
        )

        // Both conditions met: cooldown elapsed AND learning gate completed -> restriction released!
        val (resetSession, decisionReleased) = evaluator.evaluate(
            session = penalizedSessionCompleted,
            isForeground = true,
            currentUsageMillis = 20 * 60 * 1000L,
            intervalMinutes = 5,
            ignoredLimit = 3,
            now = penaltyEnd + 1000L
        )

        assertTrue("Decision must be PenaltyExpired upon dual condition completion", decisionReleased is EscalationDecision.PenaltyExpired)
        assertEquals(packageName, (decisionReleased as EscalationDecision.PenaltyExpired).packageName)
        assertFalse("Enforcement must be inactive after release", resetSession.isEnforcementActive)
        assertEquals(0, resetSession.ignoredCount)
        assertEquals(0, resetSession.reminderCount)
        assertEquals(com.example.doomscroller.data.EnforcementState.MONITORING, resetSession.currentState)
        assertFalse(resetSession.hasEnforcedThisEpisode)
        assertEquals(20 * 60 * 1000L, resetSession.qualifyingUsageMillis) // baseline updated
    }

    @Test
    fun `scenario 8 - complete learning gate lifecycle and dual condition release`() {
        val packageName = "com.instagram.android"
        val intervalMinutes = 5
        val intervalMillis = 5 * 60 * 1000L
        val ignoredLimit = 2
        var now = 1_000_000L

        // 1. Initial State: MONITORING
        var session = ReminderSession(packageName = packageName, currentState = com.example.doomscroller.data.EnforcementState.MONITORING)
        assertEquals(com.example.doomscroller.data.EnforcementState.MONITORING, session.currentState)

        // 2. Threshold reached -> REMINDER_DUE
        val (sDue, dReminder1) = evaluator.evaluate(
            session = session,
            isForeground = true,
            currentUsageMillis = intervalMillis,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = now
        )
        assertTrue(dReminder1 is EscalationDecision.SendReminder)
        assertEquals(com.example.doomscroller.data.EnforcementState.REMINDER_DUE, sDue.currentState)

        // 3. Notification submitted -> REMINDER_SENT
        val sSent = evaluator.markReminderSent(sDue)
        assertEquals(com.example.doomscroller.data.EnforcementState.REMINDER_SENT, sSent.currentState)

        // 4. Another interval passes without acknowledgement (ignored=1) -> REMINDER_DUE -> REMINDER_SENT
        now += intervalMillis
        val (sDue2, dReminder2) = evaluator.evaluate(
            session = sSent,
            isForeground = true,
            currentUsageMillis = 2 * intervalMillis,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = now
        )
        assertTrue(dReminder2 is EscalationDecision.SendReminder)
        assertEquals(1, (dReminder2 as EscalationDecision.SendReminder).ignoredCount)
        val sSent2 = evaluator.markReminderSent(sDue2)
        assertEquals(com.example.doomscroller.data.EnforcementState.REMINDER_SENT, sSent2.currentState)

        // 5. Another interval passes without acknowledgement (ignored reaches limit 2) -> ENFORCING
        now += intervalMillis
        val (sEnforcing, dEnforce) = evaluator.evaluate(
            session = sSent2,
            isForeground = true,
            currentUsageMillis = 3 * intervalMillis,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = now
        )
        assertTrue(dEnforce is EscalationDecision.TriggerEnforcement)
        assertEquals(com.example.doomscroller.data.EnforcementState.ENFORCING, sEnforcing.currentState)
        assertEquals(2, sEnforcing.learningQuestions.size)
        assertTrue(sEnforcing.activeQuoteId != null)

        // 6. Action dispatched -> LEARNING_GATE_REQUIRED
        val sGateRequired = evaluator.markEnforced(sEnforcing)
        assertEquals(com.example.doomscroller.data.EnforcementState.LEARNING_GATE_REQUIRED, sGateRequired.currentState)
        assertTrue(sGateRequired.hasEnforcedThisEpisode)

        // 7. User answers Question 1 incorrectly -> does NOT advance count
        val q1Id = sGateRequired.learningQuestions[0]
        val (sAfterWrong, wrongResult) = evaluator.submitAnswer(sGateRequired, q1Id, "Wrong Option Definitely")
        assertFalse(wrongResult.isCorrect)
        assertFalse(wrongResult.isCompleted)
        assertEquals(0, wrongResult.correctCount)
        assertEquals(0, sAfterWrong.answeredQuestions.size)

        // 8. User answers Question 1 correctly -> advances to 1/2
        val q1 = com.example.doomscroller.domain.learning.QuestionBank.getQuestionById(q1Id)!!
        val (sAfterQ1, q1Result) = evaluator.submitAnswer(sAfterWrong, q1Id, q1.correctAnswer)
        assertTrue(q1Result.isCorrect)
        assertFalse(q1Result.isCompleted)
        assertEquals(1, q1Result.correctCount)
        assertEquals(1, sAfterQ1.answeredQuestions.size)
        assertEquals(com.example.doomscroller.data.EnforcementState.LEARNING_GATE_IN_PROGRESS, sAfterQ1.currentState)

        // 9. User answers Question 2 correctly -> completes gate!
        val q2Id = sAfterQ1.learningQuestions[1]
        val q2 = com.example.doomscroller.domain.learning.QuestionBank.getQuestionById(q2Id)!!
        val (sAfterQ2, q2Result) = evaluator.submitAnswer(sAfterQ1, q2Id, q2.correctAnswer)
        assertTrue(q2Result.isCorrect)
        assertTrue(q2Result.isCompleted)
        assertEquals(2, q2Result.correctCount)
        assertTrue(sAfterQ2.isLearningGateCompleted)
        assertEquals(com.example.doomscroller.data.EnforcementState.LEARNING_GATE_COMPLETED, sAfterQ2.currentState)

        // 10. While penalty cooldown is STILL active (only 2 mins passed) -> restriction remains ACTIVE
        now += 2 * 60 * 1000L
        val (sMidCooldown, dMid) = evaluator.evaluate(
            session = sAfterQ2,
            isForeground = true,
            currentUsageMillis = 3 * intervalMillis + 60_000L,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = now
        )
        assertTrue(dMid is EscalationDecision.NoAction)
        assertTrue(sMidCooldown.isEnforcementActive)

        // 11. Penalty duration elapses (now >= enforcementEndTimeMillis) AND learning gate is completed -> RESTRICTION_RELEASED -> MONITORING
        now += 4 * 60 * 1000L
        val (sRecovered, dExpired) = evaluator.evaluate(
            session = sMidCooldown,
            isForeground = true,
            currentUsageMillis = 3 * intervalMillis + 120_000L,
            intervalMinutes = intervalMinutes,
            ignoredLimit = ignoredLimit,
            now = now
        )
        assertTrue(dExpired is EscalationDecision.PenaltyExpired)
        assertEquals(com.example.doomscroller.data.EnforcementState.MONITORING, sRecovered.currentState)
        assertFalse(sRecovered.isEnforcementActive)
        assertFalse(sRecovered.hasEnforcedThisEpisode)
        assertEquals(0, sRecovered.ignoredCount)
        assertEquals(0, sRecovered.reminderCount)
        assertEquals(0, sRecovered.answeredQuestions.size)
        assertFalse(sRecovered.isLearningGateCompleted)
    }

    @Test
    fun `scenario 9 - learning question bank and quote engine validation`() {
        // Verify question bank has verified questions
        val allQ = com.example.doomscroller.domain.learning.QuestionBank.allQuestions
        assertTrue("Bank must have at least 20 questions", allQ.size >= 20)
        allQ.forEach { q ->
            assertEquals("Each question must have exactly 4 options", 4, q.options.size)
            assertTrue("Correct answer must be present in options", q.options.contains(q.correctAnswer))
            assertTrue("Explanation must not be blank", q.explanation.isNotBlank())
        }

        // Verify distinct selection
        val selected = com.example.doomscroller.domain.learning.QuestionBank.selectDistinctQuestions(2)
        assertEquals(2, selected.size)
        assertTrue("Selected questions must be distinct", selected[0].id != selected[1].id)

        // Verify motivational quotes
        val allQuotes = com.example.doomscroller.domain.quotes.MotivationalQuoteEngine.allQuotes
        assertTrue("Quote bank must have at least 25 quotes", allQuotes.size >= 25)
        allQuotes.forEach { q ->
            assertTrue("Quote text must not be blank", q.text.isNotBlank())
            assertTrue("Author must not be blank", q.author.isNotBlank())
        }

        // Verify contextual quote resolution
        val igQuote = com.example.doomscroller.domain.quotes.MotivationalQuoteEngine.getQuoteForPackage("com.instagram.android")
        assertTrue(igQuote.text.isNotBlank())
        val ytQuote = com.example.doomscroller.domain.quotes.MotivationalQuoteEngine.getQuoteForPackage("com.google.android.youtube")
        assertTrue(ytQuote.text.isNotBlank())
    }
}
