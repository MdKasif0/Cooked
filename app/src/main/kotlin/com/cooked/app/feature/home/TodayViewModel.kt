package com.cooked.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.domain.repository.ReminderRepository
import com.cooked.app.domain.usecase.CalculateCookedScoreUseCase
import com.cooked.app.domain.usecase.CompleteReminderUseCase
import com.cooked.app.domain.usecase.SnoozeReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val calculateCookedScoreUseCase: CalculateCookedScoreUseCase,
    private val completeReminderUseCase: CompleteReminderUseCase,
    private val snoozeReminderUseCase: SnoozeReminderUseCase
) : ViewModel() {

    val uiState: StateFlow<TodayUiState> = repository.getActiveReminders()
        .map { reminders ->
            val score = calculateCookedScoreUseCase(reminders)
            TodayUiState(
                reminders = reminders,
                cookedScore = score,
                isLoading = false,
                emptyMessage = "Nothing due. Suspiciously calm."
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TodayUiState(isLoading = true)
        )

    fun onDoneClick(reminderId: String) {
        viewModelScope.launch {
            completeReminderUseCase(reminderId)
        }
    }

    fun onSnoozeClick(reminderId: String) {
        viewModelScope.launch {
            snoozeReminderUseCase(reminderId, snoozeMinutes = 10)
        }
    }
}
