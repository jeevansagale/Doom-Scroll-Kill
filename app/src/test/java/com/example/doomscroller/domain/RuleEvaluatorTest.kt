package com.example.doomscroller.domain

import com.example.doomscroller.data.AppRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RuleEvaluatorTest {

    private val evaluator = RuleEvaluator()
    private val defaultDate = LocalDate.of(2026, 10, 18)

    @Test
    fun `below threshold returns NoAction`() {
        val rule = AppRule("com.test", 60, true)
        val result = evaluator.evaluate(rule, 30, 1000L, defaultDate, null)
        assertTrue(result is EvaluationResult.NoAction)
    }

    @Test
    fun `exact threshold returns ShowWarning on first trigger`() {
        val rule = AppRule("com.test", 60, true)
        val result = evaluator.evaluate(rule, 60, 1000L, defaultDate, null)
        assertTrue(result is EvaluationResult.ShowWarning)
        val state = (result as EvaluationResult.ShowWarning).newWarningState
        assertEquals(1, state.warningCount)
        assertEquals(1000L, state.lastWarningTimeMillis)
    }

    @Test
    fun `disabled rule returns NoAction even over threshold`() {
        val rule = AppRule("com.test", 60, false)
        val result = evaluator.evaluate(rule, 70, 1000L, defaultDate, null)
        assertTrue(result is EvaluationResult.NoAction)
    }

    @Test
    fun `warning interval prevents immediate re-trigger`() {
        val rule = AppRule("com.test", 60, true)
        val initialState = WarningState("com.test", defaultDate, 1, 1000L)
        
        // 1 minute later
        val result = evaluator.evaluate(rule, 65, 1000L + 60_000L, defaultDate, initialState)
        assertTrue(result is EvaluationResult.NoAction)
    }

    @Test
    fun `after interval returns ShowWarning or Escalate`() {
        val rule = AppRule("com.test", 60, true)
        val initialState = WarningState("com.test", defaultDate, 1, 1000L)
        
        // 6 minutes later
        val result = evaluator.evaluate(rule, 65, 1000L + 6 * 60 * 1000L, defaultDate, initialState)
        assertTrue(result is EvaluationResult.ShowWarning)
        val state = (result as EvaluationResult.ShowWarning).newWarningState
        assertEquals(2, state.warningCount)
    }

    @Test
    fun `max warnings triggers Escalate`() {
        val rule = AppRule("com.test", 60, true)
        val initialState = WarningState("com.test", defaultDate, 2, 1000L)
        
        // 6 minutes later
        val result = evaluator.evaluate(rule, 65, 1000L + 6 * 60 * 1000L, defaultDate, initialState)
        assertTrue(result is EvaluationResult.Escalate)
    }

    @Test
    fun `new day resets state`() {
        val rule = AppRule("com.test", 60, true)
        val initialState = WarningState("com.test", defaultDate.minusDays(1), 3, 1000L)
        
        val result = evaluator.evaluate(rule, 65, 1000L + 24 * 60 * 60 * 1000L, defaultDate, initialState)
        assertTrue(result is EvaluationResult.ShowWarning)
        val state = (result as EvaluationResult.ShowWarning).newWarningState
        assertEquals(1, state.warningCount)
    }
}