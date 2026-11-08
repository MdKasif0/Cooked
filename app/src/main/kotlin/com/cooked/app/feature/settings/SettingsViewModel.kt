package com.cooked.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.designsystem.ThemeMode
import com.cooked.app.core.util.SampleDataSeeder
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.model.Personality
import com.cooked.app.scheduling.alarm.AlarmScheduler
import com.cooked.app.scheduling.battery.ReliabilityChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val sampleDataSeeder: SampleDataSeeder,
    private val alarmScheduler: AlarmScheduler,
    private val reliabilityChecker: ReliabilityChecker
) : ViewModel() {

    private val _alarmsRefreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.themeMode,
        preferencesRepository.defaultNagIntervalMinutes,
        preferencesRepository.personality,
        preferencesRepository.isOemWarningDismissed,
        _alarmsRefreshTrigger
    ) { themeMode, nagInterval, personality, oemDismissed, _ ->
        val alarms = alarmScheduler.getScheduledAlarms()
        val report = reliabilityChecker.check()

        SettingsUiState(
            themeMode = themeMode,
            defaultNagIntervalMinutes = nagInterval,
            personality = personality,
            isOemWarningDismissed = oemDismissed,
            appVersion = "1.0.0",
            scheduledAlarms = alarms,
            reliabilityReport = report
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun onThemeModeSelected(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun onNagIntervalSelected(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.setDefaultNagIntervalMinutes(minutes)
        }
    }

    fun onPersonalitySelected(personality: Personality) {
        viewModelScope.launch {
            preferencesRepository.setPersonality(personality)
        }
    }

    fun refreshAlarms() {
        _alarmsRefreshTrigger.value += 1
    }

    fun fireAlarmNow(itemId: String) {
        viewModelScope.launch {
            alarmScheduler.fireNow(itemId)
            refreshAlarms()
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            sampleDataSeeder.seedSampleData()
            alarmScheduler.rescheduleAll()
            refreshAlarms()
        }
    }
}
