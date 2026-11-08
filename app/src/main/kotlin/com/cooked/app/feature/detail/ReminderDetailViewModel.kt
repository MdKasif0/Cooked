package com.cooked.app.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.deadline.HeatCalculator
import com.cooked.app.domain.model.ChecklistStep
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemEventType
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

    val uiState: StateFlow<ReminderDetailUiState> = combine(
        itemRepository.observeItem(reminderId),
        userPreferencesRepository.userPrefs,
        _isProgressManuallyOverridden,
        _showRescheduleSheet
    ) { item, prefs, manuallyOverridden, showReschedule ->
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
                suggestedStartBy = startBy,
                isLoading = false,
                isDeleted = false,
                isProgressManuallyOverridden = manuallyOverridden,
                showRescheduleSheet = showReschedule
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReminderDetailUiState(isLoading = true)
    )

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
            itemRepository.markDone(reminderId)
        }
    }

    fun onDelete() {
        viewModelScope.launch {
            itemRepository.delete(reminderId)
        }
    }

    private fun updateItem(transform: (Item) -> Item) {
        val currentItem = uiState.value.item ?: return
        val updated = transform(currentItem)
        viewModelScope.launch {
            itemRepository.update(updated)
        }
    }
}
