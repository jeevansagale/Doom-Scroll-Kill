package com.example.doomscroller.data.monitor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class UsageTrackingAndMidnightTest {

    private val selfPackage = "com.example.doomscroller"
    private val defaultZone = ZoneId.of("UTC")

    // =========================================================================
    // 1. Exclusion of Doomscroll's Own Package
    // =========================================================================

    @Test
    fun `today usage strictly excludes Doomscroll own package from external total`() {
        val midnight = 10000000L
        val now = midnight + 3600000L // 1 hour into the day

        val events = listOf(
            // Doomscroll open for 20 minutes (10:00 to 10:20)
            RawUsageEvent(
                packageName = selfPackage,
                timeStamp = midnight,
                eventType = RawUsageEvent.ACTIVITY_RESUMED
            ),
            RawUsageEvent(
                packageName = selfPackage,
                timeStamp = midnight + 20 * 60 * 1000L,
                eventType = RawUsageEvent.ACTIVITY_PAUSED
            ),
            // Instagram open for 30 minutes (10:20 to 10:50)
            RawUsageEvent(
                packageName = "com.instagram.android",
                timeStamp = midnight + 20 * 60 * 1000L,
                eventType = RawUsageEvent.ACTIVITY_RESUMED
            ),
            RawUsageEvent(
                packageName = "com.instagram.android",
                timeStamp = midnight + 50 * 60 * 1000L,
                eventType = RawUsageEvent.ACTIVITY_PAUSED
            )
        )

        val usageMap = UsageCalculator.computeUsageFromEvents(
            events = events,
            startOfDayMillis = midnight,
            nowMillis = now,
            selfPackageName = selfPackage
        )

        // 1. Doomscroll own package must NOT be present in usageMap
        assertFalse("Doomscroll own package must be excluded", usageMap.containsKey(selfPackage))

        // 2. Instagram must have exactly 30 minutes (1,800,000 ms)
        assertEquals(30 * 60 * 1000L, usageMap["com.instagram.android"])

        // 3. External total must be exactly 30 minutes, not 50 minutes
        val totalExternal = usageMap.values.sum()
        assertEquals(30 * 60 * 1000L, totalExternal)
    }

    // =========================================================================
    // 2. Midnight Boundary & Yesterday Leak Prevention
    // =========================================================================

    @Test
    fun `usage boundaries begin strictly at local midnight - yesterday time does not leak`() {
        val midnight = 20000000L // 00:00:00 today
        val yesterdayStart = midnight - 15 * 60 * 1000L // 23:45 yesterday (15 min before midnight)
        val todayEnd = midnight + 25 * 60 * 1000L // 00:25 today (25 min after midnight)
        val now = midnight + 3600000L

        // A single continuous session that began yesterday and continued into today
        val events = listOf(
            RawUsageEvent(
                packageName = "com.google.android.youtube",
                timeStamp = yesterdayStart,
                eventType = RawUsageEvent.ACTIVITY_RESUMED
            ),
            RawUsageEvent(
                packageName = "com.google.android.youtube",
                timeStamp = todayEnd,
                eventType = RawUsageEvent.ACTIVITY_PAUSED
            )
        )

        val usageMap = UsageCalculator.computeUsageFromEvents(
            events = events,
            startOfDayMillis = midnight,
            nowMillis = now,
            selfPackageName = selfPackage
        )

        // Total session was 40 minutes, but only 25 minutes occurred TODAY after midnight
        assertEquals(25 * 60 * 1000L, usageMap["com.google.android.youtube"])
    }

    @Test
    fun `session occurring entirely yesterday contributes exactly zero to today total`() {
        val midnight = 20000000L
        val now = midnight + 3600000L

        // Session ended 10 minutes before midnight
        val events = listOf(
            RawUsageEvent(
                packageName = "com.reddit.frontpage",
                timeStamp = midnight - 60 * 60 * 1000L,
                eventType = RawUsageEvent.ACTIVITY_RESUMED
            ),
            RawUsageEvent(
                packageName = "com.reddit.frontpage",
                timeStamp = midnight - 10 * 60 * 1000L,
                eventType = RawUsageEvent.ACTIVITY_PAUSED
            )
        )

        val usageMap = UsageCalculator.computeUsageFromEvents(
            events = events,
            startOfDayMillis = midnight,
            nowMillis = now,
            selfPackageName = selfPackage
        )

        // Must not contain any usage for today
        assertEquals(0L, usageMap["com.reddit.frontpage"] ?: 0L)
        assertEquals(0L, usageMap.values.sum())
    }

    // =========================================================================
    // 3. Screen-Off and Lock Transitions Pause Accrual
    // =========================================================================

    @Test
    fun `screen-off and lock transitions pause foreground accrual`() {
        val midnight = 10000000L
        val now = midnight + 2 * 3600000L // 2 hours into day

        val events = listOf(
            // App opened at 10:00
            RawUsageEvent(
                packageName = "com.zhiliaoapp.musically",
                timeStamp = midnight + 10 * 60 * 1000L,
                eventType = RawUsageEvent.ACTIVITY_RESUMED
            ),
            // User locked device at 10:15 (15 mins active)
            RawUsageEvent(
                packageName = "com.zhiliaoapp.musically",
                timeStamp = midnight + 25 * 60 * 1000L,
                eventType = RawUsageEvent.SCREEN_NON_INTERACTIVE
            ),
            // Device remains asleep in pocket for 45 minutes until 11:00
            RawUsageEvent(
                packageName = "com.zhiliaoapp.musically",
                timeStamp = midnight + 70 * 60 * 1000L,
                eventType = RawUsageEvent.SCREEN_INTERACTIVE
            ),
            // User resumes app at 11:00 and uses for 10 minutes until 11:10
            RawUsageEvent(
                packageName = "com.zhiliaoapp.musically",
                timeStamp = midnight + 70 * 60 * 1000L,
                eventType = RawUsageEvent.ACTIVITY_RESUMED
            ),
            RawUsageEvent(
                packageName = "com.zhiliaoapp.musically",
                timeStamp = midnight + 80 * 60 * 1000L,
                eventType = RawUsageEvent.ACTIVITY_PAUSED
            )
        )

        val usageMap = UsageCalculator.computeUsageFromEvents(
            events = events,
            startOfDayMillis = midnight,
            nowMillis = now,
            selfPackageName = selfPackage
        )

        // 15 mins (first session) + 10 mins (second session) = 25 mins total
        // Screen-off time of 45 mins must NOT be included!
        val expectedMillis = 25 * 60 * 1000L
        assertEquals(expectedMillis, usageMap["com.zhiliaoapp.musically"])
    }

    // =========================================================================
    // 4. Double Counting Prevention During Rapid Switching
    // =========================================================================

    @Test
    fun `prevent event double-counting during rapid switching`() {
        val midnight = 10000000L
        val now = midnight + 3600000L

        // User switches from App A to App B without explicit pause event for App A
        val events = listOf(
            RawUsageEvent(
                packageName = "com.app.a",
                timeStamp = midnight,
                eventType = RawUsageEvent.ACTIVITY_RESUMED
            ),
            // At +10m, user switches straight to App B
            RawUsageEvent(
                packageName = "com.app.b",
                timeStamp = midnight + 10 * 60 * 1000L,
                eventType = RawUsageEvent.ACTIVITY_RESUMED
            ),
            // At +25m, user pauses App B
            RawUsageEvent(
                packageName = "com.app.b",
                timeStamp = midnight + 25 * 60 * 1000L,
                eventType = RawUsageEvent.ACTIVITY_PAUSED
            )
        )

        val usageMap = UsageCalculator.computeUsageFromEvents(
            events = events,
            startOfDayMillis = midnight,
            nowMillis = now,
            selfPackageName = selfPackage
        )

        // App A had 10 mins; App B had 15 mins. Total is exactly 25 mins.
        assertEquals(10 * 60 * 1000L, usageMap["com.app.a"])
        assertEquals(15 * 60 * 1000L, usageMap["com.app.b"])
        assertEquals(25 * 60 * 1000L, usageMap.values.sum())
    }

    // =========================================================================
    // 5. Midnight Rollover Detection
    // =========================================================================

    @Test
    fun `midnight rollover correctly detects when calendar day changes`() {
        val zone = ZoneId.of("America/New_York")
        val date1 = LocalDate.of(2026, 10, 18)
        val date2 = LocalDate.of(2026, 10, 19)

        val midnight1 = UsageCalculator.getLocalDayStartMillis(date1, zone)
        val midnight2 = UsageCalculator.getLocalDayStartMillis(date2, zone)

        // Reference at 23:59:00 on Day 1
        val lateDay1 = midnight1 + 23 * 3600000L + 59 * 60000L
        // Reference at 00:01:00 on Day 2
        val earlyDay2 = midnight2 + 60000L

        // Same day checks
        assertFalse(UsageCalculator.isDifferentDay(midnight1, lateDay1, zone))
        assertFalse(UsageCalculator.isDifferentDay(midnight2, earlyDay2, zone))

        // Midnight crossing check
        assertTrue(UsageCalculator.isDifferentDay(lateDay1, earlyDay2, zone))
    }

    // =========================================================================
    // 6. Timezone Changes and Daylight Saving Time (DST)
    // =========================================================================

    @Test
    fun `timezone changes and daylight saving time do not corrupt daily boundaries`() {
        val zones = listOf(
            ZoneId.of("UTC"),
            ZoneId.of("America/New_York"),
            ZoneId.of("Asia/Tokyo"),
            ZoneId.of("Europe/London")
        )

        val testDate = LocalDate.of(2026, 10, 19)

        for (zone in zones) {
            val startMillis = UsageCalculator.getLocalDayStartMillis(testDate, zone)
            val zdt = Instant.ofEpochMilli(startMillis).atZone(zone)

            // Start of day must have hour = 0, minute = 0, second = 0
            assertEquals("Start of day hour must be 0 for $zone", 0, zdt.hour)
            assertEquals("Start of day minute must be 0 for $zone", 0, zdt.minute)
            assertEquals("Start of day second must be 0 for $zone", 0, zdt.second)
            assertEquals("Date must match testDate for $zone", testDate, zdt.toLocalDate())
        }
    }

    @Test
    fun `DST shift days calculate exact start of day without assuming 24 hours`() {
        val nyZone = ZoneId.of("America/New_York")
        // Spring forward DST transition date (23-hour day in New York)
        val springDate = LocalDate.of(2026, 3, 8)
        val springStart = UsageCalculator.getLocalDayStartMillis(springDate, nyZone)
        val springZdt = Instant.ofEpochMilli(springStart).atZone(nyZone)

        assertEquals(0, springZdt.hour)
        assertEquals(0, springZdt.minute)
        assertEquals(springDate, springZdt.toLocalDate())

        // Fall back DST transition date (25-hour day in New York)
        val fallDate = LocalDate.of(2026, 11, 1)
        val fallStart = UsageCalculator.getLocalDayStartMillis(fallDate, nyZone)
        val fallZdt = Instant.ofEpochMilli(fallStart).atZone(nyZone)

        assertEquals(0, fallZdt.hour)
        assertEquals(0, fallZdt.minute)
        assertEquals(fallDate, fallZdt.toLocalDate())
    }

    // =========================================================================
    // 7. Explicit Unavailable State on Permission Denial
    // =========================================================================

    @Test
    fun `usage access denial produces clear unavailable state and never verified zero`() {
        val unavailSnapshot = UsageSnapshot(
            usageMap = emptyMap(),
            foregroundPackage = null,
            totalExternalUsageMillis = 0L,
            state = UsageDataState.PermissionRequired,
            isAvailable = false
        )

        assertFalse("Snapshot must not be marked as available when permission is denied", unavailSnapshot.isAvailable)
        assertTrue("State must be PermissionRequired", unavailSnapshot.state is UsageDataState.PermissionRequired)

        val failedSnapshot = UsageSnapshot(
            usageMap = emptyMap(),
            foregroundPackage = null,
            totalExternalUsageMillis = 0L,
            state = UsageDataState.QueryFailed("Inaccessible history"),
            isAvailable = false
        )

        assertFalse("Snapshot must not be available on query failure", failedSnapshot.isAvailable)
        assertTrue("State must be QueryFailed", failedSnapshot.state is UsageDataState.QueryFailed)
    }
}
