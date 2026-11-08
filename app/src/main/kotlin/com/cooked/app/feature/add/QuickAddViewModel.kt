package com.cooked.app.feature.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.data.datastore.UserPrefs
import com.cooked.app.domain.model.ChecklistStep
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.model.NagPolicy
import com.cooked.app.domain.model.Recurrence
import com.cooked.app.domain.parse.QuickParser
import com.cooked.app.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

enum class QuickTimeChip {
    TODAY,
    TONIGHT,
    TOMORROW,
    PLUS_10M,
    PLUS_1H
}

@HiltViewModel
class QuickAddViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: CookedClock
) : ViewModel() {

    companion object {
        private var draftCache: String? = null
        private var draftCacheTimestamp: Long = 0L
        private const val DRAFT_TTL_MS = 10 * 60 * 1000L // 10 minutes

        fun clearDraftCacheForTesting() {
            draftCache = null
            draftCacheTimestamp = 0L
        }
    }

    private val _uiState = MutableStateFlow(QuickAddUiState())
    val uiState: StateFlow<QuickAddUiState> = _uiState.asStateFlow()

    private val _saveEvents = MutableSharedFlow<Item>(extraBufferCapacity = 1)
    val saveEvents: SharedFlow<Item> = _saveEvents.asSharedFlow()

    init {
        restoreDraftIfValid()
    }

    private fun restoreDraftIfValid() {
        val cache = draftCache
        val now = System.currentTimeMillis()
        if (!cache.isNullOrBlank() && (now - draftCacheTimestamp) < DRAFT_TTL_MS) {
            onTextChanged(cache)
        }
    }

    fun onTextChanged(newText: String) {
        val parsed = QuickParser.parse(
            text = newText,
            now = clock.now(),
            zone = clock.zone()
        )
        _uiState.update {
            it.copy(
                input = newText,
                parsedResult = parsed
            )
        }
        draftCache = newText
        draftCacheTimestamp = System.currentTimeMillis()
    }

    fun onQuickChipSelected(chip: QuickTimeChip) {
        val now = clock.now()
        val zone = clock.zone()
        val zdt = now.atZone(zone)
        val today = zdt.toLocalDate()

        val targetInstant = when (chip) {
            QuickTimeChip.TODAY -> {
                val hour = if (zdt.hour >= 17) (zdt.hour + 1).coerceAtMost(23) else 18
                today.atTime(hour, 0).atZone(zone).toInstant()
            }
            QuickTimeChip.TONIGHT -> {
                today.atTime(20, 0).atZone(zone).toInstant()
            }
            QuickTimeChip.TOMORROW -> {
                today.plusDays(1).atTime(9, 0).atZone(zone).toInstant()
            }
            QuickTimeChip.PLUS_10M -> {
                now.plus(Duration.ofMinutes(10))
            }
            QuickTimeChip.PLUS_1H -> {
                now.plus(Duration.ofHours(1))
            }
        }
        _uiState.update { it.copy(overrideDueAt = targetInstant) }
    }

    fun onToggleDeadline() {
        _uiState.update {
            val currentKind = it.effectiveKind
            val newKind = if (currentKind == ItemKind.DEADLINE) ItemKind.REMINDER else ItemKind.DEADLINE
            it.copy(overrideKind = newKind)
        }
    }

    fun onSetKind(kind: ItemKind) {
        _uiState.update { it.copy(overrideKind = kind) }
    }

    fun onToggleCritical() {
        _uiState.update {
            it.copy(overrideCritical = !it.effectiveCritical)
        }
    }

    fun onSetDueAt(instant: java.time.Instant?) {
        _uiState.update { it.copy(overrideDueAt = instant) }
    }

    fun onSetEffortMinutes(minutes: Int?) {
        _uiState.update { it.copy(overrideEffortMinutes = minutes) }
    }

    fun onSetRecurrence(recurrence: Recurrence?) {
        _uiState.update { it.copy(overrideRecurrence = recurrence) }
    }

    fun setShowDateTimePicker(show: Boolean) {
        _uiState.update { it.copy(showDateTimePicker = show) }
    }

    fun onDismiss() {
        // Cache draft if there's content
        if (_uiState.value.input.isNotBlank()) {
            draftCache = _uiState.value.input
            draftCacheTimestamp = System.currentTimeMillis()
        }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return

        val now = clock.now()
        val dueAt = state.effectiveDueAt ?: now.plus(Duration.ofMinutes(15))
        val title = state.effectiveTitle.ifBlank { state.input.trim() }

        val item = Item(
            id = UUID.randomUUID().toString(),
            kind = state.effectiveKind,
            title = title,
            note = null,
            emoji = null,
            dueAt = dueAt,
            allDay = state.effectiveAllDay,
            timeZoneId = clock.zone().id,
            recurrence = state.effectiveRecurrence,
            nag = NagPolicy(intervalMinutes = 10),
            critical = state.effectiveCritical,
            status = ItemStatus.ACTIVE,
            snoozedUntil = null,
            completedAt = null,
            effortMinutes = state.effectiveEffortMinutes,
            progress = 0,
            checklist = emptyList(),
            createdAt = now,
            updatedAt = now
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            itemRepository.create(item)
            itemRepository.logEvent(
                itemId = item.id,
                type = ItemEventType.CREATED,
                at = now
            )
            // Clear cached draft upon successful save
            draftCache = null
            draftCacheTimestamp = 0L

            _saveEvents.tryEmit(item)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    isSaved = true,
                    savedItem = item
                )
            }
        }
    }

    fun undoLastSave() {
        val lastItem = _uiState.value.savedItem ?: return
        viewModelScope.launch {
            itemRepository.delete(lastItem.id)
            _uiState.update { it.copy(savedItem = null) }
        }
    }

    fun reset() {
        _uiState.value = QuickAddUiState()
    }
}
