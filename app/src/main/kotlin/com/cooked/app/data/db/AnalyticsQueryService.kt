package com.cooked.app.data.db

import com.cooked.app.data.dao.AnalyticsDao
import com.cooked.app.data.dao.DayCompletionCount
import com.cooked.app.data.dao.OnTimeStats
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsQueryService @Inject constructor(
    private val analyticsDao: AnalyticsDao
) {
    suspend fun getDoneCountPerDay(from: Instant, to: Instant): List<DayCompletionCount> {
        return analyticsDao.getDoneCountPerDay(from, to)
    }

    suspend fun getOnTimeVsLateCompletions(from: Instant, to: Instant): OnTimeStats {
        return analyticsDao.getOnTimeVsLateCompletions(from, to) ?: OnTimeStats(0, 0)
    }

    suspend fun getAverageNagsBeforeDone(from: Instant, to: Instant): Double {
        val completedCount = analyticsDao.getCompletedItemsCount(from, to)
        if (completedCount == 0) return 0.0
        val totalNags = analyticsDao.getTotalNagsForCompletedItems(from, to)
        return totalNags.toDouble() / completedCount.toDouble()
    }

    suspend fun getMedianLatenessMinutes(from: Instant, to: Instant): Double {
        val latenessList = analyticsDao.getLatenessMinutesList(from, to)
        if (latenessList.isEmpty()) return 0.0
        val size = latenessList.size
        return if (size % 2 == 1) {
            latenessList[size / 2].toDouble()
        } else {
            (latenessList[(size / 2) - 1] + latenessList[size / 2]) / 2.0
        }
    }

    suspend fun getSnoozeCountsByHourOfDay(from: Instant, to: Instant, zoneId: ZoneId): Map<Int, Int> {
        val timestamps = analyticsDao.getSnoozeTimestamps(from, to)
        val hourMap = (0..23).associateWith { 0 }.toMutableMap()
        for (ts in timestamps) {
            val hour = ts.atZone(zoneId).hour
            hourMap[hour] = (hourMap[hour] ?: 0) + 1
        }
        return hourMap
    }

    suspend fun getCompletionsByWeekday(from: Instant, to: Instant, zoneId: ZoneId): Map<DayOfWeek, Int> {
        val timestamps = analyticsDao.getCompletionTimestamps(from, to)
        val weekdayMap = DayOfWeek.values().associateWith { 0 }.toMutableMap()
        for (ts in timestamps) {
            val dow = ts.atZone(zoneId).dayOfWeek
            weekdayMap[dow] = (weekdayMap[dow] ?: 0) + 1
        }
        return weekdayMap
    }
}

