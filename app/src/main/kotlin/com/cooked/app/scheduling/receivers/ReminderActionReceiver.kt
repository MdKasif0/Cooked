package com.cooked.app.scheduling.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.cooked.app.domain.repository.ItemRepository
import com.cooked.app.scheduling.alarm.AlarmScheduler
import com.cooked.app.scheduling.notification.ReminderNotificationBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var itemRepository: ItemRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getStringExtra(ReminderNotificationBuilder.EXTRA_ITEM_ID) ?: return
        val notificationId = intent.getIntExtra(
            ReminderNotificationBuilder.EXTRA_NOTIFICATION_ID,
            ReminderNotificationBuilder.getNotificationId(itemId)
        )
        val action = intent.action
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                executeAction(context, action, itemId, notificationId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    suspend fun executeAction(
        context: Context,
        action: String?,
        itemId: String,
        notificationId: Int
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        val now = Instant.now()
        when (action) {
            ACTION_DONE -> {
                itemRepository.markDone(itemId, at = now)
                alarmScheduler.cancel(itemId)
            }

            ACTION_SNOOZE_10M -> {
                val snoozeUntil = now.plus(Duration.ofMinutes(10))
                itemRepository.snooze(itemId, snoozeUntil)
                alarmScheduler.schedule(itemId)
            }

            ACTION_SNOOZE_1H -> {
                val snoozeUntil = now.plus(Duration.ofHours(1))
                itemRepository.snooze(itemId, snoozeUntil)
                alarmScheduler.schedule(itemId)
            }

            ACTION_SNOOZE_TOMORROW -> {
                val item = itemRepository.getItem(itemId)
                val zone = try {
                    ZoneId.of(item?.timeZoneId ?: ZoneId.systemDefault().id)
                } catch (_: Exception) {
                    ZoneId.systemDefault()
                }
                val tomorrowAt9 = now.atZone(zone)
                    .toLocalDate()
                    .plusDays(1)
                    .atTime(LocalTime.of(9, 0))
                    .atZone(zone)
                    .toInstant()

                itemRepository.snooze(itemId, tomorrowAt9)
                alarmScheduler.schedule(itemId)
            }
        }

        // If active notifications <= 1, clean up the group summary notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val remainingReminders = notificationManager.activeNotifications.count {
                it.id != ReminderNotificationBuilder.SUMMARY_NOTIFICATION_ID &&
                    it.notification.group == ReminderNotificationBuilder.GROUP_COOKED_REMINDERS
            }
            if (remainingReminders <= 1) {
                notificationManager.cancel(ReminderNotificationBuilder.SUMMARY_NOTIFICATION_ID)
            }
        }
    }

    companion object {
        const val ACTION_DONE = "com.cooked.app.ACTION_DONE"
        const val ACTION_SNOOZE_10M = "com.cooked.app.ACTION_SNOOZE_10M"
        const val ACTION_SNOOZE_1H = "com.cooked.app.ACTION_SNOOZE_1H"
        const val ACTION_SNOOZE_TOMORROW = "com.cooked.app.ACTION_SNOOZE_TOMORROW"
    }
}
