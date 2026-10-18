package com.example.doomscroller.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeNotificationPolicyPreferences : NotificationPolicyPreferences {
    private var reminderIntervalMinutes = 5
    private var reminderCapPer15Min = 3
    private val histories = mutableMapOf<String, List<Long>>()
    private val lastActiveTimes = mutableMapOf<String, Long>()

    override fun getReminderIntervalMinutes(): Int = reminderIntervalMinutes
    override fun setReminderIntervalMinutes(minutes: Int) {
        reminderIntervalMinutes = minutes.coerceIn(1, 60)
    }

    override fun getReminderCapPer15Min(): Int = reminderCapPer15Min
    override fun setReminderCapPer15Min(cap: Int) {
        reminderCapPer15Min = cap.coerceIn(1, 6)
    }

    override fun getNotificationHistory(packageName: String): List<Long> {
        return histories[packageName] ?: emptyList()
    }

    override fun saveNotificationHistory(packageName: String, timestamps: List<Long>) {
        if (timestamps.isEmpty()) {
            histories.remove(packageName)
        } else {
            histories[packageName] = timestamps.sorted()
        }
    }

    override fun clearNotificationHistory(packageName: String) {
        histories.remove(packageName)
    }

    override fun getLastActiveNotificationTime(packageName: String): Long {
        return lastActiveTimes[packageName] ?: 0L
    }

    override fun setLastActiveNotificationTime(packageName: String, time: Long) {
        lastActiveTimes[packageName] = time
    }

    override fun clearActiveReminderState(packageName: String) {
        lastActiveTimes.remove(packageName)
    }
}

class NotificationRateLimiterTest {

    private lateinit var prefs: FakeNotificationPolicyPreferences
    private lateinit var rateLimiter: NotificationRateLimiter

    private val pkgA = "com.instagram.android"
    private val pkgB = "com.google.android.youtube"

    @Before
    fun setUp() {
        prefs = FakeNotificationPolicyPreferences()
        rateLimiter = NotificationRateLimiter(prefs)
    }

    @Test
    fun `defaults conform to exact policy specifications`() {
        assertEquals(5, prefs.getReminderIntervalMinutes())
        assertEquals(3, prefs.getReminderCapPer15Min())
    }

    @Test
    fun `initial trigger sends notification immediately`() {
        val now = 100_000L
        val result = rateLimiter.evaluateNotification(
            packageName = pkgA,
            now = now,
            isNewEpisode = true,
            recordIfAllowed = true
        )

        assertTrue("Initial trigger must allow notification immediately", result.allowed)
        assertEquals(1, result.countIn15mWindow)
        assertEquals(3, result.cap)
        assertEquals(5, result.intervalMinutes)
        assertEquals(listOf(now), prefs.getNotificationHistory(pkgA))
        assertEquals(now, prefs.getLastActiveNotificationTime(pkgA))
    }

    @Test
    fun `repeated polling within interval is suppressed (deduplication)`() {
        val t0 = 100_000L
        // 1. Initial trigger
        val initialResult = rateLimiter.evaluateNotification(pkgA, t0, isNewEpisode = true)
        assertTrue(initialResult.allowed)

        // 2. Poll 10 seconds later
        val poll10s = rateLimiter.evaluateNotification(pkgA, t0 + 10_000L, isNewEpisode = false)
        assertFalse("10-second polling must be suppressed", poll10s.allowed)

        // 3. Poll 60 seconds later
        val poll60s = rateLimiter.evaluateNotification(pkgA, t0 + 60_000L, isNewEpisode = false)
        assertFalse("60-second polling must be suppressed", poll60s.allowed)

        // 4. Poll 4 minutes later (interval is 5 min)
        val poll4m = rateLimiter.evaluateNotification(pkgA, t0 + 4 * 60_000L, isNewEpisode = false)
        assertFalse("4-minute polling must be suppressed (5m interval)", poll4m.allowed)
    }

