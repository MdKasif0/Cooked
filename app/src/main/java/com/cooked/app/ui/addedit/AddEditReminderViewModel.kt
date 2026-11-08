package com.cooked.app.ui.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.time.TimeUtils
import com.cooked.app.domain.model.Reminder
import com.cooked.app.domain.repository.ReminderRepository
import com.cooked.app.domain.usecase.DeleteReminderUseCase
import com.cooked.app.domain.usecase.SaveReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditReminderViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val saveReminderUseCase: SaveReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reminderIdArg: String? = savedStateHandle["reminderId"]

    private val _uiState = MutableStateFlow(
        AddEditUiState(
            reminderId = reminderIdArg,
            dueInstantUtc = TimeUtils.nowUtcMillis() + (15 * 60 * 1000L), // Default to +15 min for instant quickness
            canSave = false,
            isDeleteVisible = reminderIdArg != null
        )
    )
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private var initialCreatedAt: Long = TimeUtils.nowUtcMillis()
    private var initialIsCompleted: Boolean = false
    private var initialCompletedAt: Long? = null
    private var initialNagCount: Int = 0

    init {
        if (reminderIdArg != null) {
            viewModelScope.launch {
                val existing = repository.getReminderById(reminderIdArg)
                if (existing != null) {
                    initialCreatedAt = existing.createdAt
                    initialIsCompleted = existing.isCompleted
                    initialCompletedAt = existing.completedAt
                    initialNagCount = existing.nagCount

                    _uiState.update { current ->
                        current.copy(
                            title = existing.title,
                            note = existing.note ?: "",
                            dueInstantUtc = existing.dueInstant,
                            nagIntervalMinutes = existing.nagIntervalMinutes,
                            isDeadlineMode = existing.isDeadlineMode,
                            deadlineStartInstantUtc = existing.deadlineStartInstant,
                            estimatedEffortMinutes = existing.estimatedEffortMinutes,
                            canSave = existing.title.isNotBlank()
                        )
                    }
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, canSave = newTitle.isNotBlank()) }
    }

    fun onNoteChange(newNote: String) {
        _uiState.update { it.copy(note = newNote) }
    }

    fun setQuickTimeInMinutes(minutes: Int) {
        val target = TimeUtils.nowUtcMillis() + (minutes * 60 * 1000L)
        _uiState.update { it.copy(dueInstantUtc = target) }
    }

    fun setQuickTimeTonight() {
        val today = LocalDate.now()
        val tonightTime = LocalTime.of(20, 0)
        val target = TimeUtils.localDateAndTimeToUtcMillis(today, tonightTime)
        _uiState.update { it.copy(dueInstantUtc = target) }
    }

    fun setQuickTimeTomorrowMorning() {
        val tomorrow = LocalDate.now().plusDays(1)
        val morningTime = LocalTime.of(9, 0)
        val target = TimeUtils.localDateAndTimeToUtcMillis(tomorrow, morningTime)
        _uiState.update { it.copy(dueInstantUtc = target) }
    }

    fun setCustomDueInstant(utcMillis: Long) {
        _uiState.update { it.copy(dueInstantUtc = utcMillis) }
    }

    fun onNagIntervalChange(minutes: Int) {
        _uiState.update { it.copy(nagIntervalMinutes = minutes) }
    }

    fun onDeadlineModeToggle(enabled: Boolean) {
        _uiState.update { current ->
            current.copy(
                isDeadlineMode = enabled,
                deadlineStartInstantUtc = if (enabled) TimeUtils.nowUtcMillis() else null
            )
        }
    }

    fun onEffortMinutesChange(minutes: Int) {
        _uiState.update { it.copy(estimatedEffortMinutes = minutes) }
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) return

        val now = TimeUtils.nowUtcMillis()
        val reminder = Reminder(
            id = state.reminderId ?: UUID.randomUUID().toString(),
            title = state.title.trim(),
            note = state.note.trim().ifEmpty { null },
            dueInstant = state.dueInstantUtc,
            nagIntervalMinutes = state.nagIntervalMinutes,
            isCompleted = initialIsCompleted,
            completedAt = initialCompletedAt,
            isNaggingActive = false,
            nagCount = initialNagCount,
            snoozedUntil = null,
            isDeadlineMode = state.isDeadlineMode,
            deadlineStartInstant = state.deadlineStartInstantUtc ?: now,
            estimatedEffortMinutes = state.estimatedEffortMinutes,
            createdAt = if (state.reminderId != null) initialCreatedAt else now,
            updatedAt = now
        )

        viewModelScope.launch {
            saveReminderUseCase(reminder)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun delete() {
        val id = _uiState.value.reminderId ?: return
        viewModelScope.launch {
            deleteReminderUseCase(id)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}

