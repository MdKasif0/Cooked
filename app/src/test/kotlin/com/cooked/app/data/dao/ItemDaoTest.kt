package com.cooked.app.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.cooked.app.data.db.AnalyticsQueryService
import com.cooked.app.data.db.CookedDatabase
import com.cooked.app.data.entity.ChecklistStepEntity
import com.cooked.app.data.entity.ItemEntity
import com.cooked.app.data.entity.ItemEventEntity
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.model.NagPolicy
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ItemDaoTest {

    private lateinit var database: CookedDatabase
    private lateinit var itemDao: ItemDao
    private lateinit var checklistStepDao: ChecklistStepDao
    private lateinit var itemEventDao: ItemEventDao
    private lateinit var analyticsDao: AnalyticsDao
    private lateinit var analyticsService: AnalyticsQueryService

    private val now = Instant.parse("2026-05-15T12:00:00Z")
    private val zoneId = ZoneId.of("UTC")

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CookedDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        itemDao = database.itemDao()
        checklistStepDao = database.checklistStepDao()
        itemEventDao = database.itemEventDao()
        analyticsDao = database.analyticsDao()
        analyticsService = AnalyticsQueryService(analyticsDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createSampleItem(
        id: String = UUID.randomUUID().toString(),
        kind: ItemKind = ItemKind.REMINDER,
        title: String = "Test Item",
        dueAt: Instant = now.plus(2, ChronoUnit.HOURS),
        status: ItemStatus = ItemStatus.ACTIVE,
        deletedAt: Instant? = null,
        completedAt: Instant? = null
    ): ItemEntity = ItemEntity(
        id = id,
        kind = kind,
        title = title,
        note = null,
        emoji = null,
        dueAt = dueAt,
        allDay = false,
        timeZoneId = "UTC",
        recurrence = null,
        nag = NagPolicy(),
        critical = false,
        status = status,
        snoozedUntil = null,
        completedAt = completedAt,
        createdAt = now.minus(1, ChronoUnit.DAYS),
        updatedAt = now.minus(1, ChronoUnit.DAYS),
        deletedAt = deletedAt,
        startBy = null,
        effortMinutes = null,
        progress = 0
    )

    @Test
    fun observeActiveItems_excludesSoftDeletedAndCompletedItems() = runTest {
        val activeItem = createSampleItem(title = "Active Reminder")
        val completedItem = createSampleItem(title = "Completed", status = ItemStatus.DONE)
        val deletedItem = createSampleItem(title = "Deleted", deletedAt = now)

        itemDao.upsertItems(listOf(activeItem, completedItem, deletedItem))

        val result = itemDao.observeActiveItems().first()
        assertThat(result.map { it.item.id }).containsExactly(activeItem.id)
    }

    @Test
    fun observeToday_returnsOnlyItemsDueToday() = runTest {
        val startOfDay = Instant.parse("2026-05-15T00:00:00Z")
        val endOfDay = Instant.parse("2026-05-15T23:59:59Z")

        val dueToday = createSampleItem(dueAt = Instant.parse("2026-05-15T15:00:00Z"))
        val dueTomorrow = createSampleItem(dueAt = Instant.parse("2026-05-16T10:00:00Z"))

        itemDao.upsertItems(listOf(dueToday, dueTomorrow))

        val result = itemDao.observeToday(startOfDay, endOfDay).first()
        assertThat(result.map { it.item.id }).containsExactly(dueToday.id)
    }

    @Test
    fun observeOverdue_returnsItemsWithDueAtBeforeNow() = runTest {
        val overdueItem = createSampleItem(dueAt = now.minus(2, ChronoUnit.HOURS))
        val futureItem = createSampleItem(dueAt = now.plus(2, ChronoUnit.HOURS))

        itemDao.upsertItems(listOf(overdueItem, futureItem))

        val result = itemDao.observeOverdue(now).first()
        assertThat(result.map { it.item.id }).containsExactly(overdueItem.id)
    }

    @Test
    fun observeDeadlinesByUrgency_returnsOnlyDeadlinesSortedByDue() = runTest {
        val deadlineLater = createSampleItem(
            kind = ItemKind.DEADLINE,
            dueAt = now.plus(5, ChronoUnit.DAYS)
        )
        val deadlineSooner = createSampleItem(
            kind = ItemKind.DEADLINE,
            dueAt = now.plus(1, ChronoUnit.DAYS)
        )
        val regularReminder = createSampleItem(
            kind = ItemKind.REMINDER,
            dueAt = now.plus(1, ChronoUnit.HOURS)
        )

        itemDao.upsertItems(listOf(deadlineLater, deadlineSooner, regularReminder))

        val result = itemDao.observeDeadlinesByUrgency().first()
        assertThat(result.map { it.item.id }).containsExactly(deadlineSooner.id, deadlineLater.id).inOrder()
    }

    @Test
    fun observeItemWithChecklist_loadsChecklistSteps() = runTest {
        val item = createSampleItem(kind = ItemKind.DEADLINE)
        itemDao.upsertItem(item)

        val step1 = ChecklistStepEntity(UUID.randomUUID().toString(), item.id, "Step 1", true, 0, now)
        val step2 = ChecklistStepEntity(UUID.randomUUID().toString(), item.id, "Step 2", false, 1, now)
        checklistStepDao.upsertSteps(listOf(step1, step2))

        val loaded = itemDao.observeItemWithChecklist(item.id).first()
        assertThat(loaded).isNotNull()
        assertThat(loaded!!.checklist).hasSize(2)
        assertThat(loaded.checklist[0].text).isEqualTo("Step 1")
    }

    @Test
    fun softDeleteAndRestore_togglesItemVisibility() = runTest {
        val item = createSampleItem()
        itemDao.upsertItem(item)

        // Soft delete
        itemDao.softDelete(item.id, deletedAt = now, updatedAt = now)
        val activeAfterDelete = itemDao.observeActiveItems().first()
        assertThat(activeAfterDelete).isEmpty()

        // Restore
        itemDao.restore(item.id, updatedAt = now)
        val activeAfterRestore = itemDao.observeActiveItems().first()
        assertThat(activeAfterRestore.map { it.item.id }).containsExactly(item.id)
    }

    @Test
    fun rollRecurringOccurrence_updatesDueAtAndResetsSnooze() = runTest {
        val item = createSampleItem(dueAt = now)
        itemDao.upsertItem(item)

        val nextDue = now.plus(1, ChronoUnit.DAYS)
        itemDao.rollRecurringOccurrence(item.id, nextDue, updatedAt = now)

        val loaded = itemDao.getItemWithChecklist(item.id)
        assertThat(loaded!!.item.dueAt).isEqualTo(nextDue)
        assertThat(loaded.item.status).isEqualTo(ItemStatus.ACTIVE)
        assertThat(loaded.item.snoozedUntil).isNull()
    }

    @Test
    fun analyticsQueries_calculateMetricsCorrectly() = runTest {
        val from = now.minus(7, ChronoUnit.DAYS)
        val to = now.plus(1, ChronoUnit.DAYS)

        // Seed: 2 items completed on time, 1 completed late by 60 mins, 1 completed late by 120 mins
        val itemOnTime1 = createSampleItem(
            dueAt = now.minus(3, ChronoUnit.DAYS),
            status = ItemStatus.DONE,
            completedAt = now.minus(3, ChronoUnit.DAYS).minus(30, ChronoUnit.MINUTES)
        )
        val itemOnTime2 = createSampleItem(
            dueAt = now.minus(2, ChronoUnit.DAYS),
            status = ItemStatus.DONE,
            completedAt = now.minus(2, ChronoUnit.DAYS)
        )
        val itemLate1 = createSampleItem(
            dueAt = now.minus(1, ChronoUnit.DAYS),
            status = ItemStatus.DONE,
            completedAt = now.minus(1, ChronoUnit.DAYS).plus(60, ChronoUnit.MINUTES)
        )
        val itemLate2 = createSampleItem(
            dueAt = now.minus(1, ChronoUnit.DAYS),
            status = ItemStatus.DONE,
            completedAt = now.minus(1, ChronoUnit.DAYS).plus(120, ChronoUnit.MINUTES)
        )
        itemDao.upsertItems(listOf(itemOnTime1, itemOnTime2, itemLate1, itemLate2))

        // Seed events: 4 DONE events, 3 NAGGED events on itemLate1, 2 SNOOZE events at 14:00
        val doneEvents = listOf(itemOnTime1, itemOnTime2, itemLate1, itemLate2).map {
            ItemEventEntity(UUID.randomUUID().toString(), it.id, ItemEventType.DONE, it.completedAt!!, emptyMap())
        }
        val nagEvents = listOf(
            ItemEventEntity(UUID.randomUUID().toString(), itemLate1.id, ItemEventType.NAGGED, now.minus(1, ChronoUnit.DAYS).plus(10, ChronoUnit.MINUTES), emptyMap()),
            ItemEventEntity(UUID.randomUUID().toString(), itemLate1.id, ItemEventType.NAGGED, now.minus(1, ChronoUnit.DAYS).plus(20, ChronoUnit.MINUTES), emptyMap()),
            ItemEventEntity(UUID.randomUUID().toString(), itemLate1.id, ItemEventType.NAGGED, now.minus(1, ChronoUnit.DAYS).plus(30, ChronoUnit.MINUTES), emptyMap())
        )
        val snoozeTime14 = Instant.parse("2026-05-14T14:15:00Z")
        val snoozeEvents = listOf(
            ItemEventEntity(UUID.randomUUID().toString(), itemLate1.id, ItemEventType.SNOOZED, snoozeTime14, emptyMap()),
            ItemEventEntity(UUID.randomUUID().toString(), itemLate2.id, ItemEventType.SNOOZED, snoozeTime14, emptyMap())
        )
        itemEventDao.insertEvents(doneEvents + nagEvents + snoozeEvents)

        // 1. On time vs Late stats
        val onTimeStats = analyticsService.getOnTimeVsLateCompletions(from, to)
        assertThat(onTimeStats.onTimeCount).isEqualTo(2)
        assertThat(onTimeStats.lateCount).isEqualTo(2)

        // 2. Median lateness: [60 mins, 120 mins] -> median is (60 + 120) / 2 = 90.0
        val medianLateness = analyticsService.getMedianLatenessMinutes(from, to)
        assertThat(medianLateness).isEqualTo(90.0)

        // 3. Average nags before done: 3 nags across 4 completed items = 0.75
        val avgNags = analyticsService.getAverageNagsBeforeDone(from, to)
        assertThat(avgNags).isEqualTo(0.75)

        // 4. Snooze counts by hour of day: hour 14 has 2 snoozes
        val snoozeByHour = analyticsService.getSnoozeCountsByHourOfDay(from, to, zoneId)
        assertThat(snoozeByHour[14]).isEqualTo(2)
        assertThat(snoozeByHour[10]).isEqualTo(0)

        // 5. Done count per day
        val doneDays = analyticsService.getDoneCountPerDay(from, to)
        assertThat(doneDays).isNotEmpty()
    }
}

