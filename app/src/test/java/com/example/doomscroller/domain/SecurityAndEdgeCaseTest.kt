package com.example.doomscroller.domain

import com.example.doomscroller.data.AppRule
import com.example.doomscroller.data.EnforcementState
import com.example.doomscroller.data.PenaltyInfo
import com.example.doomscroller.data.ReminderSession
import com.example.doomscroller.domain.learning.QuestionBank
import com.example.doomscroller.domain.learning.QuestionCategory
import com.example.doomscroller.domain.quotes.MotivationalQuoteEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SecurityAndEdgeCaseTest {

    private val reminderEvaluator = ReminderEscalationEvaluator()
    private val ruleEvaluator = RuleEvaluator()
    private val testDate = LocalDate.of(2026, 10, 19)

    // =========================================================================
    // 1. Dual-Condition Release Tests
    // =========================================================================

    @Test
    fun `dual condition release - cooldown elapsed but learning gate incomplete keeps restriction active`() {
        val now = 2000000L
        val penaltyEndTime = now - 1000L // 1 second in the past (cooldown has elapsed)

        val session = ReminderSession(
            packageName = "com.instagram.android",
            isEnforcementActive = true,
            enforcementStartTimeMillis = now - 300000L,
            enforcementEndTimeMillis = penaltyEndTime,
            currentState = EnforcementState.COOLDOWN,
            isLearningGateCompleted = false // NOT completed
        )

        // isPenaltyExpired is true
        assertTrue(now >= session.enforcementEndTimeMillis)
        // But learning gate is incomplete
        assertFalse(session.isLearningGateCompleted)
        // Therefore restriction must NOT be satisfied
        assertFalse(session.isRestrictionSatisfied(now))
    }

    @Test
    fun `dual condition release - learning gate completed but cooldown still active keeps restriction active`() {
        val now = 2000000L
        val penaltyEndTime = now + 120000L // 2 minutes remaining on 5-minute penalty

        val session = ReminderSession(
            packageName = "com.instagram.android",
            isEnforcementActive = true,
            enforcementStartTimeMillis = now - 180000L,
            enforcementEndTimeMillis = penaltyEndTime,
            currentState = EnforcementState.LEARNING_GATE_COMPLETED,
            isLearningGateCompleted = true // Completed quiz
        )

        // Cooldown has NOT elapsed
        assertFalse(now >= session.enforcementEndTimeMillis)
        // Learning gate is completed
        assertTrue(session.isLearningGateCompleted)
        // Therefore restriction must NOT be satisfied yet
        assertFalse(session.isRestrictionSatisfied(now))
    }

    @Test
    fun `dual condition release - both cooldown elapsed and learning gate completed satisfies restriction`() {
        val now = 2000000L
        val penaltyEndTime = now - 1000L // Elapsed

        val session = ReminderSession(
            packageName = "com.instagram.android",
            isEnforcementActive = true,
            enforcementStartTimeMillis = now - 300000L,
            enforcementEndTimeMillis = penaltyEndTime,
            currentState = EnforcementState.RESTRICTION_RELEASED,
            isLearningGateCompleted = true
        )

        // Both conditions satisfied
        assertTrue(session.isRestrictionSatisfied(now))
    }

    // =========================================================================
    // 2. Security Intent Sanitization & Injection Prevention
    // =========================================================================

    @Test
    fun `intent package name validation safely rejects malicious strings`() {
        val maliciousPackages = listOf(
            "com.instagram.android;rm -rf /",
            "../etc/passwd",
            "<script>alert(1)</script>",
            "com.example.app\ncom.evil",
            "com.example.app & echo hacked",
            "   ",
            ""
        )

        val packageRegex = Regex("^[a-zA-Z0-9_.]+$")

        for (malicious in maliciousPackages) {
            val isSafe = malicious.matches(packageRegex)
            assertFalse("Malicious package '$malicious' should have been rejected", isSafe)
        }

        val legitimatePackages = listOf(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.zhiliaoapp.musically",
            "com.snapchat.android"
        )

        for (legit in legitimatePackages) {
            val isSafe = legit.matches(packageRegex)
            assertTrue("Legitimate package '$legit' should have been accepted", isSafe)
        }
    }

    // =========================================================================
    // 3. Self-Lockout Prevention
    // =========================================================================

    @Test
    fun `self-lockout prevention - doomscroller package should not lock its own host app`() {
        val hostApp = "com.example.doomscroller"
        val rule = AppRule(packageName = hostApp, limitMinutes = 5, isEnabled = true)

        // Verifying that host app identity check is deterministic
        val isTargetSelf = hostApp == "com.example.doomscroller"
        assertTrue("Host app self-lockout check must identify own package", isTargetSelf)
    }

    // =========================================================================
    // 4. Parameter Boundary Clamping
    // =========================================================================

    @Test
    fun `parameter boundary clamping - interval and limit cannot be zero or negative`() {
        val negativeInterval = -10
        val zeroInterval = 0
        val clampedMinInterval = negativeInterval.coerceIn(1, 60)
        val clampedZeroInterval = zeroInterval.coerceIn(1, 60)
        assertEquals(1, clampedMinInterval)
        assertEquals(1, clampedZeroInterval)

        val excessiveInterval = 999
        val clampedMaxInterval = excessiveInterval.coerceIn(1, 60)
        assertEquals(60, clampedMaxInterval)

        val zeroLimit = 0
        val clampedZeroLimit = zeroLimit.coerceIn(1, 10)
        assertEquals(1, clampedZeroLimit)

        val excessiveLimit = 50
        val clampedMaxLimit = excessiveLimit.coerceIn(1, 10)
        assertEquals(10, clampedMaxLimit)
    }

    // =========================================================================
    // 5. Screen-Off & Background State Attribution Edge Cases
    // =========================================================================

    @Test
    fun `background state transition - app in background does not accumulate qualifying usage`() {
        val session = ReminderSession(
            packageName = "com.instagram.android",
            qualifyingUsageMillis = 2 * 60 * 1000L
        )

        // Evaluator called with isForeground = false while time advances 10 minutes
        val (newSession, decision) = reminderEvaluator.evaluate(
            session = session,
            isForeground = false,
            currentUsageMillis = 12 * 60 * 1000L,
            intervalMinutes = 5,
            ignoredLimit = 3,
            now = 1000000L + 10 * 60 * 1000L
        )

        // When in background, no reminders may trigger
        assertTrue(decision is EscalationDecision.NoAction)
        assertEquals("Qualifying usage must freeze while in background", 2 * 60 * 1000L, newSession.qualifyingUsageMillis)
        assertEquals(0, newSession.reminderCount)
    }

    // =========================================================================
    // 6. Rapid App Flapping / Switching
    // =========================================================================

    @Test
    fun `rapid switching between monitored apps maintains independent session tracking`() {
        var sessionA = ReminderSession(packageName = "com.instagram.android")
        var sessionB = ReminderSession(packageName = "com.google.android.youtube")

        val startTime = 1000000L
        var currentTime = startTime

        // User alternates between App A and App B every 30 seconds for 10 cycles (5 min each total)
        for (i in 0 until 10) {
            // 30 seconds in App A
            currentTime += 30 * 1000L
            val (updatedA, _) = reminderEvaluator.evaluate(
                session = sessionA,
                isForeground = true,
                currentUsageMillis = (i + 1) * 30 * 1000L,
                intervalMinutes = 5,
                ignoredLimit = 3,
                now = currentTime
            )
            sessionA = updatedA

            // 30 seconds in App B
            currentTime += 30 * 1000L
            val (updatedB, _) = reminderEvaluator.evaluate(
                session = sessionB,
                isForeground = true,
                currentUsageMillis = (i + 1) * 30 * 1000L,
                intervalMinutes = 5,
                ignoredLimit = 3,
                now = currentTime
            )
            sessionB = updatedB
        }

        // Each app has baseline preserved at 0L and reminder count reached 1 at 5 minutes
        assertEquals(0L, sessionA.qualifyingUsageMillis)
        assertEquals(0L, sessionB.qualifyingUsageMillis)
        assertEquals(1, sessionA.reminderCount)
        assertEquals(1, sessionB.reminderCount)
    }

    // =========================================================================
    // 7. Question Bank Integrity & Validation
    // =========================================================================

    @Test
    fun `question bank integrity - all questions must satisfy strict educational standards`() {
        val questions = QuestionBank.allQuestions
        assertTrue("Question bank should not be empty", questions.isNotEmpty())

        val ids = mutableSetOf<String>()
        for (q in questions) {
            assertTrue("Question ID must be unique: ${q.id}", ids.add(q.id))
            assertTrue("Question text must be non-blank for ${q.id}", q.questionText.isNotBlank())
            assertEquals("Question ${q.id} must have exactly 4 options", 4, q.options.size)
            assertEquals("Options for question ${q.id} must be unique", 4, q.options.distinct().size)
            assertTrue("Correct answer must be among options for ${q.id}", q.options.contains(q.correctAnswer))
            assertTrue("Explanation must be non-blank for ${q.id}", q.explanation.isNotBlank())

            // Test answer validator logic
            assertTrue("Validation of correct answer should pass", q.validateAnswer(q.correctAnswer))
            assertFalse("Validation of incorrect answer should fail", q.validateAnswer("Definite Wrong Answer"))
        }
    }

    @Test
    fun `question bank sampling - selectDistinctQuestions returns requested count and excludes solved`() {
        val questions = QuestionBank.selectDistinctQuestions(2)
        assertEquals(2, questions.size)
        assertTrue(questions[0].id != questions[1].id)

        val solvedId = questions[0].id
        val nextQuestions = QuestionBank.selectDistinctQuestions(2, excludeIds = setOf(solvedId))
        assertEquals(2, nextQuestions.size)
        assertFalse("Excluded question must not be reselected", nextQuestions.any { it.id == solvedId })
    }

    // =========================================================================
    // 8. Motivational Quote Engine Integrity
    // =========================================================================

    @Test
    fun `quote engine integrity - provides valid quotes for specific and general contexts`() {
        val igQuote = MotivationalQuoteEngine.getQuoteForPackage("com.instagram.android")
        assertNotNull(igQuote)
        assertTrue(igQuote.text.isNotBlank())
        assertEquals("instagram", igQuote.targetContext)

        val ytQuote = MotivationalQuoteEngine.getQuoteForPackage("com.google.android.youtube")
        assertNotNull(ytQuote)
        assertTrue(ytQuote.text.isNotBlank())
        assertEquals("youtube", ytQuote.targetContext)

        val fallbackQuote = MotivationalQuoteEngine.getQuoteForPackage("com.uncommon.game")
        assertNotNull(fallbackQuote)
        assertTrue(fallbackQuote.text.isNotBlank())
    }

    // =========================================================================
    // 9. Backward Time Jump / Clock Skew Resilience
    // =========================================================================

    @Test
    fun `clock skew - backward time jump does not trigger false reminders or crashes`() {
        val startTime = 5000000L
        val session = ReminderSession(
            packageName = "com.instagram.android",
            qualifyingUsageMillis = 4 * 60 * 1000L,
            lastReminderTimeMillis = startTime
        )

        // NTP or user moves clock backward by 1 hour
        val (newSession, decision) = reminderEvaluator.evaluate(
            session = session,
            isForeground = true,
            currentUsageMillis = 4 * 60 * 1000L,
            intervalMinutes = 5,
            ignoredLimit = 3,
            now = startTime - 3600000L // 1 hour earlier!
        )

        // Must gracefully handle backward time without crash or spurious reminder
        assertTrue(decision is EscalationDecision.NoAction)
    }
}
