package com.cooked.app.scheduling.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.cooked.app.MainActivity
import com.cooked.app.core.time.TimeUtils
import com.cooked.app.domain.model.Reminder
import com.cooked.app.scheduling.receivers.ReminderAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    override fun schedule(reminder: Reminder) {
        if (alarmManager == null || reminder.isCompleted) {
            cancel(reminder.id)
            return
        }

        val now = TimeUtils.nowUtcMillis()
        val triggerAtMillis = when {
            reminder.snoozedUntil != null && reminder.snoozedUntil > now -> reminder.snoozedUntil
            reminder.isNaggingActive || reminder.isOverdue -> now + (reminder.nagIntervalMinutes * 60 * 1000L)
            reminder.dueInstant != null && reminder.dueInstant > now -> reminder.dueInstant
            else -> {
                cancel(reminder.id)
                return
            }
        }

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderAlarmReceiver.EXTRA_TITLE, reminder.title)
            putExtra(ReminderAlarmReceiver.EXTRA_NOTE, reminder.note)
            putExtra(ReminderAlarmReceiver.EXTRA_NAG_INTERVAL, reminder.nagIntervalMinutes)
        }

        val requestCode = reminder.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    override fun cancel(reminderId: String) {
        if (alarmManager == null) return
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val requestCode = reminderId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
