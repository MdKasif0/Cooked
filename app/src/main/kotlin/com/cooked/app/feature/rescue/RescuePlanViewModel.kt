package com.cooked.app.feature.rescue

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.config.FeatureFlags
import com.cooked.app.core.telemetry.Telemetry
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.calendar.CalendarManager
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.repository.ItemRepository
import com.cooked.app.domain.rescue.RescuePlan
import com.cooked.app.domain.rescue.RescuePlanner
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

sealed interface RescuePlanUiState {
    object Loading : RescuePlanUiState
    data class Success(val plan: RescuePlan) : RescuePlanUiState
    object Error : RescuePlanUiState
}

@HiltViewModel
class RescuePlanViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val itemRepository: ItemRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val telemetry: Telemetry,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val targetItemId: String = checkNotNull(savedStateHandle["targetItemId"])
    private val _uiState = MutableStateFlow<RescuePlanUiState>(RescuePlanUiState.Loading)
    val uiState: StateFlow<RescuePlanUiState> = _uiState.asStateFlow()

    init {
        loadPlan()
    }

    private fun loadPlan() {
        viewModelScope.launch {
            val target = itemRepository.getItem(targetItemId)
            if (target == null) {
                _uiState.value = RescuePlanUiState.Error
                return@launch
            }
            val allActive = itemRepository.getActiveItems()
            val prefs = userPreferencesRepository.userPrefs.first()

            val calendarBusy = if (FeatureFlags.isCalendarCapacityEnabled && prefs.calendarCapacityEnabled) {
                CalendarManager.getBusyBlocks(
                    context = context,
                    selectedCalendarIds = prefs.selectedCalendarIds
                )
            } else {
                emptyList()
            }

            val plan = RescuePlanner.generatePlan(
                target = target,
                allItems = allActive,
                now = Instant.now(),
                dailyCapacityMinutes = prefs.dailyCapacityMinutes,
                quietHoursStart = prefs.quietHoursStart,
                quietHoursEnd = prefs.quietHoursEnd,
                calendarBusyBlocks = calendarBusy,
                zone = ZoneId.systemDefault()
            )

            _uiState.value = RescuePlanUiState.Success(plan)
        }
    }

    fun lockItIn(onComplete: () -> Unit) {
        val currentState = _uiState.value
        if (currentState is RescuePlanUiState.Success) {
            viewModelScope.launch {
                telemetry.rescuePlanCreated(currentState.plan.sessions.size)
                itemRepository.logEvent(
                    itemId = targetItemId,
                    type = ItemEventType.STARTED,
                    meta = mapOf("planSessions" to currentState.plan.sessions.size.toString())
                )
                onComplete()
            }
        }
    }
}
