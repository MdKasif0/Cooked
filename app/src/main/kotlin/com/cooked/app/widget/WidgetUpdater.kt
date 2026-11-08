package com.cooked.app.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.repository.ItemRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

/**
 * Battery-Conscious Widget Update Strategy:
 * -----------------------------------------
 * 1. Event-Driven: Whenever an item changes, completes, snoozes, or is created in Room DB,
 *    `CookedApplication` receives the `itemChanged` event and calls `WidgetUpdater.refreshAll(context)`.
 *    Zero battery is consumed when idle.
 *
 * 2. Adaptive Countdown Cadence:
 *    - Hourly cadence when all deadlines are > 1 hour away or when no deadlines exist.
 *    - Minute-aligned exact updates ONLY when a visible pinned/hottest deadline has < 1 hour remaining,
 *      allowing accurate countdowns ("42m", "15m", "1m") right before due time without draining battery.
 */
object WidgetUpdater {

    private val updaterScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private const val ACTION_MINUTE_TICK = "com.cooked.app.action.WIDGET_MINUTE_TICK"
    private const val REQUEST_CODE_MINUTE_TICK = 9182

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetUpdaterEntryPoint {
        fun itemRepository(): ItemRepository
    }

    /**
     * Refresh all 4 Glance widgets.
     */
    fun refreshAll(context: Context) {
        updaterScope.launch {
            try {
                QuickAddWidget().updateAll(context)
                NextUpWidget().updateAll(context)
                CountdownWidget().updateAll(context)
                CookedScoreWidget().updateAll(context)
            } catch (_: Exception) {}

            checkAndScheduleMinuteUpdates(context)
        }
    }

    /**
     * Schedules a minute-aligned tick only if an active deadline is due within the next 60 minutes.
     */
    fun checkAndScheduleMinuteUpdates(context: Context) {
        updaterScope.launch {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetUpdaterEntryPoint::class.java
            )
            val repo = entryPoint.itemRepository()
            val now = Instant.now()
            val nextHour = now.plus(Duration.ofHours(1))

            val hasUrgentCountdown = repo.getActiveItems().any { item ->
                item.kind == ItemKind.DEADLINE && item.dueAt.isAfter(now) && item.dueAt.isBefore(nextHour)
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val intent = Intent(context, WidgetMinuteReceiver::class.java).apply {
                action = ACTION_MINUTE_TICK
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_MINUTE_TICK,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (hasUrgentCountdown && alarmManager != null) {
                // Next minute-aligned tick (e.g. aligned to the start of the next minute)
                val nextMinuteEpochMillis = ((System.currentTimeMillis() / 60000L) + 1) * 60000L
                try {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextMinuteEpochMillis,
                        pendingIntent
                    )
                } catch (_: SecurityException) {}
            } else {
                alarmManager?.cancel(pendingIntent)
            }
        }
    }
}

class WidgetMinuteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        WidgetUpdater.refreshAll(context)
    }
}
