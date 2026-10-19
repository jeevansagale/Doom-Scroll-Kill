package com.example.doomscroller.data.monitor

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.ArrayMap
import android.util.Log
import com.example.doomscroller.platform.PermissionManager
import java.time.LocalDate
import java.time.ZoneId

sealed class UsageDataState {
    object PermissionRequired : UsageDataState()
    data class QueryFailed(val errorMessage: String) : UsageDataState()
    data class Success(
        val date: LocalDate,
        val dayStartMillis: Long,
        val totalExternalUsageMillis: Long,
        val isEventsBased: Boolean
    ) : UsageDataState()
}

data class UsageSnapshot(
    val usageMap: Map<String, Long>,
    val foregroundPackage: String?,
    val totalExternalUsageMillis: Long = 0L,
    val state: UsageDataState = UsageDataState.Success(
        date = LocalDate.now(),
        dayStartMillis = 0L,
        totalExternalUsageMillis = totalExternalUsageMillis,
        isEventsBased = true
    ),
    val isAvailable: Boolean = true
)

class UsageRepository(private val context: Context) {
    companion object {
        private const val TAG = "UsageRepository"
        private const val CACHE_TTL_MS = 2500L
        private const val LOOKBACK_WINDOW_MS = 15 * 60 * 1000L // 15 mins lookback to establish foreground app at midnight
    }

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    private val permissionManager = PermissionManager(context)
    private val selfPackageName = context.packageName

    @Volatile
    private var lastQueryTime = 0L
    @Volatile
    private var cachedLocalDate: LocalDate? = null
    @Volatile
    private var cachedSnapshot: UsageSnapshot = UsageSnapshot(
        usageMap = emptyMap(),
        foregroundPackage = null,
        totalExternalUsageMillis = 0L,
        state = UsageDataState.PermissionRequired,
        isAvailable = false
    )

    fun getTodayUsage(forceRefresh: Boolean = false): Map<String, Long> {
        return getUsageSnapshot(forceRefresh).usageMap
    }

    fun getUsageSnapshot(forceRefresh: Boolean = false): UsageSnapshot {
        // 1. Explicitly check system-granted Usage Access permission
        if (!permissionManager.hasUsageAccess() || usageStatsManager == null) {
            val unavailSnapshot = UsageSnapshot(
                usageMap = emptyMap(),
                foregroundPackage = null,
                totalExternalUsageMillis = 0L,
                state = UsageDataState.PermissionRequired,
                isAvailable = false
            )
            cachedSnapshot = unavailSnapshot
            return unavailSnapshot
        }

        val now = System.currentTimeMillis()
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val startOfDay = UsageCalculator.getLocalDayStartMillis(today, zoneId)

        // 2. Return cache only if within TTL, date has not changed, and data is valid
        if (!forceRefresh &&
            (now - lastQueryTime < CACHE_TTL_MS) &&
            (cachedLocalDate == today) &&
            cachedSnapshot.isAvailable
        ) {
            return cachedSnapshot
        }

        try {
            // 3. Event-based tracking from midnight with lookback window
            val queryStart = (startOfDay - LOOKBACK_WINDOW_MS).coerceAtLeast(0L)
            val events = usageStatsManager.queryEvents(queryStart, now)
            val rawEvents = mutableListOf<RawUsageEvent>()
            var latestForeground: String? = null

            if (events != null) {
                val event = UsageEvents.Event()
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    val pkg = event.packageName ?: continue
                    rawEvents.add(
                        RawUsageEvent(
                            packageName = pkg,
                            timeStamp = event.timeStamp,
                            eventType = event.eventType
                        )
                    )
                    when (event.eventType) {
                        UsageEvents.Event.ACTIVITY_RESUMED -> latestForeground = pkg
                        UsageEvents.Event.ACTIVITY_PAUSED -> {
                            if (latestForeground == pkg) latestForeground = null
                        }
                        UsageEvents.Event.SCREEN_NON_INTERACTIVE, UsageEvents.Event.KEYGUARD_SHOWN -> {
                            latestForeground = null
                        }
                    }
                }
            }

            // 4. Compute accurate event-based usage (excludes selfPackageName and yesterday's usage)
            val usageMap = ArrayMap<String, Long>()
            val calculatedMap = UsageCalculator.computeUsageFromEvents(
                events = rawEvents,
                startOfDayMillis = startOfDay,
                nowMillis = now,
                selfPackageName = selfPackageName
            )
            usageMap.putAll(calculatedMap)

            var isEventsBased = rawEvents.isNotEmpty()

            // 5. Fallback path if UsageEvents returned empty (e.g. system pruned events after reboot)
            if (usageMap.isEmpty()) {
                val dailyStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startOfDay,
                    now
                )
                if (!dailyStats.isNullOrEmpty()) {
                    for (stat in dailyStats) {
                        val pkg = stat.packageName
                        if (pkg != null && pkg != selfPackageName && stat.totalTimeInForeground > 0L) {
                            val current = usageMap[pkg] ?: 0L
                            usageMap[pkg] = maxOf(current, stat.totalTimeInForeground)
                        }
                    }
                    isEventsBased = false
                }
            }

            // If latest foreground package is selfPackageName, keep foregroundPackage accurate for service,
            // but selfPackageName is strictly excluded from usageMap and external total.
            val totalExternalUsage = usageMap.values.sum()

            lastQueryTime = now
            cachedLocalDate = today
            val snapshot = UsageSnapshot(
                usageMap = usageMap,
                foregroundPackage = latestForeground,
                totalExternalUsageMillis = totalExternalUsage,
                state = UsageDataState.Success(
                    date = today,
                    dayStartMillis = startOfDay,
                    totalExternalUsageMillis = totalExternalUsage,
                    isEventsBased = isEventsBased
                ),
                isAvailable = true
            )
            cachedSnapshot = snapshot
            return snapshot
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating device usage stats", e)
            val failedSnapshot = UsageSnapshot(
                usageMap = emptyMap(),
                foregroundPackage = null,
                totalExternalUsageMillis = 0L,
                state = UsageDataState.QueryFailed(e.message ?: "Unknown query failure"),
                isAvailable = false
            )
            cachedSnapshot = failedSnapshot
            return failedSnapshot
        }
    }
}
