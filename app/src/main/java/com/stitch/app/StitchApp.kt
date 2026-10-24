package com.stitch.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StitchApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Channel for Foreground Monitoring Service
            val monitoringChannel = NotificationChannel(
                CHANNEL_MONITORING_ID,
                "Focus Monitoring Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when Stitch focus guard is actively running."
                setShowBadge(false)
            }

            // Channel for Peer Accountability Alerts & Nudges
            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS_ID,
                "Peer Accountability Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for friend nudges, pact invitations, and focus locks."
                enableVibration(true)
                setShowBadge(true)
            }

            // Channel for Schedule Reminders
            val scheduleChannel = NotificationChannel(
                CHANNEL_SCHEDULE_ID,
                "Schedule & Auto-lock Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for scheduled focus sessions and calendar auto-locks."
            }

            notificationManager.createNotificationChannels(
                listOf(monitoringChannel, alertsChannel, scheduleChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_MONITORING_ID = "stitch_monitoring_channel"
        const val CHANNEL_ALERTS_ID = "stitch_alerts_channel"
        const val CHANNEL_SCHEDULE_ID = "stitch_schedule_channel"
    }
}
