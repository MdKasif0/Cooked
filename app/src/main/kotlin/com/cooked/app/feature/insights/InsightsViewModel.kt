package com.cooked.app.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.time.CookedClock
import com.cooked.app.domain.repository.ReminderRepository
import com.cooked.app.domain.usecase.CalculateCookedScoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val calculateCookedScoreUseCase: CalculateCookedScoreUseCase,
    private val clock: CookedClock
) : ViewModel() {

    val uiState: StateFlow<InsightsUiState> = repository.getActiveReminders()
        .map { reminders ->
            val now = clock.now().toEpochMilli()
            val score = calculateCookedScoreUseCase(reminders, now)
            val message = if (score.score == 0) {
                "Cooked Index: 0%. You are thriving, no cap."
            } else {
                "Cooked Index: ${score.score}%. ${score.headline}"
            }

            InsightsUiState(
                cookedScore = score,
                emptyMessage = message,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InsightsUiState(isLoading = true)
        )
}

