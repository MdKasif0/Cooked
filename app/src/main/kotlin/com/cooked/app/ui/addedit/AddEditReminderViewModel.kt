package com.cooked.app.ui.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.model.NagPolicy
import com.cooked.app.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

/**
 * Add/edit form backed by the `items` table (the same store every other screen, alarm and
 * widget reads from). Saving goes through [ItemRepository], whose itemChanged event
 * reschedules alarms and refreshes widgets.
 */
@HiltViewModel
class AddEditReminderViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: CookedClock,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reminderIdArg: String? = savedStateHandle["reminderId"]

    private val _uiState = MutableStateFlow(
        AddEditUiState(
            reminderId = reminderIdArg,
            dueInstantUtc = clock.now().plus(Duration.ofMinutes(15)).toEpochMilli(),
            canSave = false,
            isDeleteVisible = reminderIdArg != null
        )
    )
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    /** The item as loaded from the database; null when creating a new one. */
    private var existingItem: Item? = null

    init {
        viewModelScope.launch {
            if (reminderIdArg != null) {
                val existing = itemRepository.getItem(reminderIdArg)
                if (existing != null && !existing.isDeleted) {
                    existingItem = existing
                    _uiState.update { current ->
                        current.copy(
                            title = existing.title,
                            note = existing.note ?: "",
                            dueInstantUtc = existing.dueAt.toEpochMilli(),
                            nagIntervalMinutes = existing.nag.intervalMinutes,
                            isDeadlineMode = existing.kind == ItemKind.DEADLINE,
                            deadlineStartInstantUtc = existing.startBy?.toEpochMilli(),
                            estimatedEffortMinutes = existing.effortMinutes ?: current.estimatedEffortMinutes,
                            canSave = existing.title.isNotBlank()
                        )
                    }
                } else {
                    // Item no longer exists: treat as a new item instead of editing a ghost.
                    _uiState.update { it.copy(reminderId = null, isDeleteVisible = false) }
                }
            } else {
                val defaultNag = userPreferencesRepository.userPrefs.first().defaultNagInterval
                _uiState.update { it.copy(nagIntervalMinutes = defaultNag) }
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
        val target = clock.now().plus(Duration.ofMinutes(minutes.toLong())).toEpochMilli()
        _uiState.update { it.copy(dueInstantUtc = target) }
    }

    fun setQuickTimeTonight() {
        val zone = clock.zone()
        val today = clock.now().atZone(zone).toLocalDate()
        val target = today.atTime(LocalTime.of(20, 0)).atZone(zone).toInstant().toEpochMilli()
        _uiState.update { it.copy(dueInstantUtc = target) }
    }

    fun setQuickTimeTomorrowMorning() {
        val zone = clock.zone()
        val tomorrow = clock.now().atZone(zone).toLocalDate().plusDays(1)
        val target = tomorrow.atTime(LocalTime.of(9, 0)).atZone(zone).toInstant().toEpochMilli()
        _uiState.update { it.copy(dueInstantUtc = target) }
    }

    fun setCustomDueInstant(utcMillis: Long) {
        _uiState.update { it.copy(dueInstantUtc = utcMillis) }
    }

    fun onNagIntervalChange(minutes: Int) {
        _uiState.update { it.copy(nagIntervalMinutes = minutes) }
    }

    fun onDeadlineModeToggle(enabled: Boolean) {
        _uiState.update { it.copy(isDeadlineMode = enabled) }
    }

    fun onEffortMinutesChange(minutes: Int) {
        _uiState.update { it.copy(estimatedEffortMinutes = minutes) }
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) return

        val now = clock.now()
        val dueAt = state.dueInstantUtc?.let { Instant.ofEpochMilli(it) }
            ?: now.plus(Duration.ofMinutes(15))
        val kind = if (state.isDeadlineMode) ItemKind.DEADLINE else ItemKind.REMINDER
        val effort = if (state.isDeadlineMode) state.estimatedEffortMinutes else null
        val existing = existingItem

        viewModelScope.launch {
            if (existing != null) {
                val dueChanged = existing.dueAt != dueAt
                itemRepository.update(
                    existing.copy(
                        title = state.title.trim(),
                        note = state.note.trim().ifEmpty { null },
                        dueAt = dueAt,
                        kind = kind,
                        effortMinutes = effort,
                        nag = existing.nag.copy(intervalMinutes = state.nagIntervalMinutes),
                        // A new due time supersedes any snooze set for the old one.
                        snoozedUntil = if (dueChanged) null else existing.snoozedUntil
                    )
                )
            } else {
                itemRepository.create(
                    Item(
                        id = UUID.randomUUID().toString(),
                        kind = kind,
                        title = state.title.trim(),
                        note = state.note.trim().ifEmpty { null },
                        dueAt = dueAt,
                        timeZoneId = clock.zone().id,
                        nag = NagPolicy(intervalMinutes = state.nagIntervalMinutes),
                        status = ItemStatus.ACTIVE,
                        effortMinutes = effort,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun delete() {
        val id = existingItem?.id ?: return
        viewModelScope.launch {
            itemRepository.delete(id)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
