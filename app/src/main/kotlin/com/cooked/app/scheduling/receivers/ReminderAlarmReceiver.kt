package com.cooked.app.scheduling.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.repository.ItemRepository
import com.cooked.app.scheduling.alarm.AlarmScheduler
import com.cooked.app.scheduling.notification.ReminderNotificationBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var itemRepository: ItemRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var notificationBuilder: ReminderNotificationBuilder

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getStringExtra(EXTRA_ITEM_ID) ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val item = itemRepository.getItem(itemId)
                if (item == null || item.status != ItemStatus.ACTIVE || item.isDeleted) {
                    alarmScheduler.cancel(itemId)
                    return@launch
                }

                val nagCount = itemRepository.getNagCount(itemId)
                val userPrefs = userPreferencesRepository.userPrefs.first()
                val personality = userPrefs.personality
                val isHinglish = userPrefs.hinglishVibes

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val notification = notificationBuilder.buildReminderNotification(
                    item = item,
                    nagCount = nagCount,
                    personality = personality,
                    isHinglish = isHinglish
                )
                val notificationId = ReminderNotificationBuilder.getNotificationId(itemId)
                notificationManager.notify(notificationId, notification)

                // Log FIRED or NAGGED event
                val eventType = if (nagCount == 0) ItemEventType.FIRED else ItemEventType.NAGGED
                itemRepository.logEvent(
                    itemId = itemId,
                    type = eventType,
                    meta = mapOf("nagCount" to nagCount.toString())
                )

                // Multi-item grouping: collapse if > 3 active cooked reminders
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val activeRemindersCount = notificationManager.activeNotifications.count {
                        it.id != ReminderNotificationBuilder.SUMMARY_NOTIFICATION_ID &&
                            it.notification.group == ReminderNotificationBuilder.GROUP_COOKED_REMINDERS
                    }
                    if (activeRemindersCount > 3) {
                        notificationManager.notify(
                            ReminderNotificationBuilder.SUMMARY_NOTIFICATION_ID,
                            notificationBuilder.buildSummaryNotification(activeRemindersCount)
                        )
                    }
                }

                // Schedule next nag
                alarmScheduler.schedule(item, nagCount = nagCount + 1)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_ITEM_ID = "extra_item_id"
        const val EXTRA_ALARM_TYPE = "extra_alarm_type"
    }
}
