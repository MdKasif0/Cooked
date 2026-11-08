package com.cooked.app.scheduling.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.cooked.app.MainActivity
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.repository.ItemRepository
import com.cooked.app.scheduling.receivers.ReminderAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAlarmScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val itemRepository: ItemRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: CookedClock
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    private val scheduledAlarmsMap = ConcurrentHashMap<String, ScheduledAlarmInfo>()

    override suspend fun schedule(item: Item, nagCount: Int?) {
        val now = clock.now()
        val prefs = userPreferencesRepository.userPrefs.first()
        val count = nagCount ?: itemRepository.getNagCount(item.id)

        val nextAlarm = AlarmPlanner.plan(
            item = item,
            now = now,
            prefs = prefs,
            nagCount = count
        )

        if (nextAlarm == null) {
            cancel(item.id)
            return
        }

        // Cancel previous pending alarms for all types of this item to keep exactly ONE pending alarm per item
        cancelAllPendingIntentsForItem(item.id)

        val requestCode = getRequestCode(item.id, nextAlarm.type)
        val alarmIntent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_ITEM_ID, item.id)
            putExtra(ReminderAlarmReceiver.EXTRA_ALARM_TYPE, nextAlarm.type.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ReminderAlarmReceiver.EXTRA_ITEM_ID, item.id)
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerMillis = nextAlarm.time.toEpochMilli().coerceAtLeast(now.toEpochMilli())
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager?.canScheduleExactAlarms() == true
        } else {
            true
        }

        try {
            if (canScheduleExact) {
                if (item.critical || nextAlarm.type == AlarmType.FIRST || nextAlarm.type == AlarmType.FINAL_CALL) {
                    val clockInfo = AlarmManager.AlarmClockInfo(triggerMillis, showPendingIntent)
                    alarmManager?.setAlarmClock(clockInfo, pendingIntent)
                } else {
                    alarmManager?.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager?.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager?.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }

        scheduledAlarmsMap[item.id] = ScheduledAlarmInfo(
            itemId = item.id,
            itemTitle = item.title,
            triggerTime = nextAlarm.time,
            type = nextAlarm.type,
            isExact = canScheduleExact
        )
    }

    override suspend fun schedule(itemId: String) {
        val item = itemRepository.getItem(itemId)
        if (item != null) {
            schedule(item)
        } else {
            cancel(itemId)
        }
    }

    override fun cancel(itemId: String) {
        cancelAllPendingIntentsForItem(itemId)
        scheduledAlarmsMap.remove(itemId)
    }

    override suspend fun rescheduleAll() {
        val activeItems = itemRepository.getActiveItems()
        for (item in activeItems) {
            schedule(item)
        }
    }

    override fun getScheduledAlarms(): List<ScheduledAlarmInfo> {
        return scheduledAlarmsMap.values.sortedBy { it.triggerTime }
    }

    override suspend fun fireNow(itemId: String) {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_ITEM_ID, itemId)
            putExtra(ReminderAlarmReceiver.EXTRA_ALARM_TYPE, AlarmType.FIRST.name)
        }
        context.sendBroadcast(intent)
    }

    private fun cancelAllPendingIntentsForItem(itemId: String) {
        if (alarmManager == null) return
        for (type in AlarmType.entries) {
            val requestCode = getRequestCode(itemId, type)
            val intent = Intent(context, ReminderAlarmReceiver::class.java)
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

    companion object {
        fun getRequestCode(itemId: String, type: AlarmType): Int {
            var hash = itemId.hashCode()
            hash = 31 * hash + type.name.hashCode()
            return hash and 0x7FFFFFFF
        }
    }
}
