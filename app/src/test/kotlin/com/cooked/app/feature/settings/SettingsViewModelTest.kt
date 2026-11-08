package com.cooked.app.feature.settings

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.cooked.app.core.designsystem.ThemeMode
import com.cooked.app.core.util.SampleDataSeeder
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.data.db.CookedDatabase
import com.cooked.app.domain.backup.BackupManager
import com.cooked.app.domain.backup.CookedBackupData
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.model.Personality
import com.cooked.app.scheduling.alarm.AlarmScheduler
import com.cooked.app.scheduling.alarm.ScheduledAlarmInfo
import com.cooked.app.scheduling.battery.ReliabilityChecker
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var database: CookedDatabase
    private lateinit var userPrefsRepo: UserPreferencesRepository
    private lateinit var backupManager: BackupManager
    private lateinit var alarmScheduler: FakeAlarmScheduler
    private lateinit var reliabilityChecker: ReliabilityChecker
    private lateinit var sampleDataSeeder: SampleDataSeeder
    private lateinit var viewModel: SettingsViewModel

    private class FakeAlarmScheduler : AlarmScheduler {
        override suspend fun schedule(item: Item, nagCount: Int?) {}
        override suspend fun schedule(itemId: String) {}
        override fun cancel(itemId: String) {}
        override suspend fun rescheduleAll() {}
        override fun getScheduledAlarms(): List<ScheduledAlarmInfo> = emptyList()
        override suspend fun fireNow(itemId: String) {}
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, CookedDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userPrefsRepo = UserPreferencesRepository(context)
        alarmScheduler = FakeAlarmScheduler()
        reliabilityChecker = ReliabilityChecker(context)
        backupManager = BackupManager(
            context = context,
            database = database,
            alarmScheduler = alarmScheduler,
            userPreferencesRepository = userPrefsRepo
        )
        sampleDataSeeder = SampleDataSeeder(
            database = database,
            itemDao = database.itemDao(),
            checklistStepDao = database.checklistStepDao(),
            itemEventDao = database.itemEventDao()
        )

        viewModel = SettingsViewModel(
            preferencesRepository = userPrefsRepo,
            backupManager = backupManager,
            alarmScheduler = alarmScheduler,
            reliabilityChecker = reliabilityChecker,
            sampleDataSeeder = sampleDataSeeder
        )
    }

    @After
    fun teardown() {
        database.close()
        Dispatchers.resetMain()
    }

    private fun waitUntil(timeoutMs: Long = 3000, condition: () -> Boolean) {
        val start = System.currentTimeMillis()
        while (!condition() && System.currentTimeMillis() - start < timeoutMs) {
            Thread.sleep(20)
            testDispatcher.scheduler.advanceUntilIdle()
        }
    }

    @Test
    fun preferenceUpdaters_modifyPreferencesCorrectly() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.onThemeModeSelected(ThemeMode.DARK)
        waitUntil { viewModel.uiState.value.themeMode == ThemeMode.DARK }
        assertThat(viewModel.uiState.value.themeMode).isEqualTo(ThemeMode.DARK)

        viewModel.onDynamicColorToggled(false)
        waitUntil { !viewModel.uiState.value.dynamicColor }
        assertThat(viewModel.uiState.value.dynamicColor).isFalse()

        viewModel.onNagIntervalSelected(15)
        waitUntil { viewModel.uiState.value.defaultNagIntervalMinutes == 15 }
        assertThat(viewModel.uiState.value.defaultNagIntervalMinutes).isEqualTo(15)

        viewModel.onQuietHoursChanged(LocalTime.of(23, 0), LocalTime.of(7, 30))
        waitUntil { viewModel.uiState.value.quietHoursStart == LocalTime.of(23, 0) }
        val state = viewModel.uiState.value
        assertThat(state.quietHoursStart).isEqualTo(LocalTime.of(23, 0))
        assertThat(state.quietHoursEnd).isEqualTo(LocalTime.of(7, 30))

        viewModel.onPersonalitySelected(Personality.UNHINGED)
        waitUntil { viewModel.uiState.value.personality == Personality.UNHINGED }
        assertThat(viewModel.uiState.value.personality).isEqualTo(Personality.UNHINGED)

        viewModel.onDailyCapacityChanged(180)
        waitUntil { viewModel.uiState.value.dailyCapacityMinutes == 180 }
        assertThat(viewModel.uiState.value.dailyCapacityMinutes).isEqualTo(180)

        viewModel.onPreferredNudgeTimeChanged(LocalTime.of(8, 0))
        waitUntil { viewModel.uiState.value.preferredNudgeTime == LocalTime.of(8, 0) }
        assertThat(viewModel.uiState.value.preferredNudgeTime).isEqualTo(LocalTime.of(8, 0))

        viewModel.onWeekStartsOnChanged(DayOfWeek.SUNDAY)
        waitUntil { viewModel.uiState.value.weekStartsOn == DayOfWeek.SUNDAY }
        assertThat(viewModel.uiState.value.weekStartsOn).isEqualTo(DayOfWeek.SUNDAY)
    }

    @Test
    fun deleteFlow_twoStepConfirmationWipesData() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        waitUntil { viewModel.uiState.value.appVersion == "1.0.0" }

        // Seed sample data first
        viewModel.seedSampleData()
        advanceUntilIdle()
        assertThat(database.itemDao().getAllNonDeletedItems()).isNotEmpty()

        // Step 1: User clicks Delete All Data
        viewModel.requestDeleteAllData()
        waitUntil { viewModel.uiState.value.showDeleteConfirmDialog }
        assertThat(viewModel.uiState.value.showDeleteConfirmDialog).isTrue()

        // User confirms step 1 -> opens step 2 double-confirm dialog
        viewModel.confirmFirstDelete()
        waitUntil { viewModel.uiState.value.showDeleteDoubleConfirmDialog }
        val stateAfterStep1 = viewModel.uiState.value
        assertThat(stateAfterStep1.showDeleteConfirmDialog).isFalse()
        assertThat(stateAfterStep1.showDeleteDoubleConfirmDialog).isTrue()

        // User confirms step 2 -> wipes database and dismisses dialog
        viewModel.confirmFinalDelete()
        waitUntil { !viewModel.uiState.value.showDeleteDoubleConfirmDialog && viewModel.uiState.value.snackbarMessage != null }

        val finalState = viewModel.uiState.value
        assertThat(finalState.showDeleteDoubleConfirmDialog).isFalse()
        assertThat(database.itemDao().getAllNonDeletedItems()).isEmpty()
        assertThat(finalState.snackbarMessage).contains("permanently deleted")
    }

    @Test
    fun dismissDeleteDialogs_clearsConfirmationStates() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        waitUntil { viewModel.uiState.value.appVersion == "1.0.0" }

        viewModel.requestDeleteAllData()
        waitUntil { viewModel.uiState.value.showDeleteConfirmDialog }
        assertThat(viewModel.uiState.value.showDeleteConfirmDialog).isTrue()

        viewModel.dismissDeleteDialogs()
        waitUntil { !viewModel.uiState.value.showDeleteConfirmDialog }
        val state = viewModel.uiState.value
        assertThat(state.showDeleteConfirmDialog).isFalse()
        assertThat(state.showDeleteDoubleConfirmDialog).isFalse()
    }

    @Test
    fun privacyDialog_visibilityToggles() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        waitUntil { viewModel.uiState.value.appVersion == "1.0.0" }

        viewModel.setPrivacyDialogVisible(true)
        waitUntil { viewModel.uiState.value.showPrivacyDialog }
        assertThat(viewModel.uiState.value.showPrivacyDialog).isTrue()

        viewModel.setPrivacyDialogVisible(false)
        waitUntil { !viewModel.uiState.value.showPrivacyDialog }
        assertThat(viewModel.uiState.value.showPrivacyDialog).isFalse()
    }
}
