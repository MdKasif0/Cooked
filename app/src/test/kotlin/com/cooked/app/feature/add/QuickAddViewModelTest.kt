package com.cooked.app.feature.add

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.data.db.CookedDatabase
import com.cooked.app.data.repository.ItemRepositoryImpl
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.Recurrence
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.ZoneId

private class MockCookedClock(
    var currentInstant: Instant = Instant.parse("2026-11-08T10:00:00Z"),
    var currentZone: ZoneId = ZoneId.of("UTC")
) : CookedClock {
    override fun now(): Instant = currentInstant
    override fun zone(): ZoneId = currentZone
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QuickAddViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: CookedDatabase
    private lateinit var repository: ItemRepositoryImpl
    private lateinit var userPrefsRepo: UserPreferencesRepository
    private lateinit var clock: MockCookedClock
    private lateinit var viewModel: QuickAddViewModel

    @Before
    fun setup() {
        QuickAddViewModel.clearDraftCacheForTesting()
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CookedDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor(testDispatcher.asExecutor())
            .setTransactionExecutor(testDispatcher.asExecutor())
            .build()
        clock = MockCookedClock()
        repository = ItemRepositoryImpl(
            database = database,
            itemDao = database.itemDao(),
            checklistStepDao = database.checklistStepDao(),
            itemEventDao = database.itemEventDao(),
            clock = clock
        )
        userPrefsRepo = UserPreferencesRepository(context)

        viewModel = QuickAddViewModel(
            itemRepository = repository,
            userPreferencesRepository = userPrefsRepo,
            clock = clock
        )
    }

    @After
    fun tearDown() {
        QuickAddViewModel.clearDraftCacheForTesting()
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `typing text runs parser and updates live state`() = runTest(testDispatcher) {
        viewModel.onTextChanged("submit physics assignment due fri 5pm 2h !!")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("submit physics assignment", state.effectiveTitle)
        assertEquals(ItemKind.DEADLINE, state.effectiveKind)
        assertEquals(120, state.effectiveEffortMinutes)
        assertTrue(state.effectiveCritical)
        assertNotNull(state.effectiveDueAt)
        assertTrue(state.canSave)
    }

    @Test
    fun `quick chip overrides parsed due time`() = runTest(testDispatcher) {
        viewModel.onTextChanged("read book")
        advanceUntilIdle()

        viewModel.onQuickChipSelected(QuickTimeChip.PLUS_10M)
        val expected = clock.now().plus(java.time.Duration.ofMinutes(10))
        assertEquals(expected, viewModel.uiState.value.effectiveDueAt)
    }

    @Test
    fun `toggle deadline switches kind between reminder and deadline`() = runTest(testDispatcher) {
        viewModel.onTextChanged("call mom")
        advanceUntilIdle()
        assertEquals(ItemKind.REMINDER, viewModel.uiState.value.effectiveKind)

        viewModel.onToggleDeadline()
        assertEquals(ItemKind.DEADLINE, viewModel.uiState.value.effectiveKind)

        viewModel.onToggleDeadline()
        assertEquals(ItemKind.REMINDER, viewModel.uiState.value.effectiveKind)
    }

    @Test
    fun `save creates item in repository and clears draft`() = runTest(testDispatcher) {
        viewModel.onTextChanged("pay electricity bill tomorrow 10am")
        advanceUntilIdle()

        viewModel.save()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSaved)
        assertNotNull(state.savedItem)

        val items = repository.getActiveItems()
        assertEquals(1, items.size)
        assertEquals("pay electricity bill", items[0].title)
    }

    @Test
    fun `undo last save deletes item from repository`() = runTest(testDispatcher) {
        viewModel.onTextChanged("stretch +15m")
        advanceUntilIdle()

        viewModel.save()
        advanceUntilIdle()
        assertEquals(1, repository.getActiveItems().size)

        viewModel.undoLastSave()
        advanceUntilIdle()
        assertEquals(0, repository.getActiveItems().size)
    }
}
