package com.example.doomscroller.platform

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.example.doomscroller.data.PreferencesManager
import com.example.doomscroller.data.monitor.UsageRepository
import com.example.doomscroller.domain.EnforcementManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class UsageMonitoringService : Service() {
    companion object {
        private const val TAG = "UsageMonitoringService"
        private const val SCREEN_ON_POLL_INTERVAL_MS = 10_000L // 10 seconds when user active
        private const val RESTRICTED_FAST_POLL_INTERVAL_MS = 1_500L // 1.5 seconds when penalty or intervention active
        private const val SCREEN_OFF_POLL_INTERVAL_MS = 60_000L // 60 seconds when screen off

        fun start(context: Context) {
            val intent = Intent(context, UsageMonitoringService::class.java)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.i(TAG, "UsageMonitoringService started")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start UsageMonitoringService", e)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, UsageMonitoringService::class.java)
            try {
                context.stopService(intent)
                Log.i(TAG, "UsageMonitoringService stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop UsageMonitoringService", e)
            }
        }
        fun triggerImmediateCheck(context: Context) {
            val intent = Intent(context, UsageMonitoringService::class.java).apply {
                action = "ACTION_CHECK_NOW"
            }
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to trigger check", e)
            }
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitoringJob: Job? = null
    private var isScreenOn = true

    private lateinit var usageRepository: UsageRepository
    private lateinit var prefs: PreferencesManager
    private lateinit var enforcementManager: EnforcementManager
    private lateinit var notificationCoordinator: NotificationCoordinator

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    Log.d(TAG, "Screen ON: activating active polling interval")
                    isScreenOn = true
                }
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d(TAG, "Screen OFF: throttling polling to preserve battery")
                    isScreenOn = false
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        usageRepository = UsageRepository(applicationContext)
        prefs = PreferencesManager(applicationContext)
        enforcementManager = EnforcementManager.getInstance(applicationContext)
        notificationCoordinator = NotificationCoordinator(applicationContext)

        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        isScreenOn = powerManager?.isInteractive ?: true

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenReceiver, filter)

        // Start as foreground service immediately
        val notification = notificationCoordinator.createServiceNotification()
        startForeground(NotificationCoordinator.SERVICE_NOTIFICATION_ID, notification)
        Log.i(TAG, "UsageMonitoringService onCreate completed with foreground notification")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_CHECK_NOW" -> {
                serviceScope.launch {
                    runEvaluationOnce()
                }
            }
            "ACTION_ACKNOWLEDGE" -> {
                val pkg = intent.getStringExtra("packageName")
                val eventId = intent.getStringExtra("eventId")
                if (!pkg.isNullOrEmpty()) {
                    Log.i(TAG, "Received ACTION_ACKNOWLEDGE intent for $pkg (eventId=$eventId)")
                    enforcementManager.acknowledgeIntervention(pkg, eventId)
                }
            }
        }
        startMonitoringLoop()
        return START_STICKY
    }

    private suspend fun runEvaluationOnce() {
        try {
            val rules = prefs.getRules().filter { it.isEnabled }
            val snapshot = usageRepository.getUsageSnapshot(forceRefresh = true)
            enforcementManager.evaluateRules(
                usageMap = snapshot.usageMap,
                rules = rules,
                foregroundPackage = snapshot.foregroundPackage
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in runEvaluationOnce", e)
        }
    }

    private fun startMonitoringLoop() {
        if (monitoringJob?.isActive == true) return

        monitoringJob = serviceScope.launch {
            Log.i(TAG, "Monitoring loop started on background coroutine")
            while (isActive) {
                val hasActiveRestrictions = enforcementManager.hasActivePenaltiesOrInterventions()
                try {
                    val rules = prefs.getRules().filter { it.isEnabled }
                    val snapshot = usageRepository.getUsageSnapshot(forceRefresh = hasActiveRestrictions)
                    enforcementManager.evaluateRules(
                        usageMap = snapshot.usageMap,
                        rules = rules,
                        foregroundPackage = snapshot.foregroundPackage
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error in usage monitoring loop", e)
                }

                val sleepTime = if (!isScreenOn) {
                    SCREEN_OFF_POLL_INTERVAL_MS
                } else if (hasActiveRestrictions) {
                    RESTRICTED_FAST_POLL_INTERVAL_MS
                } else {
                    SCREEN_ON_POLL_INTERVAL_MS
                }
                delay(sleepTime)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "Screen receiver already unregistered")
        }
        serviceScope.cancel()
        Log.i(TAG, "UsageMonitoringService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
