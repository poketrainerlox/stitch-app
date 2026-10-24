package com.stitch.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.stitch.app.MainActivity
import com.stitch.app.R
import com.stitch.app.StitchApp
import kotlinx.coroutines.*

class UsageTrackingService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private var isMonitoring = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createServiceNotification("Stitch Focus Guard is active")
        startForeground(NOTIFICATION_ID, notification)

        if (!isMonitoring) {
            isMonitoring = true
            startPeriodicCheck()
        }

        return START_STICKY
    }

    private fun startPeriodicCheck() {
        serviceScope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            while (isActive) {
                try {
                    if (StitchAccessibilityService.activeBlockedPackages.isNotEmpty() && usageStatsManager != null) {
                        val foregroundPkg = getForegroundApp(usageStatsManager)
                        if (foregroundPkg != null &&
                            StitchAccessibilityService.activeBlockedPackages.contains(foregroundPkg) &&
                            foregroundPkg != packageName
                        ) {
                            withContext(Dispatchers.Main) {
                                BlockerOverlayService.showLock(
                                    context = applicationContext,
                                    packageName = foregroundPkg,
                                    appName = StitchAccessibilityService.activeBlockedAppNames[foregroundPkg] ?: "Restricted App",
                                    guardianName = StitchAccessibilityService.activeGuardianName,
                                    pactId = StitchAccessibilityService.activePactId,
                                    remainingSeconds = StitchAccessibilityService.activeRemainingSeconds
                                )
                            }
                        }
                    }
                } catch (_: Exception) {}
                delay(2000L)
            }
        }
    }

    private fun getForegroundApp(usageStatsManager: UsageStatsManager): String? {
        val time = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(time - 10000, time)
        var lastEvent: UsageEvents.Event? = null
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            ) {
                lastEvent = event
            }
        }
        return lastEvent?.packageName
    }

    private fun createServiceNotification(text: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, StitchApp.CHANNEL_MONITORING_ID)
            .setContentTitle("Stitch Focus Shield")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        isMonitoring = false
    }

    companion object {
        private const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            val intent = Intent(context, UsageTrackingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, UsageTrackingService::class.java)
            context.stopService(intent)
        }
    }
}
