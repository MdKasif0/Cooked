package com.cooked.app.domain.rescue

import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

class RescuePlannerTest {

    private val zone = ZoneId.of("Asia/Kolkata")
    private val baseDate = LocalDate.of(2026, 11, 10)
    // Now: 10 Nov 2026 at 10:00 AM
    private val now = baseDate.atTime(10, 0).atZone(zone).toInstant()

    @Test
    fun generatePlan_allocates45MinSessions() {
        // Target: Due in 2 days, 180 min effort, 0 progress -> Needs 4 x 45m sessions
        val dueAt = baseDate.plusDays(2).atTime(18, 0).atZone(zone).toInstant()
        val target = Item(
            id = UUID.randomUUID().toString(),
            title = "Final Project Report",
            dueAt = dueAt,
            allDay = false,
            kind = ItemKind.DEADLINE,
            effortMinutes = 180,
            progress = 0
        )

        val plan = RescuePlanner.generatePlan(
            target = target,
            allItems = listOf(target),
            now = now,
            dailyCapacityMinutes = 300,
            quietHoursStart = LocalTime.of(22, 0),
            quietHoursEnd = LocalTime.of(8, 0),
            zone = zone
        )

        assertEquals(4, plan.sessions.size)
        plan.sessions.forEach { session ->
            assertEquals(45, session.durationMinutes)
            assertTrue(session.title.contains("Session"))
        }
        assertEquals(180, plan.totalMinutesPlanned)
        assertFalse(plan.isOverloaded)
    }

    @Test
    fun generatePlan_respectsQuietHours() {
        // Target due tomorrow morning at 11:00 AM.
        // Now is 10 Nov at 21:30 (9:30 PM). Quiet hours: 22:00 to 08:00.
        // Sessions should not be placed during 22:00 - 08:00.
        val lateNow = baseDate.atTime(21, 30).atZone(zone).toInstant()
        val dueAt = baseDate.plusDays(1).atTime(12, 0).atZone(zone).toInstant()
        val target = Item(
            id = UUID.randomUUID().toString(),
            title = "Urgent Lab Submission",
            dueAt = dueAt,
            allDay = false,
            kind = ItemKind.DEADLINE,
            effortMinutes = 90,
            progress = 0
        )

        val plan = RescuePlanner.generatePlan(
            target = target,
            allItems = listOf(target),
            now = lateNow,
            dailyCapacityMinutes = 300,
            quietHoursStart = LocalTime.of(22, 0),
            quietHoursEnd = LocalTime.of(8, 0),
            zone = zone
        )

        plan.sessions.forEach { session ->
            val sessionTime = session.scheduledStart.atZone(zone).toLocalTime()
            val isInQuietHours = sessionTime.isAfter(LocalTime.of(22, 0)) || sessionTime.isBefore(LocalTime.of(8, 0))
            assertFalse("Session scheduled at $sessionTime falls in quiet hours", isInQuietHours)
        }
    }

    @Test
    fun generatePlan_skipsCalendarBusyBlocks() {
        val dueAt = baseDate.atTime(18, 0).atZone(zone).toInstant()
        val target = Item(
            id = UUID.randomUUID().toString(),
            title = "DBMS Assignment",
            dueAt = dueAt,
            allDay = false,
            kind = ItemKind.DEADLINE,
            effortMinutes = 90,
            progress = 0
        )

        // Busy block on calendar: 10 Nov from 10:00 to 13:00 (Meeting / Class)
        val busyBlock = TimeBlock(
            start = baseDate.atTime(10, 0).atZone(zone).toInstant(),
            end = baseDate.atTime(13, 0).atZone(zone).toInstant(),
            title = "Team Meeting"
        )

        val plan = RescuePlanner.generatePlan(
            target = target,
            allItems = listOf(target),
            now = now,
            dailyCapacityMinutes = 300,
            calendarBusyBlocks = listOf(busyBlock),
            zone = zone
        )

        plan.sessions.forEach { session ->
            val sessionEnd = session.scheduledStart.plus(session.durationMinutes.toLong(), ChronoUnit.MINUTES)
            val overlapsBusy = session.scheduledStart.isBefore(busyBlock.end) && sessionEnd.isAfter(busyBlock.start)
            assertFalse("Session ${session.scheduledStart} overlaps busy block", overlapsBusy)
        }
    }

    @Test
    fun generatePlan_detectsOverloadAndRecommendsPush() {
        // Target: Due today in 3 hours, requires 300 min effort (5 hours), impossible!
        val dueAt = baseDate.atTime(13, 0).atZone(zone).toInstant()
        val target = Item(
            id = UUID.randomUUID().toString(),
            title = "Overloaded Exam Prep",
            dueAt = dueAt,
            allDay = false,
            kind = ItemKind.DEADLINE,
            effortMinutes = 300,
            progress = 0
        )

        val otherDeadline = Item(
            id = UUID.randomUUID().toString(),
            title = "Optional Blog Post",
            dueAt = baseDate.atTime(16, 0).atZone(zone).toInstant(),
            allDay = false,
            kind = ItemKind.DEADLINE,
            effortMinutes = 120,
            progress = 0
        )

        val plan = RescuePlanner.generatePlan(
            target = target,
            allItems = listOf(target, otherDeadline),
            now = now,
            dailyCapacityMinutes = 120,
            zone = zone
        )

        assertTrue(plan.isOverloaded)
        assertTrue(plan.pushRecommendations.isNotEmpty())
    }
}
