package com.cooked.app.scheduling.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cooked.app.data.dao.WeeklySnapshotDao
import com.cooked.app.data.entity.WeeklySnapshotEntity
import com.cooked.app.domain.deadline.CookedScoreCalculator
import com.cooked.app.domain.repository.ItemRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

class WeeklySnapshotWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WeeklySnapshotEntryPoint {
        fun itemRepository(): ItemRepository
        fun weeklySnapshotDao(): WeeklySnapshotDao
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            WeeklySnapshotEntryPoint::class.java
        )
        val itemRepository = entryPoint.itemRepository()
        val snapshotDao = entryPoint.weeklySnapshotDao()

        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        val activeDeadlines = itemRepository.getActiveItems()
        val score = CookedScoreCalculator.calculate(
            items = activeDeadlines,
            now = now
        ).score

        snapshotDao.insertSnapshot(
            WeeklySnapshotEntity(
                weekStartEpochDay = monday.toEpochDay(),
                cookedScore = score,
                recordedAt = now
            )
        )

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "cooked_weekly_snapshot"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val request = PeriodicWorkRequestBuilder<WeeklySnapshotWorker>(7, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
