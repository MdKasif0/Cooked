package com.cooked.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.designsystem.ThemeMode
import com.cooked.app.core.util.SampleDataSeeder
import com.cooked.app.data.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val sampleDataSeeder: SampleDataSeeder
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.themeMode,
        preferencesRepository.defaultNagIntervalMinutes,
        preferencesRepository.isOemWarningDismissed
    ) { themeMode, nagInterval, oemDismissed ->
        SettingsUiState(
            themeMode = themeMode,
            defaultNagIntervalMinutes = nagInterval,
            isOemWarningDismissed = oemDismissed,
            appVersion = "1.0.0"
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

    fun seedSampleData() {
        viewModelScope.launch {
            sampleDataSeeder.seedSampleData()
        }
    }
}
