package com.example.doomscroller.data.monitor

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.ArrayMap
import android.util.Log
import java.time.LocalDate
import java.time.ZoneId

data class UsageSnapshot(
    val usageMap: Map<String, Long>,
    val foregroundPackage: String?
)

class UsageRepository(private val context: Context) {
    companion object {
        private const val TAG = "UsageRepository"
        private const val CACHE_TTL_MS = 2500L
        private const val RECENT_EVENTS_WINDOW_MS = 60_000L // 60s rolling window for foreground detection
    }

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    @Volatile
    private var lastQueryTime = 0L
    @Volatile
    private var cachedSnapshot: UsageSnapshot = UsageSnapshot(emptyMap(), null)

    fun getTodayUsage(forceRefresh: Boolean = false): Map<String, Long> {
        return getUsageSnapshot(forceRefresh).usageMap
    }

    fun getUsageSnapshot(forceRefresh: Boolean = false): UsageSnapshot {
        val now = System.currentTimeMillis()
        if (!forceRefresh && now - lastQueryTime < CACHE_TTL_MS && cachedSnapshot.usageMap.isNotEmpty()) {
            return cachedSnapshot
        }

        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // 1. Fast path: Query daily aggregates precomputed by Android OS
        val usageMap = ArrayMap<String, Long>()
        try {
            val dailyStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startOfDay,
                now
            )
            if (!dailyStats.isNullOrEmpty()) {
                for (stat in dailyStats) {
                    if (stat.totalTimeInForeground > 0) {
                        val current = usageMap[stat.packageName] ?: 0L
                        usageMap[stat.packageName] = maxOf(current, stat.totalTimeInForeground)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying daily usage stats", e)
        }

        // 2. Query only recent events (last 60s) for real-time foreground detection
        var currentForeground: String? = null
        var foregroundStartTime = 0L
        val recentStart = (now - RECENT_EVENTS_WINDOW_MS).coerceAtLeast(startOfDay)

        try {
            val events = usageStatsManager.queryEvents(recentStart, now)
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val packageName = event.packageName ?: continue
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        currentForeground = packageName
                        foregroundStartTime = event.timeStamp
                    }
                    UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED -> {
                        if (currentForeground == packageName) {
                            currentForeground = null
                            foregroundStartTime = 0L
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying recent UsageEvents", e)
        }

        // If recent window didn't capture a transition and dailyStats was empty, fallback to cached foreground
        if (currentForeground == null && cachedSnapshot.foregroundPackage != null) {
            currentForeground = cachedSnapshot.foregroundPackage
        }

        // Add real-time live elapsed delta for the active foreground application
        if (currentForeground != null && foregroundStartTime > 0L) {
            val sessionDuration = (now - foregroundStartTime).coerceAtLeast(0L)
            if (sessionDuration > 0) {
                val previousTotal = usageMap[currentForeground] ?: 0L
                usageMap[currentForeground] = previousTotal + sessionDuration
            }
        }

        lastQueryTime = now
        val snapshot = UsageSnapshot(usageMap = usageMap, foregroundPackage = currentForeground)
        cachedSnapshot = snapshot
        return snapshot
    }
}
