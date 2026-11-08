package com.cooked.app.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.deadline.HeatCalculator
import com.cooked.app.domain.model.ChecklistStep
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemEvent
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.model.Recurrence
import com.cooked.app.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ReminderDetailViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: CookedClock,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reminderId: String = checkNotNull(savedStateHandle["reminderId"])
    private val _isProgressManuallyOverridden = MutableStateFlow(false)
    private val _showRescheduleSheet = MutableStateFlow(false)
    private val _showEmojiPicker = MutableStateFlow(false)
    private val _showDatePicker = MutableStateFlow(false)

    val uiState: StateFlow<ReminderDetailUiState> = combine(
        itemRepository.observeItem(reminderId),
        itemRepository.observeEvents(reminderId),
        userPreferencesRepository.userPrefs,
        _isProgressManuallyOverridden,
        _showRescheduleSheet,
        _showEmojiPicker,
        _showDatePicker
    ) { args ->
        val item = args[0] as Item?
        @Suppress("UNCHECKED_CAST")
        val events = args[1] as List<ItemEvent>
        val prefs = args[2] as com.cooked.app.data.datastore.UserPrefs
        val manuallyOverridden = args[3] as Boolean
        val showReschedule = args[4] as Boolean
        val showEmoji = args[5] as Boolean
        val showDate = args[6] as Boolean

        if (item == null) {
            ReminderDetailUiState(isLoading = false, isDeleted = true)
        } else {
            val now = clock.now()
            val rawHeat = HeatCalculator.calculate(
                item = item,
                now = now,
                dailyCapacityMinutes = prefs.dailyCapacityMinutes,
                quietHoursStart = prefs.quietHoursStart,
                quietHoursEnd = prefs.quietHoursEnd
            )

            val hasStarted = itemRepository.hasStartedRecently(item.id)
            val effectiveHeat = if (hasStarted || item.progress > 0) {
                HeatCalculator.relaxHeatLevel(rawHeat.heatLevel)
            } else {
                rawHeat.heatLevel
            }

            val heatResult = rawHeat.copy(heatLevel = effectiveHeat)
            val startBy = item.startBy ?: HeatCalculator.suggestStartBy(
                item = item,
                now = now,
                dailyCapacityMinutes = prefs.dailyCapacityMinutes,
                preferredNudgeTime = prefs.preferredNudgeTime
            )

            ReminderDetailUiState(
                item = item,
                heatResult = heatResult,
                events = events.sortedByDescending { it.at },
                suggestedStartBy = startBy,
                isLoading = false,
                isDeleted = false,
                isProgressManuallyOverridden = manuallyOverridden,
                showRescheduleSheet = showReschedule,
                showEmojiPicker = showEmoji,
                showDatePicker = showDate
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReminderDetailUiState(isLoading = true)
    )

    fun onTitleChanged(newTitle: String) {
        if (newTitle.isNotBlank()) {
            updateItem { it.copy(title = newTitle.trim()) }
        }
    }

    fun onEmojiSelected(emoji: String?) {
        _showEmojiPicker.value = false
        updateItem { it.copy(emoji = emoji) }
    }

    fun onDueAtChanged(newDueAt: Instant) {
        _showDatePicker.value = false
        viewModelScope.launch {
            itemRepository.reschedule(reminderId, newDueAt)
        }
    }

    fun onRecurrenceChanged(recurrence: Recurrence?) {
        updateItem { it.copy(recurrence = recurrence) }
    }

    fun onNagIntervalChanged(intervalMinutes: Int) {
        updateItem { it.copy(nag = it.nag.copy(intervalMinutes = intervalMinutes)) }
    }

    fun onEscalateToggle(escalate: Boolean) {
        updateItem { it.copy(nag = it.nag.copy(escalate = escalate)) }
    }

    fun onCriticalToggle(critical: Boolean) {
        updateItem { it.copy(critical = critical) }
    }

    fun onNoteChanged(newNote: String) {
        updateItem { it.copy(note = newNote.trim().ifEmpty { null }) }
    }

    fun onProgressChanged(progressRaw: Float) {
        val snapped = (progressRaw / 10f).roundToInt() * 10
        _isProgressManuallyOverridden.value = true
        updateItem { it.copy(progress = snapped.coerceIn(0, 100)) }
    }

    fun onEffortSelected(minutes: Int) {
        updateItem { it.copy(effortMinutes = minutes) }
    }

    fun onToggleChecklistStep(stepId: String) {
        val currentItem = uiState.value.item ?: return
        val updatedSteps = currentItem.checklist.map { step ->
            if (step.id == stepId) step.copy(done = !step.done) else step
        }

        val newProgress = if (!_isProgressManuallyOverridden.value && updatedSteps.isNotEmpty()) {
            val doneCount = updatedSteps.count { it.done }
            ((doneCount.toDouble() / updatedSteps.size) * 100).roundToInt()
        } else {
            currentItem.progress
        }

        viewModelScope.launch {
            itemRepository.update(currentItem.copy(checklist = updatedSteps, progress = newProgress))
        }
    }

    fun onAddChecklistStep(text: String) {
        val currentItem = uiState.value.item ?: return
        if (text.isBlank()) return

        val newStep = ChecklistStep(
            id = UUID.randomUUID().toString(),
            itemId = currentItem.id,
            text = text.trim(),
            done = false,
            order = currentItem.checklist.size
        )
        val updatedSteps = currentItem.checklist + newStep

        val newProgress = if (!_isProgressManuallyOverridden.value && updatedSteps.isNotEmpty()) {
            val doneCount = updatedSteps.count { it.done }
            ((doneCount.toDouble() / updatedSteps.size) * 100).roundToInt()
        } else {
            currentItem.progress
        }

        viewModelScope.launch {
            itemRepository.update(currentItem.copy(checklist = updatedSteps, progress = newProgress))
        }
    }

    fun onDeleteChecklistStep(stepId: String) {
        val currentItem = uiState.value.item ?: return
        val updatedSteps = currentItem.checklist.filter { it.id != stepId }

        val newProgress = if (!_isProgressManuallyOverridden.value && updatedSteps.isNotEmpty()) {
            val doneCount = updatedSteps.count { it.done }
            ((doneCount.toDouble() / updatedSteps.size) * 100).roundToInt()
        } else {
            currentItem.progress
        }

        viewModelScope.launch {
            itemRepository.update(currentItem.copy(checklist = updatedSteps, progress = newProgress))
        }
    }

    fun onStartByChanged(newStartBy: Instant) {
        updateItem { it.copy(startBy = newStartBy) }
    }

    fun setShowRescheduleSheet(show: Boolean) {
        _showRescheduleSheet.value = show
    }

    fun setShowEmojiPicker(show: Boolean) {
        _showEmojiPicker.value = show
    }

    fun setShowDatePicker(show: Boolean) {
        _showDatePicker.value = show
    }

    fun onPushDeadline(newDueAt: Instant, reason: String) {
        viewModelScope.launch {
            itemRepository.reschedule(reminderId, newDueAt)
            itemRepository.logEvent(
                itemId = reminderId,
                type = ItemEventType.RESCHEDULED,
                meta = mapOf(
                    "reason" to reason,
                    "newDueAt" to newDueAt.toString()
                )
            )
            _showRescheduleSheet.value = false
        }
    }

    fun onMarkDone() {
        viewModelScope.launch {
            itemRepository.markDone(reminderId, clock.now())
        }
    }

    fun onDelete() {
        viewModelScope.launch {
            itemRepository.delete(reminderId)
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            itemRepository.restore(reminderId)
        }
    }

    private fun updateItem(transform: (Item) -> Item) {
        val currentItem = uiState.value.item ?: return
        val updated = transform(currentItem)
        viewModelScope.launch {
            itemRepository.update(updated)
            itemRepository.logEvent(
                itemId = currentItem.id,
                type = ItemEventType.EDITED
            )
        }
    }
}
