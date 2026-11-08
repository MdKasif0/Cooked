package com.cooked.app.ui.addedit

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.data.db.CookedDatabase
import com.cooked.app.data.repository.ItemRepositoryImpl
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.NagPolicy
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

private class FixedClock(private val instant: Instant) : CookedClock {
    override fun now(): Instant = instant
    override fun zone(): ZoneId = ZoneId.of("UTC")
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AddEditReminderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val now = Instant.parse("2026-11-08T12:00:00Z")
    private lateinit var database: CookedDatabase
    private lateinit var repository: ItemRepositoryImpl
    private lateinit var prefs: UserPreferencesRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CookedDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor(testDispatcher.asExecutor())
            .setTransactionExecutor(testDispatcher.asExecutor())
            .build()
        repository = ItemRepositoryImpl(
            database = database,
            itemDao = database.itemDao(),
            checklistStepDao = database.checklistStepDao(),
            itemEventDao = database.itemEventDao(),
            clock = FixedClock(now)
        )
        prefs = UserPreferencesRepository(context)
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    private fun viewModel(id: String?) = AddEditReminderViewModel(
        itemRepository = repository,
        userPreferencesRepository = prefs,
        clock = FixedClock(now),
        savedStateHandle = SavedStateHandle(if (id != null) mapOf("reminderId" to id) else emptyMap())
    )

    @Test
    fun editingExistingItem_loadsItAndSavesChangesToItemsTable() = runTest(testDispatcher) {
        val original = Item(
            id = "item-1",
            kind = ItemKind.REMINDER,
            title = "Pay rent",
            note = "landlord",
            dueAt = now.plus(Duration.ofHours(3)),
            timeZoneId = "UTC",
            nag = NagPolicy(intervalMinutes = 15, escalate = true),
            critical = true,
            snoozedUntil = now.plus(Duration.ofHours(1)),
            createdAt = now,
            updatedAt = now
        )
        repository.create(original)
        advanceUntilIdle()

        val vm = viewModel("item-1")
        advanceUntilIdle()

        assertThat(vm.uiState.value.title).isEqualTo("Pay rent")
        assertThat(vm.uiState.value.note).isEqualTo("landlord")
        assertThat(vm.uiState.value.nagIntervalMinutes).isEqualTo(15)
        assertThat(vm.uiState.value.canSave).isTrue()

        val newDue = now.plus(Duration.ofDays(1))
        vm.onTitleChange("Pay rent (Nov)")
        vm.setCustomDueInstant(newDue.toEpochMilli())
        vm.save()
        advanceUntilIdle()

        val saved = repository.getItem("item-1")!!
        assertThat(vm.uiState.value.isSaved).isTrue()
        assertThat(saved.title).isEqualTo("Pay rent (Nov)")
        assertThat(saved.dueAt).isEqualTo(newDue)
        // Fields the form doesn't show must survive the edit
        assertThat(saved.critical).isTrue()
        assertThat(saved.nag.escalate).isTrue()
        // A new due time clears the old snooze
        assertThat(saved.snoozedUntil).isNull()
    }

    @Test
    fun newItem_isCreatedInItemsTable() = runTest(testDispatcher) {
        val vm = viewModel(null)
        advanceUntilIdle()

        vm.onTitleChange("Submit essay")
        vm.onDeadlineModeToggle(true)
        vm.onEffortMinutesChange(120)
        vm.save()
        advanceUntilIdle()

        val items = repository.getActiveItems()
        assertThat(items).hasSize(1)
        assertThat(items.single().title).isEqualTo("Submit essay")
        assertThat(items.single().kind).isEqualTo(ItemKind.DEADLINE)
        assertThat(items.single().effortMinutes).isEqualTo(120)
    }

    @Test
    fun delete_softDeletesTheItem() = runTest(testDispatcher) {
        repository.create(
            Item(id = "gone", kind = ItemKind.REMINDER, title = "Old", dueAt = now.plus(Duration.ofHours(1)))
        )
        advanceUntilIdle()

        val vm = viewModel("gone")
        advanceUntilIdle()
        vm.delete()
        advanceUntilIdle()

        assertThat(repository.getItem("gone")!!.isDeleted).isTrue()
    }
}
