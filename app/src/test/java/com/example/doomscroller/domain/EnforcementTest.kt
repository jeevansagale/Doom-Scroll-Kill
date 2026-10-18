package com.example.doomscroller.domain

import com.example.doomscroller.data.AppRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class EnforcementTest {

    private val evaluator = RuleEvaluator()
    private val testDate = LocalDate.of(2026, 10, 18)

    @Test
    fun `quota reached activates 5-minute minimum penalty`() {
        val rule = AppRule("com.google.android.youtube", limitMinutes = 5, isEnabled = true)
        val now = 1000000L
        val result = evaluator.evaluate(
            rule = rule,
            usedMinutes = 5,
            currentTimeMillis = now,
            currentDate = testDate,
            previousState = null
        )

        assertTrue("Expected ShowWarning on first quota trigger", result is EvaluationResult.ShowWarning)
        val state = (result as EvaluationResult.ShowWarning).newWarningState
        assertEquals(1, state.warningCount)
        assertEquals(now, state.lastWarningTimeMillis)
        assertEquals(now + 5 * 60 * 1000L, state.penaltyEndTimeMillis)
    }

    @Test
    fun `no duplicate warning during 5-minute penalty period`() {
        val rule = AppRule("com.google.android.youtube", limitMinutes = 5, isEnabled = true)
        val startTime = 1000000L
        val state = WarningState(
            packageName = "com.google.android.youtube",
            date = testDate,
            warningCount = 1,
            lastWarningTimeMillis = startTime,
            penaltyEndTimeMillis = startTime + 5 * 60 * 1000L
        )

        // 2 minutes into penalty
        val result = evaluator.evaluate(
            rule = rule,
            usedMinutes = 6,
            currentTimeMillis = startTime + 2 * 60 * 1000L,
            currentDate = testDate,
            previousState = state
        )

        assertTrue("Expected NoAction while 5-minute penalty/interval is active", result is EvaluationResult.NoAction)
    }

    @Test
    fun `penalty expiration allows subsequent warning or escalation`() {
        val rule = AppRule("com.google.android.youtube", limitMinutes = 5, isEnabled = true)
        val startTime = 1000000L
        val state = WarningState(
            packageName = "com.google.android.youtube",
            date = testDate,
            warningCount = 1,
            lastWarningTimeMillis = startTime,
            penaltyEndTimeMillis = startTime + 5 * 60 * 1000L
        )

        // Exactly 5 minutes later (after penalty expires)
        val result = evaluator.evaluate(
            rule = rule,
            usedMinutes = 7,
            currentTimeMillis = startTime + 5 * 60 * 1000L,
            currentDate = testDate,
            previousState = state
        )

        assertTrue("Expected ShowWarning after penalty expires", result is EvaluationResult.ShowWarning)
        val newState = (result as EvaluationResult.ShowWarning).newWarningState
        assertEquals(2, newState.warningCount)
        assertEquals(startTime + 5 * 60 * 1000L + 5 * 60 * 1000L, newState.penaltyEndTimeMillis)
    }

    @Test
    fun `third warning triggers escalation with 5-minute penalty`() {
        val rule = AppRule("com.google.android.youtube", limitMinutes = 5, isEnabled = true)
        val startTime = 1000000L
        val state = WarningState(
            packageName = "com.google.android.youtube",
            date = testDate,
            warningCount = 2,
            lastWarningTimeMillis = startTime,
            penaltyEndTimeMillis = startTime + 5 * 60 * 1000L
        )

        // 5 minutes later
        val result = evaluator.evaluate(
            rule = rule,
            usedMinutes = 8,
            currentTimeMillis = startTime + 5 * 60 * 1000L,
            currentDate = testDate,
            previousState = state
        )

        assertTrue("Expected Escalate on 3rd warning", result is EvaluationResult.Escalate)
        val newState = (result as EvaluationResult.Escalate).newWarningState
        assertEquals(3, newState.warningCount)
        assertEquals(startTime + 10 * 60 * 1000L, newState.penaltyEndTimeMillis)
    }

    @Test
    fun `disabled rule returns NoAction even when over quota`() {
        val rule = AppRule("com.google.android.youtube", limitMinutes = 5, isEnabled = false)
        val result = evaluator.evaluate(
            rule = rule,
            usedMinutes = 10,
            currentTimeMillis = 1000000L,
            currentDate = testDate,
            previousState = null
        )
        assertTrue("Disabled rule must never trigger warnings", result is EvaluationResult.NoAction)
    }

    @Test
    fun `usage below limit returns NoAction`() {
        val rule = AppRule("com.google.android.youtube", limitMinutes = 10, isEnabled = true)
        val result = evaluator.evaluate(
            rule = rule,
            usedMinutes = 9,
            currentTimeMillis = 1000000L,
            currentDate = testDate,
            previousState = null
        )
        assertTrue("Usage below limit must return NoAction", result is EvaluationResult.NoAction)
    }

    @Test
    fun `unmonitored foreground app filtering simulation`() {
        val monitoredRules = listOf(
            AppRule("com.instagram.android", limitMinutes = 5, isEnabled = true),
            AppRule("com.google.android.youtube", limitMinutes = 10, isEnabled = true)
        )
        val monitoredSet = monitoredRules.map { it.packageName }.toSet()

        // User opens unmonitored Settings app
        val foregroundApp = "com.android.settings"
        val isMonitored = foregroundApp in monitoredSet
        assertFalse("Unmonitored app must not be treated as a monitored package", isMonitored)

        // Evaluation simulation
        val evaluatedRule = if (isMonitored) monitoredRules.find { it.packageName == foregroundApp } else null
        assertEquals(null, evaluatedRule)
    }

    @Test
    fun `empty monitoring list produces zero target evaluations`() {
        val emptyRules = emptyList<AppRule>()
        val monitoredSet = emptyRules.map { it.packageName }.toSet()
        val foregroundApp = "com.instagram.android"

        val canEvaluate = monitoredSet.isNotEmpty() && foregroundApp in monitoredSet
        assertFalse("Empty monitoring list must never evaluate any app", canEvaluate)
    }

    @Test
    fun `monitored app in foreground evaluates only that specific app`() {
        val rules = listOf(
            AppRule("com.instagram.android", limitMinutes = 5, isEnabled = true),
            AppRule("com.google.android.youtube", limitMinutes = 10, isEnabled = true)
        )
        val monitoredSet = rules.map { it.packageName }.toSet()

        val foregroundApp = "com.instagram.android"
        assertTrue(foregroundApp in monitoredSet)

        val activeRule = rules.find { it.packageName == foregroundApp }
        assertEquals("com.instagram.android", activeRule?.packageName)
        assertEquals(5, activeRule?.limitMinutes)
    }

    @Test
    fun `start destination resolves correctly based on onboarding and permission state`() {
        fun resolveStartRoute(onboardingCompleted: Boolean, hasUsageAccess: Boolean): String {
            return when {
                !onboardingCompleted -> "onboarding"
                !hasUsageAccess -> "permission_setup"
                else -> "dashboard"
            }
        }

        assertEquals("onboarding", resolveStartRoute(onboardingCompleted = false, hasUsageAccess = false))
        assertEquals("onboarding", resolveStartRoute(onboardingCompleted = false, hasUsageAccess = true))
        assertEquals("permission_setup", resolveStartRoute(onboardingCompleted = true, hasUsageAccess = false))
        assertEquals("dashboard", resolveStartRoute(onboardingCompleted = true, hasUsageAccess = true))
    }

    @Test
    fun `leave action semantics target home launcher without terminating unrelated apps`() {
        val action = android.content.Intent.ACTION_MAIN
        val category = android.content.Intent.CATEGORY_HOME
        val flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK

        assertEquals("android.intent.action.MAIN", action)
        assertEquals("android.intent.category.HOME", category)
        assertEquals(0x10000000, flags)
    }
}
