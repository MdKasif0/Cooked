package com.cooked.app.feature.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.time.CookedClock
import com.cooked.app.core.time.TimeUtils
import com.cooked.app.domain.model.Reminder
import com.cooked.app.domain.usecase.SaveReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class QuickAddViewModel @Inject constructor(
    private val saveReminderUseCase: SaveReminderUseCase,
    private val clock: CookedClock
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        QuickAddUiState(
            dueInstantUtc = clock.now().toEpochMilli() + (15 * 60 * 1000L)
        )
    )
    val uiState: StateFlow<QuickAddUiState> = _uiState.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, canSave = newTitle.isNotBlank()) }
    }

    fun onNoteChange(newNote: String) {
        _uiState.update { it.copy(note = newNote) }
    }

    fun setQuickTimeInMinutes(minutes: Int) {
        val target = clock.now().toEpochMilli() + (minutes * 60 * 1000L)
        _uiState.update { it.copy(dueInstantUtc = target) }
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) return

        val now = clock.now().toEpochMilli()
        val reminder = Reminder(
            id = UUID.randomUUID().toString(),
            title = state.title.trim(),
            note = state.note.trim().ifEmpty { null },
            dueInstant = state.dueInstantUtc,
            nagIntervalMinutes = state.nagIntervalMinutes,
            isCompleted = false,
            isNaggingActive = false,
            nagCount = 0,
            isDeadlineMode = state.isDeadlineMode,
            deadlineStartInstant = if (state.isDeadlineMode) now else null,
            estimatedEffortMinutes = state.estimatedEffortMinutes,
            createdAt = now,
            updatedAt = now
        )

        viewModelScope.launch {
            saveReminderUseCase(reminder)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun reset() {
        _uiState.value = QuickAddUiState(
            dueInstantUtc = clock.now().toEpochMilli() + (15 * 60 * 1000L)
        )
    }
}
