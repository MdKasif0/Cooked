package com.cooked.app.scheduling.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cooked.app.domain.repository.ReminderRepository
import com.cooked.app.scheduling.notification.ReminderNotificationBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: ReminderRepository

    @Inject
    lateinit var notificationBuilder: ReminderNotificationBuilder

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Reminder"
        val note = intent.getStringExtra(EXTRA_NOTE)
        val nagInterval = intent.getIntExtra(EXTRA_NAG_INTERVAL, 10)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminder = repository.getReminderById(reminderId)
                if (reminder == null || reminder.isCompleted) {
                    return@launch
                }

                repository.setNaggingActive(reminderId, isNagging = true, incrementNagCount = true)

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notification = notificationBuilder.createNotification(
                    reminderId = reminderId,
                    title = title,
                    note = note,
                    nagInterval = nagInterval
                )
                notificationManager.notify(reminderId.hashCode(), notification)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_NOTE = "extra_note"
        const val EXTRA_NAG_INTERVAL = "extra_nag_interval"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}

