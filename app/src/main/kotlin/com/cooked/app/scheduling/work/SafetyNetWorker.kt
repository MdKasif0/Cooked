package com.cooked.app.scheduling.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.repository.ItemRepository
import com.cooked.app.scheduling.alarm.AlarmScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

class SafetyNetWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SafetyNetWorkerEntryPoint {
        fun itemRepository(): ItemRepository
        fun alarmScheduler(): AlarmScheduler
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            SafetyNetWorkerEntryPoint::class.java
        )
        val itemRepository = entryPoint.itemRepository()
        val alarmScheduler = entryPoint.alarmScheduler()

        val now = Instant.now()
        val activeItems = itemRepository.getActiveItems()

        for (item in activeItems) {
            // Check if alarm was missed: overdue by > 15 min with no active snooze
            val isOverdue = now.isAfter(item.dueAt.plus(Duration.ofMinutes(15)))
            val isSnoozed = item.snoozedUntil != null && item.snoozedUntil.isAfter(now)

            if (isOverdue && !isSnoozed) {
                val nagCount = itemRepository.getNagCount(item.id)
                if (nagCount == 0) {
                    val lateByMinutes = Duration.between(item.dueAt, now).toMinutes()
                    itemRepository.logEvent(
                        itemId = item.id,
                        type = ItemEventType.MISSED,
                        meta = mapOf(
                            "lateByMinutes" to lateByMinutes.toString(),
                            "detectedBy" to "SafetyNetWorker"
                        ),
                        at = now
                    )
                }
            }

            // Always ensure each active item has a pending alarm planned
            alarmScheduler.schedule(item)
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "cooked_daily_safety_net"

        fun enqueueDaily(context: Context) {
            val request = PeriodicWorkRequestBuilder<SafetyNetWorker>(1, TimeUnit.DAYS)
                .setConstraints(Constraints.NONE)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
