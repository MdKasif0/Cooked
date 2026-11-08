package com.cooked.app.feature.home

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.data.db.CookedDatabase
import com.cooked.app.data.repository.ItemRepositoryImpl
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.model.NagPolicy
import com.cooked.app.scheduling.battery.ReliabilityChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

private class TodayMockClock(
    var currentInstant: Instant = Instant.parse("2026-11-08T10:00:00Z"),
    var currentZone: ZoneId = ZoneId.of("UTC")
) : CookedClock {
    override fun now(): Instant = currentInstant
    override fun zone(): ZoneId = currentZone
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TodayViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: CookedDatabase
    private lateinit var repository: ItemRepositoryImpl
    private lateinit var userPrefsRepo: UserPreferencesRepository
    private lateinit var clock: TodayMockClock
    private lateinit var viewModel: TodayViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CookedDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor(testDispatcher.asExecutor())
            .setTransactionExecutor(testDispatcher.asExecutor())
            .build()
        clock = TodayMockClock()
        repository = ItemRepositoryImpl(
            database = database,
            itemDao = database.itemDao(),
            checklistStepDao = database.checklistStepDao(),
            itemEventDao = database.itemEventDao(),
            clock = clock
        )
        userPrefsRepo = UserPreferencesRepository(context)

        viewModel = TodayViewModel(
            itemRepository = repository,
            userPreferencesRepository = userPrefsRepo,
            reliabilityChecker = ReliabilityChecker(context),
            clock = clock
        )
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    private fun createItem(
        title: String,
        dueAt: Instant,
        kind: ItemKind = ItemKind.REMINDER
    ): Item = Item(
        id = UUID.randomUUID().toString(),
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
        status = ItemStatus.ACTIVE,
        snoozedUntil = null,
        completedAt = null,
        effortMinutes = null,
        progress = 0,
        checklist = emptyList(),
        createdAt = clock.now(),
        updatedAt = clock.now()
    )

    @Test
    fun `empty state gives calm voice status`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("Nothing due. Suspiciously calm.", state.voiceStatus)
        assertTrue(state.isEmpty)
    }

    @Test
    fun `items categorized into overdue, now, and later`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        val now = clock.now()
        val overdueItem = createItem("Overdue assignment", now.minus(Duration.ofHours(2)))
        val nowItem = createItem("Call dentist", now.plus(Duration.ofMinutes(20)))
        val laterItem = createItem("Evening review", now.plus(Duration.ofHours(5)))

        repository.create(overdueItem)
        repository.create(nowItem)
        repository.create(laterItem)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.overdueItems.size)
        assertEquals("Overdue assignment", state.overdueItems[0].title)

        assertEquals(1, state.nowItems.size)
        assertEquals("Call dentist", state.nowItems[0].title)

        assertEquals(1, state.laterTodayItems.size)
        assertEquals("Evening review", state.laterTodayItems[0].title)

        assertEquals(3, state.totalTodayCount)
        assertTrue(state.voiceStatus.contains("overdue"))
    }

    @Test
    fun `marking item done removes it from active list`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        val item = createItem("Submit PR", clock.now().plus(Duration.ofMinutes(30)))
        repository.create(item)
        advanceUntilIdle()

        viewModel.onDone(item.id)
        advanceUntilIdle()

        val active = repository.getActiveItems()
        assertEquals(0, active.size)
    }

    @Test
    fun `multi-select toggling and batch done`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        val item1 = createItem("Task 1", clock.now().plus(Duration.ofMinutes(10)))
        val item2 = createItem("Task 2", clock.now().plus(Duration.ofMinutes(20)))
        repository.create(item1)
        repository.create(item2)
        advanceUntilIdle()

        viewModel.toggleSelection(item1.id)
        viewModel.toggleSelection(item2.id)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isMultiSelectMode)
        assertEquals(2, viewModel.uiState.value.selectedItemIds.size)

        viewModel.onBatchDone()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isMultiSelectMode)
        assertEquals(0, repository.getActiveItems().size)
    }
}
