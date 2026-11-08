package com.cooked.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.defaultNagIntervalMinutes,
        preferencesRepository.isOemWarningDismissed
    ) { nagInterval, oemDismissed ->
        SettingsUiState(
            defaultNagIntervalMinutes = nagInterval,
            isOemWarningDismissed = oemDismissed,
            appVersion = "1.0.0"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun onNagIntervalSelected(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.setDefaultNagIntervalMinutes(minutes)
        }
    }
}
