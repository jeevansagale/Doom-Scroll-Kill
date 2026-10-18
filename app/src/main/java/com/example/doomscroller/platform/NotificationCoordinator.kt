package com.example.doomscroller.platform

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.doomscroller.MainActivity
import com.example.doomscroller.R

class NotificationCoordinator(private val context: Context) {
    companion object {
        private const val TAG = "NotificationCoordinator"
        const val CHANNEL_WARNINGS = "doomscroller_warnings"
        const val CHANNEL_SERVICE = "doomscroller_service"
        const val SERVICE_NOTIFICATION_ID = 9001
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val permissionManager = PermissionManager(context)

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val warningChannel = NotificationChannel(
                CHANNEL_WARNINGS,
                "Usage Warnings & Penalties",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when daily app usage limit or cooldown penalty is active"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE,
                "Background Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent indicator for digital wellbeing monitoring"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(warningChannel)
            notificationManager.createNotificationChannel(serviceChannel)
            Log.d(TAG, "Notification channels registered successfully")
        }
    }

    fun getNotificationId(packageName: String): Int {
        // Deterministic stable ID per package
        return 1000 + (packageName.hashCode() and 0x7FFFFFFF % 10000)
    }

    fun createServiceNotification(): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            SERVICE_NOTIFICATION_ID,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_SERVICE)
            .setContentTitle("Doomscroller Monitoring Active")
            .setContentText("Actively monitoring screen time and usage limits")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    fun showReminderNotification(
        packageName: String,
        appName: String,
        reminderNumber: Int,
        ignoredCount: Int,
        limitCount: Int,
        intervalMinutes: Int
    ): Boolean {
        if (!permissionManager.hasNotificationPermission()) {
            Log.w(TAG, "POST_NOTIFICATIONS not granted. Cannot post reminder for $packageName")
            return false
        }

        try {
            val notificationId = getNotificationId(packageName)

            val leaveAppIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val leavePendingIntent = PendingIntent.getActivity(
                context,
                notificationId + 1,
                leaveAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val ackIntent = Intent(context, UsageMonitoringService::class.java).apply {
                action = "ACTION_ACKNOWLEDGE"
                putExtra("packageName", packageName)
            }
            val ackPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(
                    context,
                    notificationId + 2,
                    ackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getService(
                    context,
                    notificationId + 2,
                    ackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            val title = "Reminder #$reminderNumber: $appName (${reminderNumber * intervalMinutes}m spent)"
            val body = if (ignoredCount > 0) {
                "$ignoredCount reminders ignored. Cooldown enforcement will activate at $limitCount."
            } else {
                "You have been using $appName for ${reminderNumber * intervalMinutes} minutes. Step away to maintain focus."
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_WARNINGS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Exit to Home Screen",
                    leavePendingIntent
                )
                .addAction(
                    android.R.drawable.checkbox_on_background,
                    "Acknowledge",
                    ackPendingIntent
                )

            notificationManager.notify(notificationId, builder.build())
            Log.i(TAG, "Delivered Reminder #$reminderNumber for $packageName (ignored=$ignoredCount/$limitCount)")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deliver reminder notification for $packageName", e)
            return false
        }
    }

    fun showWarningNotification(
        packageName: String,
        appName: String,
        usedMinutes: Int,
        limitMinutes: Int,
        warningCount: Int,
        isPenaltyActive: Boolean,
        remainingSeconds: Int,
        ignoredRemindersCount: Int = 0
    ): Boolean {
        if (!permissionManager.hasNotificationPermission()) {
            Log.w(TAG, "POST_NOTIFICATIONS not granted. Cannot post warning for $packageName")
            return false
        }

        try {
            val notificationId = getNotificationId(packageName)

            // Intent to open Interruption Screen
            val openInterruptionIntent = Intent(context, MainActivity::class.java).apply {
                action = "com.example.doomscroller.ACTION_INTERRUPTION"
                putExtra("destination", "interruption")
                putExtra("packageName", packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                openInterruptionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Intent to Leave App (Home screen)
            val leaveAppIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val leavePendingIntent = PendingIntent.getActivity(
                context,
                notificationId + 1,
                leaveAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val remainingMin = remainingSeconds / 60
            val remainingSec = remainingSeconds % 60
            val countdownStr = String.format("%02d:%02d", remainingMin, remainingSec)

            val title = if (isPenaltyActive) {
                "Penalty Cooldown: $appName Limit Exceeded"
            } else {
                "Limit Reached: $appName"
            }

            val body = if (isPenaltyActive) {
                val ignoredNotice = if (ignoredRemindersCount > 0) " ($ignoredRemindersCount reminders ignored)." else "."
                "Usage limit exceeded$ignoredNotice 5-minute cooldown active ($countdownStr remaining). Stop scrolling and step away."
            } else {
                "Daily limit of ${limitMinutes}m reached (${usedMinutes}m used). Take a break."
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_WARNINGS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(openPendingIntent)
                .setFullScreenIntent(openPendingIntent, true) // Heads-up / full-screen intent
                .setAutoCancel(false)
                .setOngoing(isPenaltyActive)
                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Exit to Home Screen",
                    leavePendingIntent
                )
                .addAction(
                    android.R.drawable.ic_menu_info_details,
                    "View Warning",
                    openPendingIntent
                )

            notificationManager.notify(notificationId, builder.build())
            Log.i(TAG, "Notification delivered successfully for $packageName (id=$notificationId, penalty=$isPenaltyActive, remaining=${remainingSeconds}s)")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deliver notification for $packageName", e)
            return false
        }
    }

    fun cancelWarningNotification(packageName: String) {
        val notificationId = getNotificationId(packageName)
        notificationManager.cancel(notificationId)
        Log.i(TAG, "Cancelled notification for $packageName (id=$notificationId)")
    }
}
