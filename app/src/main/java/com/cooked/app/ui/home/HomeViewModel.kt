package com.cooked.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.oem.OemDetector
import com.cooked.app.data.preferences.UserPreferencesRepository
import com.cooked.app.domain.repository.ReminderRepository
import com.cooked.app.domain.usecase.CalculateCookedScoreUseCase
import com.cooked.app.domain.usecase.CalculatePressureUseCase
import com.cooked.app.domain.usecase.CompleteReminderUseCase
import com.cooked.app.domain.usecase.SnoozeReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val calculatePressureUseCase: CalculatePressureUseCase,
    private val calculateCookedScoreUseCase: CalculateCookedScoreUseCase,
    private val completeReminderUseCase: CompleteReminderUseCase,
    private val snoozeReminderUseCase: SnoozeReminderUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(ReminderFilter.ALL)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllReminders(),
        _searchQuery,
        _activeFilter,
        userPreferencesRepository.isOemWarningDismissed
    ) { reminders, query, filter, oemDismissed ->
        val itemsUi = reminders.map { reminder ->
            ReminderItemUi(
                reminder = reminder,
                pressureLevel = calculatePressureUseCase(reminder)
            )
        }

        val filtered = itemsUi.filter { itemUi ->
            val matchesFilter = when (filter) {
                ReminderFilter.ALL -> !itemUi.reminder.isCompleted
                ReminderFilter.NAGGING -> itemUi.reminder.isNagging
                ReminderFilter.DEADLINES -> itemUi.reminder.isDeadlineMode && !itemUi.reminder.isCompleted
                ReminderFilter.COMPLETED -> itemUi.reminder.isCompleted
            }
            val matchesSearch = query.isBlank() ||
                    itemUi.reminder.title.contains(query, ignoreCase = true) ||
                    (itemUi.reminder.note?.contains(query, ignoreCase = true) == true)

            matchesFilter && matchesSearch
        }

        val activeList = reminders.filter { !it.isCompleted }
        val cookedScore = calculateCookedScoreUseCase(activeList)
        val showOemWarning = !oemDismissed && OemDetector.isAggressiveOem()

        HomeUiState(
            reminders = itemsUi,
            filteredReminders = filtered,
            searchQuery = query,
            activeFilter = filter,
            cookedScore = cookedScore,
            isOemWarningVisible = showOemWarning,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: ReminderFilter) {
        _activeFilter.value = filter
    }

    fun onDoneClick(reminderId: String) {
        viewModelScope.launch {
            completeReminderUseCase(reminderId)
        }
    }

    fun onSnoozeClick(reminderId: String) {
        viewModelScope.launch {
            snoozeReminderUseCase(reminderId, snoozeMinutes = 10)
        }
    }

    fun onDismissOemWarning() {
        viewModelScope.launch {
            userPreferencesRepository.setOemWarningDismissed(true)
        }
    }
}

