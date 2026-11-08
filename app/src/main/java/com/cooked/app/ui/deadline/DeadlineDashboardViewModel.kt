package com.cooked.app.ui.deadline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.time.TimeUtils
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
class DeadlineDashboardViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val calculatePressureUseCase: CalculatePressureUseCase,
    private val calculateCookedScoreUseCase: CalculateCookedScoreUseCase,
    private val completeReminderUseCase: CompleteReminderUseCase,
    private val snoozeReminderUseCase: SnoozeReminderUseCase
) : ViewModel() {

    val uiState: StateFlow<DeadlineUiState> = repository.getActiveDeadlines()
        .map { deadlines ->
            val now = TimeUtils.nowUtcMillis()
            val items = deadlines.map { reminder ->
                val pressure = calculatePressureUseCase(reminder, now)
                val remainingMillis = (reminder.dueInstant ?: now) - now
                val remainingHours = remainingMillis / (1000.0 * 3600.0)
                val effortHours = reminder.estimatedEffortMinutes / 60.0
                val bufferHours = (remainingHours - effortHours).coerceAtLeast(0.0)

                DeadlineItemUi(
                    reminder = reminder,
                    pressureLevel = pressure,
                    remainingMillis = remainingMillis,
                    bufferHours = kotlin.math.round(bufferHours * 10) / 10.0
                )
            }.sortedBy { it.reminder.dueInstant }

            val cookedScore = calculateCookedScoreUseCase(deadlines, now)

            DeadlineUiState(
                deadlines = items,
                cookedScore = cookedScore,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DeadlineUiState()
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
