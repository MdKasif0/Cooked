package com.cooked.app.feature.insights

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.testing.testUserPreferencesRepository
import com.cooked.app.data.db.CookedDatabase
import com.cooked.app.data.repository.ItemRepositoryImpl
import com.cooked.app.domain.insights.model.DayCompletion
import com.cooked.app.domain.insights.model.InsightRange
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemEvent
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.model.NagPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

private class InsightsMockClock(
    var currentInstant: Instant = Instant.parse("2026-11-08T12:00:00Z"),
    var currentZone: ZoneId = ZoneId.of("UTC")
) : CookedClock {
    override fun now(): Instant = currentInstant
    override fun zone(): ZoneId = currentZone
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InsightsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: CookedDatabase
    private lateinit var repository: ItemRepositoryImpl
    private lateinit var userPrefsRepo: UserPreferencesRepository
    private lateinit var clock: InsightsMockClock
    private lateinit var viewModel: InsightsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CookedDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor(testDispatcher.asExecutor())
            .setTransactionExecutor(testDispatcher.asExecutor())
            .build()
        clock = InsightsMockClock()
        repository = ItemRepositoryImpl(
            database = database,
            itemDao = database.itemDao(),
            checklistStepDao = database.checklistStepDao(),
            itemEventDao = database.itemEventDao(),
            clock = clock
        )
        userPrefsRepo = testUserPreferencesRepository(testDispatcher)

        viewModel = InsightsViewModel(
            analyticsDao = database.analyticsDao(),
            weeklySnapshotDao = database.weeklySnapshotDao(),
            itemRepository = repository,
            userPreferencesRepository = userPrefsRepo,
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
        completedAt: Instant? = null,
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
        nag = NagPolicy(intervalMinutes = 10, escalate = false, maxNags = null, respectQuietHours = true),
        critical = false,
        status = if (completedAt != null) ItemStatus.DONE else ItemStatus.ACTIVE,
        snoozedUntil = null,
        completedAt = completedAt,
        createdAt = dueAt.minus(Duration.ofHours(2)),
        updatedAt = dueAt.minus(Duration.ofHours(2)),
        deletedAt = null
    )

    @Test
    fun initialRangeIs7dAndNotSoftLocked() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(InsightRange.WEEK_7D, state.selectedRange)
        assertFalse(state.isSoftLocked)
        assertFalse(state.isLoading)
        assertNotNull(state.summary)
        assertEquals(0, state.summary?.doneCount)
    }

    @Test
    fun selectingProRangesSoftLocksWhenNotProUser() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.onRangeSelected(InsightRange.MONTH_30D)
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertEquals(InsightRange.MONTH_30D, state.selectedRange)
        assertTrue(state.isSoftLocked)

        viewModel.onRangeSelected(InsightRange.MONTHS_90D)
        advanceUntilIdle()
        state = viewModel.uiState.value
        assertTrue(state.isSoftLocked)

        viewModel.onRangeSelected(InsightRange.WEEK_7D)
        advanceUntilIdle()
        state = viewModel.uiState.value
        assertFalse(state.isSoftLocked)
    }

    @Test
    fun selectingDayCompletionUpdatesState() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        val sampleDay = DayCompletion(
            date = LocalDate.of(2026, 11, 8),
            count = 3
        )
        viewModel.onSelectDayCompletion(sampleDay)
        advanceUntilIdle()

        assertEquals(sampleDay, viewModel.uiState.value.selectedDayCompletion)

        viewModel.onSelectDayCompletion(null)
        advanceUntilIdle()
        assertEquals(null, viewModel.uiState.value.selectedDayCompletion)
    }

    @Test
    fun toggleCookedReportDialogUpdatesState() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showCookedReportDialog)

        viewModel.setShowCookedReport(true)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showCookedReportDialog)

        viewModel.setShowCookedReport(false)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showCookedReportDialog)
    }

    @Test
    fun completedItemsPopulateSummaryStats() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }

        // Insert items completed within the last 7 days
        val now = clock.now()
        val item1 = createItem("Assignment 1", dueAt = now.minus(Duration.ofHours(5)), completedAt = now.minus(Duration.ofHours(6)))
        val item2 = createItem("Assignment 2", dueAt = now.minus(Duration.ofHours(3)), completedAt = now.minus(Duration.ofHours(1)))
        repository.create(item1)
        repository.create(item2)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.summary)
        assertEquals(2, state.summary?.doneCount)
        assertEquals(0.5, state.summary?.onTimeRate ?: 0.0, 0.001)
    }
}
