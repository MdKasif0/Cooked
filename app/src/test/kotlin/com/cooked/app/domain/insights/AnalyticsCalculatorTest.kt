package com.cooked.app.domain.insights

import com.cooked.app.domain.insights.model.InsightRange
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemEvent
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.model.NagPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

class AnalyticsCalculatorTest {

    private val baseNow = Instant.parse("2026-11-08T12:00:00Z") // Sunday
    private val zone = ZoneId.of("UTC")

    private fun createItem(
        id: String = UUID.randomUUID().toString(),
        title: String = "Test Item",
        dueAt: Instant = baseNow,
        completedAt: Instant? = null,
        kind: ItemKind = ItemKind.REMINDER
    ): Item = Item(
        id = id,
        kind = kind,
        title = title,
        note = null,
        emoji = null,
        dueAt = dueAt,
        allDay = false,
        timeZoneId = "UTC",
        recurrence = null,
        nag = NagPolicy(intervalMinutes = 10),
        critical = false,
        status = if (completedAt != null) ItemStatus.DONE else ItemStatus.ACTIVE,
        snoozedUntil = null,
        completedAt = completedAt,
        effortMinutes = null,
        progress = 0,
        checklist = emptyList(),
        createdAt = baseNow.minus(Duration.ofDays(2)),
        updatedAt = baseNow
    )

    private fun createEvent(
        itemId: String,
        type: ItemEventType,
        at: Instant = baseNow,
        meta: Map<String, String> = emptyMap()
    ): ItemEvent = ItemEvent(
        id = UUID.randomUUID().toString(),
        itemId = itemId,
        type = type,
        at = at,
        meta = meta
    )

    @Test
    fun `zero data computes safe defaults and no insights`() {
        val summary = AnalyticsCalculator.calculate(
            completedItems = emptyList(),
            previousCompletedItems = emptyList(),
            events = emptyList(),
            previousEvents = emptyList(),
            weeklySnapshots = emptyList(),
            allDeadlines = emptyList(),
            range = InsightRange.WEEK_7D,
            now = baseNow,
            zone = zone
        )

        assertEquals(0, summary.doneCount)
        assertEquals(0.0, summary.onTimeRate, 0.001)
        assertNull(summary.onTimeDelta)
        assertNull(summary.latenessMedianMinutes)
        assertNull(summary.bestWeekday)
        assertEquals(0.0, summary.nagsPerWin, 0.001)
        assertEquals(0, summary.insights.size)
        assertEquals(12, summary.cookedScoreHistory.size)
        assertEquals(91, summary.doneByDay.size)
    }

    @Test
    fun `single data point does not emit insights below threshold of 5`() {
        val item = createItem(completedAt = baseNow.minus(Duration.ofHours(1)))
        val summary = AnalyticsCalculator.calculate(
            completedItems = listOf(item),
            previousCompletedItems = emptyList(),
            events = emptyList(),
            previousEvents = emptyList(),
            weeklySnapshots = emptyList(),
            allDeadlines = emptyList(),
            range = InsightRange.WEEK_7D,
            now = baseNow,
            zone = zone
        )

        assertEquals(1, summary.doneCount)
        assertEquals(1.0, summary.onTimeRate, 0.001)
        assertEquals(1, summary.doneDelta)
        // Strictly no insights for fewer than 5 data points
        assertEquals(0, summary.insights.size)
    }

    @Test
    fun `on-time rate and delta calculation`() {
        // Current: 4 on-time, 1 late = 80% (4/5)
        val current = listOf(
            createItem(dueAt = baseNow, completedAt = baseNow.minus(Duration.ofMinutes(10))),
            createItem(dueAt = baseNow, completedAt = baseNow.minus(Duration.ofMinutes(5))),
            createItem(dueAt = baseNow, completedAt = baseNow),
            createItem(dueAt = baseNow, completedAt = baseNow.minus(Duration.ofMinutes(20))),
            createItem(dueAt = baseNow, completedAt = baseNow.plus(Duration.ofMinutes(30))) // late
        )
        // Previous: 2 on-time, 2 late = 50% (2/4)
        val previous = listOf(
            createItem(dueAt = baseNow, completedAt = baseNow.minus(Duration.ofMinutes(10))),
            createItem(dueAt = baseNow, completedAt = baseNow.minus(Duration.ofMinutes(5))),
            createItem(dueAt = baseNow, completedAt = baseNow.plus(Duration.ofMinutes(15))),
            createItem(dueAt = baseNow, completedAt = baseNow.plus(Duration.ofMinutes(45)))
        )

        val summary = AnalyticsCalculator.calculate(
            completedItems = current,
            previousCompletedItems = previous,
            events = emptyList(),
            previousEvents = emptyList(),
            weeklySnapshots = emptyList(),
            allDeadlines = emptyList(),
            range = InsightRange.WEEK_7D,
            now = baseNow,
            zone = zone
        )

        assertEquals(5, summary.doneCount)
        assertEquals(0.8, summary.onTimeRate, 0.001)
        assertEquals(0.3, summary.onTimeDelta!!, 0.001) // +30 points
        assertTrue(summary.insights.any { it.id == "ontime_delta" })
    }