    @Test
    fun `repeat reminder allowed after configured interval elapses`() {
        val t0 = 100_000L
        // Initial trigger at t0
        rateLimiter.evaluateNotification(pkgA, t0, isNewEpisode = true)

        // Exactly 5 minutes later
        val t5m = t0 + 5 * 60_000L
        val repeatResult = rateLimiter.evaluateNotification(pkgA, t5m, isNewEpisode = false)

        assertTrue("Repeat notification must be allowed after 5-minute interval elapses", repeatResult.allowed)
        assertEquals(2, repeatResult.countIn15mWindow)
        assertEquals(listOf(t0, t5m), prefs.getNotificationHistory(pkgA))
    }

    @Test
    fun `frequency cap strictly suppresses reminders exceeding cap in rolling 15 minutes`() {
        val t0 = 100_000L
        // 1st notification at t0
        val r1 = rateLimiter.evaluateNotification(pkgA, t0, isNewEpisode = true)
        assertTrue("1st notification allowed", r1.allowed)

        // 2nd notification at t0 + 5m
        val t5m = t0 + 5 * 60_000L
        val r2 = rateLimiter.evaluateNotification(pkgA, t5m, isNewEpisode = false)
        assertTrue("2nd notification allowed", r2.allowed)

        // 3rd notification at t0 + 10m (cap is 3)
        val t10m = t0 + 10 * 60_000L
        val r3 = rateLimiter.evaluateNotification(pkgA, t10m, isNewEpisode = false)
        assertTrue("3rd notification allowed", r3.allowed)

        // 4th notification at t0 + 15m (15m window from t0: rolling window contains t0, t5m, t10m -> 3 events)
        val t15m = t0 + 15 * 60_000L - 1000L // 14m 59s
        val r4 = rateLimiter.evaluateNotification(pkgA, t15m, isNewEpisode = false)
        assertFalse("4th notification must be suppressed due to cap of 3 per 15 minutes", r4.allowed)
        assertTrue(r4.reason.contains("Rolling 15-minute cap"))
    }

    @Test
    fun `rolling window ages out older notifications allowing subsequent reminders`() {
        val t0 = 100_000L
        // Trigger 3 notifications: t0, t0 + 5m, t0 + 10m
        rateLimiter.evaluateNotification(pkgA, t0, isNewEpisode = true)
        rateLimiter.evaluateNotification(pkgA, t0 + 5 * 60_000L, isNewEpisode = false)
        rateLimiter.evaluateNotification(pkgA, t0 + 10 * 60_000L, isNewEpisode = false)

        // At t0 + 16m: t0 is now outside the 15-minute rolling window! (Window contains only t5m, t10m = 2 events)
        val t16m = t0 + 16 * 60_000L
        val result = rateLimiter.evaluateNotification(pkgA, t16m, isNewEpisode = false)
        assertTrue("Notification must be permitted once oldest timestamp ages out of 15m window", result.allowed)
        assertEquals(3, result.countIn15mWindow)
    }

    @Test
    fun `recovery clears active reminder state when condition ends`() {
        val t0 = 100_000L
        rateLimiter.evaluateNotification(pkgA, t0, isNewEpisode = true)
        assertEquals(t0, prefs.getLastActiveNotificationTime(pkgA))

        // Condition ends (penalty expires or user stops scrolling)
        rateLimiter.onConditionEnded(pkgA)
        assertEquals("Active episode state must be cleared", 0L, prefs.getLastActiveNotificationTime(pkgA))
        // Rolling history is preserved for frequency cap integrity
        assertEquals(listOf(t0), prefs.getNotificationHistory(pkgA))
    }

    @Test
    fun `re-triggering treats event as new while respecting rolling frequency cap`() {
        val t0 = 100_000L
        // 1st episode at t0
        rateLimiter.evaluateNotification(pkgA, t0, isNewEpisode = true)
        // Condition ends at t0 + 3m
        rateLimiter.onConditionEnded(pkgA)

        // User opens app again at t0 + 4m (less than 5m interval!)
        val t4m = t0 + 4 * 60_000L
        val retriggerResult = rateLimiter.evaluateNotification(pkgA, t4m, isNewEpisode = true)
        assertTrue("Re-triggered event must trigger immediately as new episode", retriggerResult.allowed)
        assertEquals(2, retriggerResult.countIn15mWindow)

        // Now trigger 3rd at t4m + 5m (t9m)
        val t9m = t4m + 5 * 60_000L
        rateLimiter.evaluateNotification(pkgA, t9m, isNewEpisode = false)
        rateLimiter.onConditionEnded(pkgA)

        // At t11m, user tries to retrigger again: window has [t0, t4m, t9m] = 3 events (cap reached!)
        val t11m = t0 + 11 * 60_000L
        val capBlockedRetrigger = rateLimiter.evaluateNotification(pkgA, t11m, isNewEpisode = true)
        assertFalse("Re-triggering must still be suppressed if rolling 15m cap is reached", capBlockedRetrigger.allowed)
    }

