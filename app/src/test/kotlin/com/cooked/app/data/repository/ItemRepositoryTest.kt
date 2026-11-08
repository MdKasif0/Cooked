package com.cooked.app.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.db.CookedDatabase
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.model.Recurrence
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

class TestCookedClock(
    var currentInstant: Instant = Instant.parse("2026-05-15T10:00:00Z"),
    var currentZone: ZoneId = ZoneId.of("UTC")
) : CookedClock {
    override fun now(): Instant = currentInstant
    override fun zone(): ZoneId = currentZone
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ItemRepositoryTest {

    private lateinit var database: CookedDatabase
    private lateinit var clock: TestCookedClock
    private lateinit var repository: ItemRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CookedDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        clock = TestCookedClock()
        repository = ItemRepositoryImpl(
            database = database,
            itemDao = database.itemDao(),
            checklistStepDao = database.checklistStepDao(),
            itemEventDao = database.itemEventDao(),
            clock = clock
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun create_persistsItem_logsCreatedEvent_andEmitsItemChanged() = runTest {
        val item = Item(
            id = UUID.randomUUID().toString(),
            kind = ItemKind.REMINDER,
            title = "Drink Water",
            dueAt = clock.now().plus(1, ChronoUnit.HOURS)
        )

        repository.itemChanged.test {
            repository.create(item)
            val emittedId = awaitItem()
            assertThat(emittedId).isEqualTo(item.id)
        }

        val loaded = repository.getItem(item.id)
        assertThat(loaded).isNotNull()
        assertThat(loaded!!.title).isEqualTo("Drink Water")

        val events = database.itemEventDao().getEventsInRange(clock.now().minus(1, ChronoUnit.HOURS), clock.now().plus(1, ChronoUnit.HOURS))
        assertThat(events).hasSize(1)
        assertThat(events[0].type).isEqualTo(ItemEventType.CREATED)
        assertThat(events[0].itemId).isEqualTo(item.id)
    }

    @Test
    fun markDone_recurringItem_rollsDueAt_doesNotCreateDuplicate_andLogsDoneEvent() = runTest {
        val initialDueAt = clock.now()
        val item = Item(
            id = UUID.randomUUID().toString(),
            kind = ItemKind.REMINDER,
            title = "Daily Meditation",
            dueAt = initialDueAt,
            recurrence = Recurrence.Daily
        )
        repository.create(item)

        // Mark done at initialDueAt + 15 minutes (late by 15 mins)
        val completedAt = initialDueAt.plus(15, ChronoUnit.MINUTES)
        repository.markDone(item.id, at = completedAt)

        // 1. Verify item rolled dueAt to next day
        val updated = repository.getItem(item.id)
        assertThat(updated).isNotNull()
        val expectedNextDue = initialDueAt.plus(1, ChronoUnit.DAYS)
        assertThat(updated!!.dueAt).isEqualTo(expectedNextDue)
        assertThat(updated.status).isEqualTo(ItemStatus.ACTIVE)
        assertThat(updated.completedAt).isNull() // resets completedAt for next cycle

        // 2. Verify NO duplicate rows created
        val allItems = database.itemDao().getAllNonDeletedItems()
        assertThat(allItems).hasSize(1)

        // 3. Verify DONE event was logged with lateByMinutes and previousDueAt
        val latestDoneEvent = database.itemEventDao().getLatestEventFor(item.id, ItemEventType.DONE)
        assertThat(latestDoneEvent).isNotNull()
        assertThat(latestDoneEvent!!.type).isEqualTo(ItemEventType.DONE)
        assertThat(latestDoneEvent.meta["recurring"]).isEqualTo("true")
        assertThat(latestDoneEvent.meta["previousDueAt"]).isEqualTo(initialDueAt.toString())
        assertThat(latestDoneEvent.meta["lateByMinutes"]).isEqualTo("15")
    }

    @Test
    fun undoDone_recurringItem_restoresPreviousDueAt_andLogsUndoneEvent() = runTest {
        val initialDueAt = clock.now()
        val item = Item(
            id = UUID.randomUUID().toString(),
            kind = ItemKind.REMINDER,
            title = "Daily Workout",
            dueAt = initialDueAt,
            recurrence = Recurrence.Daily
        )
        repository.create(item)
        repository.markDone(item.id, at = initialDueAt)

        // Undo done
        repository.undoDone(item.id)

        val restored = repository.getItem(item.id)
        assertThat(restored).isNotNull()
        assertThat(restored!!.dueAt).isEqualTo(initialDueAt)

        val latestEvent = database.itemEventDao().getLatestEventFor(item.id, ItemEventType.UNDONE)
        assertThat(latestEvent).isNotNull()
        assertThat(latestEvent!!.type).isEqualTo(ItemEventType.UNDONE)
    }

    @Test
    fun markDone_nonRecurringItem_setsDoneStatus() = runTest {
        val item = Item(
            id = UUID.randomUUID().toString(),
            kind = ItemKind.DEADLINE,
            title = "Final Project Submission",
            dueAt = clock.now().plus(2, ChronoUnit.HOURS)
        )
        repository.create(item)
        repository.markDone(item.id, at = clock.now())

        val completed = repository.getItem(item.id)
        assertThat(completed).isNotNull()
        assertThat(completed!!.status).isEqualTo(ItemStatus.DONE)
        assertThat(completed.completedAt).isNotNull()
    }

    @Test
    fun snooze_updatesSnoozedUntil_andLogsSnoozedEvent() = runTest {
        val item = Item(
            id = UUID.randomUUID().toString(),
            kind = ItemKind.REMINDER,
            title = "Read Book Chapter",
            dueAt = clock.now()
        )
        repository.create(item)

        val snoozeUntil = clock.now().plus(30, ChronoUnit.MINUTES)
        repository.snooze(item.id, until = snoozeUntil)

        val snoozed = repository.getItem(item.id)
        assertThat(snoozed).isNotNull()
        assertThat(snoozed!!.snoozedUntil).isEqualTo(snoozeUntil)

        val event = database.itemEventDao().getLatestEventFor(item.id, ItemEventType.SNOOZED)
        assertThat(event).isNotNull()
        assertThat(event!!.meta["snoozeMinutes"]).isEqualTo("30")
    }

    @Test
    fun delete_softDeletesItem_excludesFromActiveQueries() = runTest {
        val item = Item(
            id = UUID.randomUUID().toString(),
            kind = ItemKind.REMINDER,
            title = "Discarded Reminder",
            dueAt = clock.now()
        )
        repository.create(item)
        repository.delete(item.id)

        val activeList = repository.observeToday().first()
        assertThat(activeList.map { it.id }).doesNotContain(item.id)

        val rawLoaded = database.itemDao().getItemWithChecklist(item.id)
        assertThat(rawLoaded!!.item.deletedAt).isNotNull()
    }
}

