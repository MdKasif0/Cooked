package com.cooked.app.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.domain.repository.ReminderRepository
import com.cooked.app.domain.usecase.CalculatePressureUseCase
import com.cooked.app.domain.usecase.CompleteReminderUseCase
import com.cooked.app.domain.usecase.DeleteReminderUseCase
import com.cooked.app.domain.usecase.SnoozeReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderDetailViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val calculatePressureUseCase: CalculatePressureUseCase,
    private val completeReminderUseCase: CompleteReminderUseCase,
    private val snoozeReminderUseCase: SnoozeReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reminderId: String = checkNotNull(savedStateHandle["reminderId"])

    private val _uiState = MutableStateFlow(ReminderDetailUiState())
    val uiState: StateFlow<ReminderDetailUiState> = _uiState.asStateFlow()

    init {
        loadReminder()
    }

    private fun loadReminder() {
        viewModelScope.launch {
            val reminder = repository.getReminderById(reminderId)
            if (reminder != null) {
                _uiState.update {
                    it.copy(
                        reminder = reminder,
                        pressureLevel = calculatePressureUseCase(reminder),
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, isDeleted = true) }
            }
        }
    }

    fun onDoneClick() {
        viewModelScope.launch {
            completeReminderUseCase(reminderId)
            loadReminder()
        }
    }

    fun onSnoozeClick(minutes: Int = 10) {
        viewModelScope.launch {
            snoozeReminderUseCase(reminderId, snoozeMinutes = minutes)
            loadReminder()
        }
    }

    fun onDeleteClick() {
        viewModelScope.launch {
            deleteReminderUseCase(reminderId)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }
}

