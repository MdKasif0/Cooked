package com.cooked.app.scheduling.alarm

import com.cooked.app.data.datastore.UserPrefs
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.model.NagPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class AlarmPlannerTest {

    private val utcZone = ZoneId.of("UTC")
    private val baseNow = Instant.parse("2026-11-08T12:00:00Z")
    private val defaultPrefs = UserPrefs(
        quietHoursStart = LocalTime.of(22, 0),
        quietHoursEnd = LocalTime.of(8, 0)
    )

    private fun testItem(
        id: String = "item-1",
        title: String = "Test Reminder",
        dueAt: Instant = baseNow.plus(Duration.ofHours(2)),
        status: ItemStatus = ItemStatus.ACTIVE,
        snoozedUntil: Instant? = null,
        startBy: Instant? = null,
        nag: NagPolicy = NagPolicy(intervalMinutes = 10, escalate = true, maxNags = null, respectQuietHours = true),
        critical: Boolean = false,
        isDeleted: Boolean = false,
        timeZoneId: String = "UTC",
        kind: ItemKind = ItemKind.REMINDER
    ): Item {
        return Item(
            id = id,
            title = title,
            dueAt = dueAt,
            status = status,
            snoozedUntil = snoozedUntil,
            startBy = startBy,
            nag = nag,
            critical = critical,
            deletedAt = if (isDeleted) baseNow else null,
            timeZoneId = timeZoneId,
            kind = kind
        )
    }

    @Test
    fun `first alert scheduled at dueAt when now is before dueAt`() {
        val dueAt = baseNow.plus(Duration.ofHours(1))
        val item = testItem(dueAt = dueAt)

        val plan = AlarmPlanner.plan(item = item, now = baseNow, prefs = defaultPrefs, nagCount = 0)

        assertNotNull(plan)
        assertEquals(dueAt, plan?.time)
        assertEquals(AlarmType.FIRST, plan?.type)
    }

    @Test
    fun `completed, soft-deleted, or archived items return null`() {
        val completedItem = testItem(status = ItemStatus.DONE)
        val archivedItem = testItem(status = ItemStatus.ARCHIVED)
        val deletedItem = testItem(isDeleted = true)

        assertNull(AlarmPlanner.plan(completedItem, baseNow, defaultPrefs))
        assertNull(AlarmPlanner.plan(archivedItem, baseNow, defaultPrefs))
        assertNull(AlarmPlanner.plan(deletedItem, baseNow, defaultPrefs))
    }

    @Test
    fun `snoozed item schedules exactly at snoozedUntil`() {
        val snoozeTime = baseNow.plus(Duration.ofMinutes(25))
        val item = testItem(snoozedUntil = snoozeTime)

        val plan = AlarmPlanner.plan(item = item, now = baseNow, prefs = defaultPrefs, nagCount = 2)

        assertNotNull(plan)
        assertEquals(snoozeTime, plan?.time)
        assertEquals(AlarmType.NAG, plan?.type)
    }

    @Test
    fun `max nags limit reached returns null`() {
        val item = testItem(
            dueAt = baseNow.minus(Duration.ofMinutes(30)),
            nag = NagPolicy(intervalMinutes = 10, maxNags = 3)
        )

        val planBeforeLimit = AlarmPlanner.plan(item = item, now = baseNow, prefs = defaultPrefs, nagCount = 2)
        assertNotNull(planBeforeLimit)

        val planAtLimit = AlarmPlanner.plan(item = item, now = baseNow, prefs = defaultPrefs, nagCount = 3)
        assertNull(planAtLimit)
    }

    @Test
    fun `nag interval respects NagPolicy`() {
        val interval = 15
        val item = testItem(
            dueAt = baseNow.minus(Duration.ofMinutes(10)),
            nag = NagPolicy(intervalMinutes = interval)
        )

        val plan = AlarmPlanner.plan(item = item, now = baseNow, prefs = defaultPrefs, nagCount = 1)

        assertNotNull(plan)
        val expectedTime = baseNow.plus(Duration.ofMinutes(interval.toLong()))
        assertEquals(expectedTime, plan?.time)
        assertEquals(AlarmType.NAG, plan?.type)
    }

    @Test
    fun `quiet hours defer nags across midnight to end of quiet hours`() {
        // Now is 23:30 UTC, quiet hours are 22:00 to 08:00
        val lateNightNow = Instant.parse("2026-11-08T23:30:00Z")
        val item = testItem(
            dueAt = lateNightNow.minus(Duration.ofHours(1)),
            timeZoneId = "UTC",
            nag = NagPolicy(intervalMinutes = 10, respectQuietHours = true),
            critical = false
        )

        val plan = AlarmPlanner.plan(item = item, now = lateNightNow, prefs = defaultPrefs, nagCount = 2)

        assertNotNull(plan)
        // Next nag would have been 23:40, but must defer to 2026-11-09T08:00:00Z
        val expectedDeferred = Instant.parse("2026-11-09T08:00:00Z")
        assertEquals(expectedDeferred, plan?.time)
    }

    @Test
    fun `critical item fires during quiet hours without deferring`() {
        val lateNightNow = Instant.parse("2026-11-08T23:30:00Z")
        val item = testItem(
            dueAt = lateNightNow.minus(Duration.ofHours(1)),
            nag = NagPolicy(intervalMinutes = 10, respectQuietHours = true),
            critical = true
        )

        val plan = AlarmPlanner.plan(item = item, now = lateNightNow, prefs = defaultPrefs, nagCount = 1)

        assertNotNull(plan)
        val expected = lateNightNow.plus(Duration.ofMinutes(10))
        assertEquals(expected, plan?.time)
    }

    @Test
    fun `critical item with nag count 3 or more escalates to FINAL_CALL`() {
        val item = testItem(
            dueAt = baseNow.minus(Duration.ofHours(1)),
            critical = true
        )

        val plan = AlarmPlanner.plan(item = item, now = baseNow, prefs = defaultPrefs, nagCount = 3)

        assertNotNull(plan)
        assertEquals(AlarmType.FINAL_CALL, plan?.type)
    }

    @Test
    fun `deadline item with startBy plans PRE_START if now is before startBy`() {
        val startBy = baseNow.plus(Duration.ofHours(1))
        val dueAt = baseNow.plus(Duration.ofDays(2))
        val item = testItem(
            kind = ItemKind.DEADLINE,
            startBy = startBy,
            dueAt = dueAt
        )

        val plan = AlarmPlanner.plan(item = item, now = baseNow, prefs = defaultPrefs, nagCount = 0)

        assertNotNull(plan)
        assertEquals(startBy, plan?.time)
        assertEquals(AlarmType.PRE_START, plan?.type)
    }
}
