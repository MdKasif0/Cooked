package com.cooked.app.data.dao

import androidx.room.Dao
import androidx.room.Query
import java.time.Instant

data class DayCompletionCount(
    val dayStartMillis: Long,
    val count: Int
)

data class OnTimeStats(
    val onTimeCount: Int,
    val lateCount: Int
)

data class HourCount(
    val hourOfDay: Int,
    val count: Int
)

data class WeekdayCount(
    val dayOfWeek: Int, // 1..7 for Monday..Sunday
    val count: Int
)

@Dao
interface AnalyticsDao {

    /**
     * Count of DONE events per day in an epoch millis range.
     */
    @Query("""
        SELECT (at / 86400000) * 86400000 AS dayStartMillis, count(*) AS count
        FROM item_events
        WHERE type = 'DONE' AND at >= :from AND at <= :to
        GROUP BY dayStartMillis
        ORDER BY dayStartMillis ASC
    """)
    suspend fun getDoneCountPerDay(from: Instant, to: Instant): List<DayCompletionCount>

    /**
     * On-time vs late completions (completedAt <= dueAt vs completedAt > dueAt).
     */
    @Query("""
        SELECT 
            SUM(CASE WHEN completedAt <= dueAt THEN 1 ELSE 0 END) AS onTimeCount,
            SUM(CASE WHEN completedAt > dueAt THEN 1 ELSE 0 END) AS lateCount
        FROM items
        WHERE status = 'DONE' AND completedAt IS NOT NULL AND completedAt >= :from AND completedAt <= :to AND deletedAt IS NULL
    """)
    suspend fun getOnTimeVsLateCompletions(from: Instant, to: Instant): OnTimeStats?

    /**
     * All late completion differences in minutes for median calculation.
     */
    @Query("""
        SELECT (completedAt - dueAt) / 60000
        FROM items
        WHERE status = 'DONE' AND completedAt IS NOT NULL AND dueAt IS NOT NULL AND completedAt > dueAt AND completedAt >= :from AND completedAt <= :to AND deletedAt IS NULL
        ORDER BY (completedAt - dueAt) ASC
    """)
    suspend fun getLatenessMinutesList(from: Instant, to: Instant): List<Long>

    /**
     * Average nags before done:
     * Counts nag events for items completed within the range.
     */
    @Query("""
        SELECT count(e.id)
        FROM item_events e
        INNER JOIN items i ON e.itemId = i.id
        WHERE e.type = 'NAGGED' AND i.status = 'DONE' AND i.completedAt >= :from AND i.completedAt <= :to
    """)
    suspend fun getTotalNagsForCompletedItems(from: Instant, to: Instant): Int

    @Query("""
        SELECT count(*)
        FROM items
        WHERE status = 'DONE' AND completedAt >= :from AND completedAt <= :to AND deletedAt IS NULL
    """)
    suspend fun getCompletedItemsCount(from: Instant, to: Instant): Int

    /**
     * Snooze event epoch timestamps in range for hour-of-day aggregation.
     */
    @Query("""
        SELECT at FROM item_events
        WHERE type = 'SNOOZED' AND at >= :from AND at <= :to
    """)
    suspend fun getSnoozeTimestamps(from: Instant, to: Instant): List<Instant>

    /**
     * Completed item timestamps in range for weekday aggregation.
     */
    @Query("""
        SELECT completedAt FROM items
        WHERE status = 'DONE' AND completedAt IS NOT NULL AND completedAt >= :from AND completedAt <= :to AND deletedAt IS NULL
    """)
    suspend fun getCompletionTimestamps(from: Instant, to: Instant): List<Instant>

    @Query("""
        SELECT * FROM items
        WHERE status = 'DONE' AND completedAt IS NOT NULL AND completedAt >= :from AND completedAt <= :to AND deletedAt IS NULL
        ORDER BY completedAt DESC
    """)
    suspend fun getCompletedItemsInRange(from: Instant, to: Instant): List<com.cooked.app.data.entity.ItemEntity>

    @Query("""
        SELECT * FROM item_events
        WHERE at >= :from AND at <= :to
        ORDER BY at ASC
    """)
    suspend fun getEventsInRange(from: Instant, to: Instant): List<com.cooked.app.data.entity.ItemEventEntity>

    @Query("""
        SELECT * FROM items
        WHERE status = 'DONE' AND completedAt IS NOT NULL AND completedAt >= :dayStart AND completedAt <= :dayEnd AND deletedAt IS NULL
        ORDER BY completedAt DESC
    """)
    suspend fun getCompletedItemsForDay(dayStart: Instant, dayEnd: Instant): List<com.cooked.app.data.entity.ItemEntity>
}

