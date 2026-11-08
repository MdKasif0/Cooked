package com.cooked.app.feature.deadlines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.deadline.CookedScoreCalculator
import com.cooked.app.domain.deadline.HeatCalculator
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeadlinesViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: CookedClock
) : ViewModel() {

    val uiState: StateFlow<DeadlinesUiState> = combine(
        itemRepository.observeDeadlines(),
        userPreferencesRepository.userPrefs
    ) { items, prefs ->
        val now = clock.now()
        val score = CookedScoreCalculator.calculate(
            items = items,
            now = now,
            dailyCapacityMinutes = prefs.dailyCapacityMinutes
        )

        val cardModels = items.map { item ->
            val hasStarted = itemRepository.hasStartedRecently(item.id)
            val rawHeat = HeatCalculator.calculate(
                item = item,
                now = now,
                dailyCapacityMinutes = prefs.dailyCapacityMinutes,
                quietHoursStart = prefs.quietHoursStart,
                quietHoursEnd = prefs.quietHoursEnd
            )

            val effectiveHeatLevel = if (hasStarted || item.progress > 0) {
                HeatCalculator.relaxHeatLevel(rawHeat.heatLevel)
            } else {
                rawHeat.heatLevel
            }

            val heatResult = rawHeat.copy(heatLevel = effectiveHeatLevel)
            val startBy = item.startBy ?: HeatCalculator.suggestStartBy(
                item = item,
                now = now,
                dailyCapacityMinutes = prefs.dailyCapacityMinutes,
                preferredNudgeTime = prefs.preferredNudgeTime
            )

            DeadlineCardUiModel(
                item = item,
                heatResult = heatResult,
                suggestedStartBy = startBy,
                hasStartedRecently = hasStarted
            )
        }.sortedWith(
            compareByDescending<DeadlineCardUiModel> { it.heatResult.heatLevel.ordinal }
                .thenBy { it.item.dueAt }
        )

        DeadlinesUiState(
            scoreResult = score,
            deadlines = cardModels,
            isLoading = false,
            emptyMessage = "No deadlines. Either you're on top of life or you haven't added them yet."
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DeadlinesUiState(isLoading = true)
    )

    fun onImOnItClick(itemId: String) {
        viewModelScope.launch {
            itemRepository.logEvent(
                itemId = itemId,
                type = ItemEventType.STARTED,
                meta = mapOf("source" to "deadline_card")
            )
        }
    }

    fun onDoneClick(itemId: String) {
        viewModelScope.launch {
            itemRepository.markDone(itemId)
        }
    }
}
