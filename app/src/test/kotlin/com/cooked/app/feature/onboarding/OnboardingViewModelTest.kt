package com.cooked.app.feature.onboarding

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.data.db.CookedDatabase
import com.cooked.app.data.repository.ItemRepositoryImpl
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.Personality
import com.cooked.app.domain.usecase.CreateItemFromExternal
import com.cooked.app.scheduling.alarm.AlarmScheduler
import com.cooked.app.scheduling.alarm.ScheduledAlarmInfo
import com.cooked.app.scheduling.battery.ReliabilityChecker
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var database: CookedDatabase
    private lateinit var userPrefsRepo: UserPreferencesRepository
    private lateinit var repository: ItemRepositoryImpl
    private lateinit var alarmScheduler: FakeAlarmScheduler
    private lateinit var reliabilityChecker: ReliabilityChecker
    private lateinit var createItemFromExternal: CreateItemFromExternal
    private lateinit var viewModel: OnboardingViewModel

    private class FakeAlarmScheduler : AlarmScheduler {
        override suspend fun schedule(item: Item, nagCount: Int?) {}
        override suspend fun schedule(itemId: String) {}
        override fun cancel(itemId: String) {}
        override suspend fun rescheduleAll() {}
        override fun getScheduledAlarms(): List<ScheduledAlarmInfo> = emptyList()
        override suspend fun fireNow(itemId: String) {}
    }

    private class FixedClock : CookedClock {
        override fun now(): Instant = Instant.parse("2026-11-08T10:00:00Z")
        override fun zone(): ZoneId = ZoneId.of("UTC")
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, CookedDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val clock = FixedClock()
        userPrefsRepo = UserPreferencesRepository(context)
        repository = ItemRepositoryImpl(
            database = database,
            itemDao = database.itemDao(),
            checklistStepDao = database.checklistStepDao(),
            itemEventDao = database.itemEventDao(),
            clock = clock
        )
        alarmScheduler = FakeAlarmScheduler()
        reliabilityChecker = ReliabilityChecker(context)
        createItemFromExternal = CreateItemFromExternal(
            itemRepository = repository,
            alarmScheduler = alarmScheduler,
            userPreferencesRepository = userPrefsRepo,
            clock = clock
        )

        viewModel = OnboardingViewModel(
            userPreferencesRepository = userPrefsRepo,
            createItemFromExternal = createItemFromExternal,
            reliabilityChecker = reliabilityChecker,
            itemEventDao = database.itemEventDao()
        )
    }

    @After
    fun teardown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_startsAtStepZero() {
        val state = viewModel.uiState.value
        assertThat(state.currentStep).isEqualTo(0)
        assertThat(state.totalSteps).isEqualTo(5)
        assertThat(state.isComplete).isFalse()
    }

    private fun waitUntil(timeoutMs: Long = 3000, condition: () -> Boolean) {
        val start = System.currentTimeMillis()
        while (!condition() && System.currentTimeMillis() - start < timeoutMs) {
            Thread.sleep(20)
            testDispatcher.scheduler.advanceUntilIdle()
        }
    }

    @Test
    fun onNext_incrementsStepsUntilCompletion() = runTest(testDispatcher) {
        assertThat(viewModel.uiState.value.currentStep).isEqualTo(0)

        viewModel.onNext()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.currentStep).isEqualTo(1)

        viewModel.onNext()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.currentStep).isEqualTo(2)

        viewModel.onNext()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.currentStep).isEqualTo(3)

        viewModel.onNext()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.currentStep).isEqualTo(4)

        // Step 4 is the last step (0, 1, 2, 3, 4 = 5 steps). Calling onNext() completes onboarding.
        viewModel.onNext()
        waitUntil { viewModel.uiState.value.isComplete }

        assertThat(viewModel.uiState.value.isComplete).isTrue()
    }

    @Test
    fun onSkip_immediatelyCompletesOnboarding() = runTest(testDispatcher) {
        viewModel.onSkip()
        waitUntil { viewModel.uiState.value.isComplete }

        assertThat(viewModel.uiState.value.isComplete).isTrue()
    }

    @Test
    fun onPersonalitySelected_updatesStateAndPreferences() = runTest(testDispatcher) {
        viewModel.onPersonalitySelected(Personality.UNHINGED)
        waitUntil { viewModel.uiState.value.selectedPersonality == Personality.UNHINGED }

        assertThat(viewModel.uiState.value.selectedPersonality).isEqualTo(Personality.UNHINGED)
    }

    @Test
    fun saveFirstWin_createsItemAndMarksCelebration() = runTest(testDispatcher) {
        viewModel.saveFirstWin("Submit project report tomorrow 5pm")
        waitUntil { viewModel.uiState.value.firstWinCreated }

        val state = viewModel.uiState.value
        assertThat(state.firstWinCreated).isTrue()
        assertThat(state.createdItem).isNotNull()
        assertThat(state.createdItem?.title).contains("Submit project report")
    }
}
