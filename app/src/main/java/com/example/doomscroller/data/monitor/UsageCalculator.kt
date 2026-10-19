package com.example.doomscroller.data.monitor

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Representation of an Android UsageEvent for testable calculation.
 */
data class RawUsageEvent(
    val packageName: String,
    val timeStamp: Long,
    val eventType: Int
) {
    companion object {
        const val ACTIVITY_RESUMED = 1 // UsageEvents.Event.ACTIVITY_RESUMED / MOVE_TO_FOREGROUND
        const val ACTIVITY_PAUSED = 2  // UsageEvents.Event.ACTIVITY_PAUSED / MOVE_TO_BACKGROUND
        const val SCREEN_INTERACTIVE = 15 // UsageEvents.Event.SCREEN_INTERACTIVE
        const val SCREEN_NON_INTERACTIVE = 16 // UsageEvents.Event.SCREEN_NON_INTERACTIVE
        const val KEYGUARD_SHOWN = 18 // UsageEvents.Event.KEYGUARD_SHOWN
        const val KEYGUARD_HIDDEN = 19 // UsageEvents.Event.KEYGUARD_HIDDEN
    }
}

object UsageCalculator {

    /**
     * Computes the epoch millisecond start of the local calendar day.
     * Uses ZonedDateTime to properly account for daylight saving time (DST) shifts
     * (23-hour or 25-hour days) and dynamic timezone changes.
     */
    fun getLocalDayStartMillis(
        date: LocalDate = LocalDate.now(ZoneId.systemDefault()),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        return date.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    /**
     * Determines whether a given timestamp is on a different calendar day
     * in the specified timezone than the given reference day.
     */
    fun isDifferentDay(
        referenceTimestamp: Long,
        currentTimestamp: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Boolean {
        val refDate = Instant.ofEpochMilli(referenceTimestamp).atZone(zoneId).toLocalDate()
        val curDate = Instant.ofEpochMilli(currentTimestamp).atZone(zoneId).toLocalDate()
        return refDate != curDate
    }

    /**
     * Computes per-package foreground usage durations from an ordered stream of raw usage events,
     * ensuring:
     * 1. Usage strictly begins at [startOfDayMillis]; yesterday's usage is never leaked.
     * 2. Double counting is impossible because only one package is active at any time.
     * 3. Screen-off (SCREEN_NON_INTERACTIVE) and lock (KEYGUARD_SHOWN) pause foreground time.
     * 4. Doomscroll's own package [selfPackageName] is completely excluded from the external app total.
     * 5. Active ongoing sessions are accrued up to [nowMillis].
     */
    fun computeUsageFromEvents(
        events: List<RawUsageEvent>,
        startOfDayMillis: Long,
        nowMillis: Long,
        selfPackageName: String
    ): Map<String, Long> {
        val usageMap = mutableMapOf<String, Long>()
        var currentFgPackage: String? = null
        var currentFgStartTime = 0L

        fun closeCurrentSession(endTime: Long) {
            val pkg = currentFgPackage ?: return
            if (currentFgStartTime > 0L) {
                val sessionStart = maxOf(currentFgStartTime, startOfDayMillis)
                val sessionEnd = minOf(endTime, nowMillis)
                val duration = (sessionEnd - sessionStart).coerceAtLeast(0L)
                if (duration > 0L && pkg != selfPackageName) {
                    usageMap[pkg] = (usageMap[pkg] ?: 0L) + duration
                }
            }
            currentFgPackage = null
            currentFgStartTime = 0L
        }

        for (event in events) {
            val time = event.timeStamp
            if (time > nowMillis) break

            when (event.eventType) {
                RawUsageEvent.ACTIVITY_RESUMED -> {
                    // Close previous foreground session if a different app was active
                    if (currentFgPackage != null && currentFgPackage != event.packageName) {
                        closeCurrentSession(time)
                    }
                    currentFgPackage = event.packageName
                    currentFgStartTime = time
                }
                RawUsageEvent.ACTIVITY_PAUSED -> {
                    if (currentFgPackage == event.packageName) {
                        closeCurrentSession(time)
                    }
                }
                RawUsageEvent.SCREEN_NON_INTERACTIVE, RawUsageEvent.KEYGUARD_SHOWN -> {
                    // Device went to sleep or locked: pause foreground accrual
                    closeCurrentSession(time)
                }
                RawUsageEvent.SCREEN_INTERACTIVE, RawUsageEvent.KEYGUARD_HIDDEN -> {
                    // Screen became active again.
                    // If no explicit resumed event occurs, wait for the next resumed event.
                }
            }
        }

        // Accrue currently ongoing foreground session up to nowMillis
        if (currentFgPackage != null && currentFgStartTime > 0L) {
            closeCurrentSession(nowMillis)
        }

        return usageMap
    }
}