    @Test
    fun `snooze habit detects peak hour and most snoozed item`() {
        val itemId1 = "item_dbms"
        val itemId2 = "item_gym"
        val events = listOf(
            createEvent(itemId = itemId1, type = ItemEventType.SNOOZED, at = Instant.parse("2026-11-08T15:10:00Z")), // 15h
            createEvent(itemId = itemId1, type = ItemEventType.SNOOZED, at = Instant.parse("2026-11-08T15:25:00Z")), // 15h
            createEvent(itemId = itemId1, type = ItemEventType.SNOOZED, at = Instant.parse("2026-11-08T15:40:00Z")), // 15h
            createEvent(itemId = itemId2, type = ItemEventType.SNOOZED, at = Instant.parse("2026-11-08T09:00:00Z")), // 9h
            createEvent(itemId = itemId1, type = ItemEventType.SNOOZED, at = Instant.parse("2026-11-08T15:55:00Z"))  // 15h (total 5 snoozes)
        )
        val items = listOf(
            createItem(id = itemId1, title = "DBMS Assignment", completedAt = baseNow)
        )

        val summary = AnalyticsCalculator.calculate(
            completedItems = items,
            previousCompletedItems = emptyList(),
            events = events,
            previousEvents = emptyList(),
            weeklySnapshots = emptyList(),
            allDeadlines = items,
            range = InsightRange.WEEK_7D,
            now = baseNow,
            zone = zone
        )

        assertEquals(15, summary.snoozeHabit.peakHour)
        assertEquals(4, summary.snoozeHabit.peakCount)
        assertEquals("DBMS Assignment", summary.snoozeHabit.mostSnoozedItemTitle)
        assertEquals(4, summary.snoozeHabit.mostSnoozedCount)
        // 5 snoozes meets threshold -> emits snooze peak insight
        assertTrue(summary.insights.any { it.id == "snooze_peak" })
    }

    @Test
    fun `lateness median calculated accurately`() {
        // Late by 10m, 20m, 40m, 60m, 100m -> median is 40m
        val items = listOf(
            createItem(dueAt = baseNow, completedAt = baseNow.plus(Duration.ofMinutes(10))),
            createItem(dueAt = baseNow, completedAt = baseNow.plus(Duration.ofMinutes(20))),
            createItem(dueAt = baseNow, completedAt = baseNow.plus(Duration.ofMinutes(40))),
            createItem(dueAt = baseNow, completedAt = baseNow.plus(Duration.ofMinutes(60))),
            createItem(dueAt = baseNow, completedAt = baseNow.plus(Duration.ofMinutes(100)))
        )

        val summary = AnalyticsCalculator.calculate(
            completedItems = items,
            previousCompletedItems = emptyList(),
            events = emptyList(),
            previousEvents = emptyList(),
            weeklySnapshots = emptyList(),
            allDeadlines = emptyList(),
            range = InsightRange.WEEK_7D,
            now = baseNow,
            zone = zone
        )

        assertEquals(40, summary.latenessMedianMinutes)
    }

    @Test
    fun `best weekday requires minimum 5 completions to surface`() {
        // 4 completions on Monday -> below threshold of 5 -> bestWeekday must be null
        val mondayTime = Instant.parse("2026-11-02T10:00:00Z") // Monday
        val items4 = (1..4).map {
            createItem(dueAt = mondayTime, completedAt = mondayTime)
        }

        val summary4 = AnalyticsCalculator.calculate(
            completedItems = items4,
            previousCompletedItems = emptyList(),
            events = emptyList(),
            previousEvents = emptyList(),
            weeklySnapshots = emptyList(),
            allDeadlines = emptyList(),
            range = InsightRange.WEEK_7D,
            now = baseNow,
            zone = zone
        )
        assertNull(summary4.bestWeekday)

        // 5 completions on Monday -> meets threshold -> Monday surfaces!
        val items5 = (1..5).map {
            createItem(dueAt = mondayTime, completedAt = mondayTime)
        }
        val summary5 = AnalyticsCalculator.calculate(
            completedItems = items5,
            previousCompletedItems = emptyList(),
            events = emptyList(),
            previousEvents = emptyList(),
            weeklySnapshots = emptyList(),
            allDeadlines = emptyList(),
            range = InsightRange.WEEK_7D,
            now = baseNow,
            zone = zone
        )
        assertEquals(DayOfWeek.MONDAY, summary5.bestWeekday)
    }
}

