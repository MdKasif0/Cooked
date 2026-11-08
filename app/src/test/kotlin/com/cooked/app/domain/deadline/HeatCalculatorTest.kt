package com.cooked.app.domain.deadline

import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

class HeatCalculatorTest {

    private val baseNow = Instant.parse("2026-11-08T10:00:00Z")
    private val zone = ZoneId.of("UTC")
    private val quietStart = LocalTime.of(22, 0)
    private val quietEnd = LocalTime.of(8, 0)

    private fun deadlineItem(
        id: String = "deadline-1",
        title: String = "Semester Project",
        dueAt: Instant,
        effortMinutes: Int? = 120,
        progress: Int = 0,
        status: ItemStatus = ItemStatus.ACTIVE,
        startBy: Instant? = null
    ): Item {
        return Item(
            id = id,
            title = title,
            kind = ItemKind.DEADLINE,
            dueAt = dueAt,
            effortMinutes = effortMinutes,
            progress = progress,
            status = status,
            startBy = startBy,
            timeZoneId = "UTC"
        )
    }

    @Test
    fun `effort known - chill when ratio is under 0_40`() {
        // Due in 5 days (5 days * 120 mins capacity = 600 available), effort = 120 mins -> ratio = 0.20
        val dueAt = baseNow.plus(Duration.ofDays(5))
        val item = deadlineItem(dueAt = dueAt, effortMinutes = 120, progress = 0)

        val result = HeatCalculator.calculate(
            item = item,
            now = baseNow,
            dailyCapacityMinutes = 120,
            quietHoursStart = quietStart,
            quietHoursEnd = quietEnd
        )

        assertEquals(HeatLevel.CHILL, result.heatLevel)
        assertTrue(result.slackMinutes > 0)
        assertTrue(result.reason.contains("free before"))
    }

    @Test
    fun `effort known - warm when ratio is between 0_40 and 0_70`() {
        // 2 days away: 360 mins available. Effort = 180 mins -> ratio = 0.50
        val dueAt = baseNow.plus(Duration.ofDays(2))
        val item = deadlineItem(dueAt = dueAt, effortMinutes = 180, progress = 0)

        val result = HeatCalculator.calculate(
            item = item,
            now = baseNow,
            dailyCapacityMinutes = 120,
            quietHoursStart = quietStart,
            quietHoursEnd = quietEnd
        )

        assertEquals(HeatLevel.WARM, result.heatLevel)
    }

    @Test
    fun `effort known - toasty when ratio is between 0_70 and 1_00`() {
        // 2 days away: 360 mins available. Effort = 280 mins -> ratio = 0.777
        val dueAt = baseNow.plus(Duration.ofDays(2))
        val item = deadlineItem(dueAt = dueAt, effortMinutes = 280, progress = 0)

        val result = HeatCalculator.calculate(
            item = item,
            now = baseNow,
            dailyCapacityMinutes = 120,
            quietHoursStart = quietStart,
            quietHoursEnd = quietEnd
        )

        assertEquals(HeatLevel.TOASTY, result.heatLevel)
    }

    @Test
    fun `effort known - cooked when effort exceeds available capacity`() {
        // 1 day away: 240 mins available. Effort = 300 mins -> ratio = 1.25
        val dueAt = baseNow.plus(Duration.ofDays(1))
        val item = deadlineItem(dueAt = dueAt, effortMinutes = 300, progress = 0)

        val result = HeatCalculator.calculate(
            item = item,
            now = baseNow,
            dailyCapacityMinutes = 120,
            quietHoursStart = quietStart,
            quietHoursEnd = quietEnd
        )

        assertEquals(HeatLevel.COOKED, result.heatLevel)
        assertTrue(result.slackMinutes < 0)
    }

    @Test
    fun `effort unknown - urgency calculated by remaining time`() {
        // > 7 days -> CHILL
        val chillItem = deadlineItem(dueAt = baseNow.plus(Duration.ofDays(10)), effortMinutes = null)
        assertEquals(HeatLevel.CHILL, HeatCalculator.calculate(chillItem, baseNow).heatLevel)

        // 3 - 7 days -> WARM
        val warmItem = deadlineItem(dueAt = baseNow.plus(Duration.ofDays(5)), effortMinutes = null)
        assertEquals(HeatLevel.WARM, HeatCalculator.calculate(warmItem, baseNow).heatLevel)

        // 1 - 3 days -> TOASTY
        val toastyItem = deadlineItem(dueAt = baseNow.plus(Duration.ofDays(2)), effortMinutes = null)
        assertEquals(HeatLevel.TOASTY, HeatCalculator.calculate(toastyItem, baseNow).heatLevel)

        // < 24 hours -> COOKED
        val cookedItem = deadlineItem(dueAt = baseNow.plus(Duration.ofHours(12)), effortMinutes = null)
        assertEquals(HeatLevel.COOKED, HeatCalculator.calculate(cookedItem, baseNow).heatLevel)
    }

    @Test
    fun `overdue deadline is always Cooked with negative slack`() {
        val overdueItem = deadlineItem(dueAt = baseNow.minus(Duration.ofHours(3)), effortMinutes = 60)

        val result = HeatCalculator.calculate(overdueItem, baseNow)

        assertEquals(HeatLevel.COOKED, result.heatLevel)
        assertEquals(1f, result.heatProgress, 0.001f)
        assertTrue(result.slackMinutes < 0)
        assertTrue(result.reason.contains("Overdue"))
    }

    @Test
    fun `progress 100 percent returns Chill with zero remaining effort`() {
        val completedItem = deadlineItem(
            dueAt = baseNow.plus(Duration.ofDays(1)),
            effortMinutes = 180,
            progress = 100
        )

        val result = HeatCalculator.calculate(completedItem, baseNow)

        assertEquals(HeatLevel.CHILL, result.heatLevel)
        assertEquals(0, result.remainingEffortMinutes)
        assertEquals(0f, result.heatProgress, 0.001f)
    }

    @Test
    fun `quiet hours exclude night window from available minutes`() {
        // From 10:00 to 23:00 on same day:
        // Waking window is 08:00 to 22:00.
        // Overlap is 10:00 to 22:00 = 12 hours = 720 waking minutes.
        // Capped by dailyCapacity = 120 -> 120 available.
        val dueAt = baseNow.plus(Duration.ofHours(13))
        val available = HeatCalculator.calculateAvailableMinutes(
            now = baseNow,
            dueAt = dueAt,
            dailyCapacityMinutes = 120,
            quietHoursStart = LocalTime.of(22, 0),
            quietHoursEnd = LocalTime.of(8, 0),
            zone = zone
        )

        assertEquals(120, available)
    }

    @Test
    fun `suggestStartBy never returns an instant in the past`() {
        // Deadline is very soon (in 2 hours) with high effort
        val dueAt = baseNow.plus(Duration.ofHours(2))
        val item = deadlineItem(dueAt = dueAt, effortMinutes = 300)

        val suggested = HeatCalculator.suggestStartBy(
            item = item,
            now = baseNow,
            dailyCapacityMinutes = 120,
            preferredNudgeTime = LocalTime.of(18, 0),
            zone = zone
        )

        // Must be at least now + 1 hour, never before now
        assertFalse("Suggested start by must not be before now", suggested.isBefore(baseNow))
        assertTrue("Suggested start by should be at least now + 1h", !suggested.isBefore(baseNow.plus(Duration.ofHours(1))))
    }
}