    @Test
    fun `scope is per monitored app and does not bleed across packages`() {
        val t0 = 100_000L
        // Exhaust cap on pkgA (3 notifications)
        rateLimiter.evaluateNotification(pkgA, t0, isNewEpisode = true)
        rateLimiter.evaluateNotification(pkgA, t0 + 5 * 60_000L, isNewEpisode = false)
        rateLimiter.evaluateNotification(pkgA, t0 + 10 * 60_000L, isNewEpisode = false)

        val pkgAResult = rateLimiter.evaluateNotification(pkgA, t0 + 12 * 60_000L, isNewEpisode = false)
        assertFalse("pkgA must be capped", pkgAResult.allowed)

        // pkgB should NOT be affected by pkgA's notifications
        val pkgBResult = rateLimiter.evaluateNotification(pkgB, t0 + 12 * 60_000L, isNewEpisode = true)
        assertTrue("pkgB must be allowed independently of pkgA", pkgBResult.allowed)
        assertEquals(1, pkgBResult.countIn15mWindow)
    }

    @Test
    fun `configurable bounds are strictly enforced`() {
        // Interval: 1 to 60 minutes
        prefs.setReminderIntervalMinutes(0)
        assertEquals(1, prefs.getReminderIntervalMinutes())

        prefs.setReminderIntervalMinutes(75)
        assertEquals(60, prefs.getReminderIntervalMinutes())

        prefs.setReminderIntervalMinutes(15)
        assertEquals(15, prefs.getReminderIntervalMinutes())

        // Cap: 1 to 6 per 15 minutes
        prefs.setReminderCapPer15Min(0)
        assertEquals(1, prefs.getReminderCapPer15Min())

        prefs.setReminderCapPer15Min(10)
        assertEquals(6, prefs.getReminderCapPer15Min())

        prefs.setReminderCapPer15Min(5)
        assertEquals(5, prefs.getReminderCapPer15Min())
    }

    @Test
    fun `configuration change applies to future reminders without extra immediate notifications`() {
        val t0 = 100_000L
        rateLimiter.evaluateNotification(pkgA, t0, isNewEpisode = true)

        // User changes interval from 5m to 10m at t0 + 2m
        prefs.setReminderIntervalMinutes(10)

        // At t0 + 6m: 6m > old interval (5m), but 6m < new interval (10m)
        val result6m = rateLimiter.evaluateNotification(pkgA, t0 + 6 * 60_000L, isNewEpisode = false)
        assertFalse("New 10m interval must be respected without extra immediate notification", result6m.allowed)

        // At t0 + 10m: new 10m interval elapses
        val result10m = rateLimiter.evaluateNotification(pkgA, t0 + 10 * 60_000L, isNewEpisode = false)
        assertTrue("Notification allowed after new 10m interval elapses", result10m.allowed)
    }

    @Test
    fun `penalty duration is separate from reminder interval`() {
        // Default penalty duration in RuleEvaluator is 5 minutes
        assertEquals(5 * 60 * 1000L, RuleEvaluator.MINIMUM_PENALTY_MILLIS)

        // Changing reminder interval to 1 minute does not alter penalty duration
        prefs.setReminderIntervalMinutes(1)
        assertEquals(1, prefs.getReminderIntervalMinutes())
        assertEquals(5 * 60 * 1000L, RuleEvaluator.MINIMUM_PENALTY_MILLIS)

        // Changing reminder interval to 60 minutes does not alter penalty duration
        prefs.setReminderIntervalMinutes(60)
        assertEquals(60, prefs.getReminderIntervalMinutes())
        assertEquals(5 * 60 * 1000L, RuleEvaluator.MINIMUM_PENALTY_MILLIS)
    }
}
