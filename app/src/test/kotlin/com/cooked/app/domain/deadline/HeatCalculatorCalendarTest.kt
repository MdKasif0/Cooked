package com.cooked.app.domain.deadline

import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.rescue.TimeBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

class HeatCalculatorCalendarTest {

    private val zone = ZoneId.of("Asia/Kolkata")
    private val baseDate = LocalDate.of(2026, 11, 10)
    // 10 Nov at 09:00
    private val now = baseDate.atTime(9, 0).atZone(zone).toInstant()

    @Test
    fun calculateHeat_calendarBusyBlocksReduceAvailableMinutesAndIncreaseHeat() {
        // Due today at 21:00 (12 hours later, 9am to 9pm).
        // Effort: 180 minutes (3h).
        val dueAt = baseDate.atTime(21, 0).atZone(zone).toInstant()
        val item = Item(
            id = UUID.randomUUID().toString(),
            title = "Machine Learning Assignment",
            dueAt = dueAt,
            allDay = false,
            kind = ItemKind.DEADLINE,
            effortMinutes = 180,
            progress = 0
        )

        // Without calendar busy blocks:
        val freeResult = HeatCalculator.calculateHeat(
            item = item,
            now = now,
            dailyCapacityMinutes = 720,
            quietHoursStart = LocalTime.of(22, 0),
            quietHoursEnd = LocalTime.of(8, 0),
            zone = zone,
            calendarBusyBlocks = emptyList()
        )

        // With calendar busy block: user has lectures from 10:00 to 18:00 (8 hours = 480 mins busy)
        val busyBlock = TimeBlock(
            start = baseDate.atTime(10, 0).atZone(zone).toInstant(),
            end = baseDate.atTime(18, 0).atZone(zone).toInstant(),
            title = "University Lectures"
        )

        val busyResult = HeatCalculator.calculateHeat(
            item = item,
            now = now,
            dailyCapacityMinutes = 720,
            quietHoursStart = LocalTime.of(22, 0),
            quietHoursEnd = LocalTime.of(8, 0),
            zone = zone,
            calendarBusyBlocks = listOf(busyBlock)
        )

        assertTrue(
            "Expected available minutes to be lower with calendar busy blocks: ${busyResult.availableMinutes} vs ${freeResult.availableMinutes}",
            busyResult.availableMinutes < freeResult.availableMinutes
        )

        assertTrue(
            "Expected heat progress to be higher with calendar busy blocks: ${busyResult.heatProgress} vs ${freeResult.heatProgress}",
            busyResult.heatProgress >= freeResult.heatProgress
        )
    }
}

