package com.cooked.app.scheduling.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat

object NotificationChannels {

    const val CHANNEL_REMINDERS_NORMAL = "reminders_normal"
    const val CHANNEL_REMINDERS_LOUD = "reminders_loud"
    const val CHANNEL_CRITICAL_ALARM = "critical_alarm"
    const val CHANNEL_DEADLINE_PRESSURE = "deadline_pressure"
    const val CHANNEL_SYSTEM = "system"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val alarmAudioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        val notificationAudioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        // 1. reminders_normal
        val normalChannel = NotificationChannel(
            CHANNEL_REMINDERS_NORMAL,
            "Reminders (Normal)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Standard reminder notifications"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
            setSound(notificationSound, notificationAudioAttributes)
        }

        // 2. reminders_loud
        val loudChannel = NotificationChannel(
            CHANNEL_REMINDERS_LOUD,
            "Reminders (Loud)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Escalated persistent reminders for tasks you have ignored"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 600, 200, 600, 200, 1000)
            setSound(alarmSound, alarmAudioAttributes)
        }

        // 3. critical_alarm
        val criticalChannel = NotificationChannel(
            CHANNEL_CRITICAL_ALARM,
            "Critical Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "High urgency alarms for critical commitments and final call notifications"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 800, 200, 800, 200, 800, 200, 1200)
            setSound(alarmSound, alarmAudioAttributes)
            setBypassDnd(true)
        }

        // 4. deadline_pressure
        val deadlineChannel = NotificationChannel(
            CHANNEL_DEADLINE_PRESSURE,
            "Deadline Pressure",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Upcoming deadline countdown warnings and effort ramp alerts"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400)
            setSound(notificationSound, notificationAudioAttributes)
        }

        // 5. system
        val systemChannel = NotificationChannel(
            CHANNEL_SYSTEM,
            "System & Reliability",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Battery optimization warnings, exact alarm diagnostics and sync notices"
            enableVibration(false)
            setSound(null, null)
        }

        notificationManager.createNotificationChannels(
            listOf(normalChannel, loudChannel, criticalChannel, deadlineChannel, systemChannel)
        )
    }

    /**
     * Checks whether notifications are enabled overall, and returns the enabled status for each channel.
     */
    fun checkChannelsHealth(context: Context): Map<String, Boolean> {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) {
            return mapOf(
                CHANNEL_REMINDERS_NORMAL to false,
                CHANNEL_REMINDERS_LOUD to false,
                CHANNEL_CRITICAL_ALARM to false,
                CHANNEL_DEADLINE_PRESSURE to false,
                CHANNEL_SYSTEM to false
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            if (notificationManager != null) {
                return listOf(
                    CHANNEL_REMINDERS_NORMAL,
                    CHANNEL_REMINDERS_LOUD,
                    CHANNEL_CRITICAL_ALARM,
                    CHANNEL_DEADLINE_PRESSURE,
                    CHANNEL_SYSTEM
                ).associateWith { channelId ->
                    val channel = notificationManager.getNotificationChannel(channelId)
                    channel == null || channel.importance != NotificationManager.IMPORTANCE_NONE
                }
            }
        }

        return listOf(
            CHANNEL_REMINDERS_NORMAL,
            CHANNEL_REMINDERS_LOUD,
            CHANNEL_CRITICAL_ALARM,
            CHANNEL_DEADLINE_PRESSURE,
            CHANNEL_SYSTEM
        ).associateWith { true }
    }
}
