package com.cooked.app.feature.deadlines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.time.CookedClock
import com.cooked.app.domain.repository.ReminderRepository
import com.cooked.app.domain.usecase.CalculateCookedScoreUseCase
import com.cooked.app.domain.usecase.CalculatePressureUseCase
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
class DeadlinesViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val calculatePressureUseCase: CalculatePressureUseCase,
    private val calculateCookedScoreUseCase: CalculateCookedScoreUseCase,
    private val completeReminderUseCase: CompleteReminderUseCase,
    private val snoozeReminderUseCase: SnoozeReminderUseCase,
    private val clock: CookedClock
) : ViewModel() {

    val uiState: StateFlow<DeadlinesUiState> = repository.getActiveDeadlines()
        .map { list ->
            val now = clock.now().toEpochMilli()
            val items = list.map { reminder ->
                DeadlineItem(
                    reminder = reminder,
                    pressureLevel = calculatePressureUseCase(reminder, now),
                    remainingMillis = (reminder.dueInstant ?: now) - now
                )
            }.sortedBy { it.reminder.dueInstant }

            val score = calculateCookedScoreUseCase(list, now)

            DeadlinesUiState(
                deadlines = items,
                cookedScore = score,
                isLoading = false,
                emptyMessage = "Zero hard deadlines on the radar. Don't jinx it."
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DeadlinesUiState(isLoading = true)
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
