package com.cooked.app.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.dao.AnalyticsDao
import com.cooked.app.data.dao.WeeklySnapshotDao
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.insights.AnalyticsCalculator
import com.cooked.app.domain.insights.model.DayCompletion
import com.cooked.app.domain.insights.model.InsightRange
import com.cooked.app.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val analyticsDao: AnalyticsDao,
    private val weeklySnapshotDao: WeeklySnapshotDao,
    private val itemRepository: ItemRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: CookedClock
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(InsightRange.WEEK_7D)
    private val _selectedDayCompletion = MutableStateFlow<DayCompletion?>(null)
    private val _showCookedReportDialog = MutableStateFlow(false)

    val uiState: StateFlow<InsightsUiState> = combine(
        _selectedRange,
        userPreferencesRepository.userPrefs,
        itemRepository.observeDeadlines(),
        _selectedDayCompletion,
        _showCookedReportDialog
    ) { range, prefs, deadlines, selectedDay, showReport ->
        val now = clock.now()
        val zone = clock.zone()

        val rangeDays = range.days.toLong()
        val from = now.minus(Duration.ofDays(rangeDays))
        val prevFrom = from.minus(Duration.ofDays(rangeDays))
        val prevTo = from

        // 1. Current period data
        val currentEntities = analyticsDao.getCompletedItemsInRange(from, now)
        val currentCompletedItems = currentEntities.map { it.toDomain() }
        val currentEvents = analyticsDao.getEventsInRange(from, now).map { it.toDomain() }

        // 2. Previous period data for deltas
        val prevEntities = analyticsDao.getCompletedItemsInRange(prevFrom, prevTo)
        val prevCompletedItems = prevEntities.map { it.toDomain() }
        val prevEvents = analyticsDao.getEventsInRange(prevFrom, prevTo).map { it.toDomain() }

        // 3. Weekly snapshots
        val snapshots = weeklySnapshotDao.getRecentSnapshots(12)

        val summary = AnalyticsCalculator.calculate(
            completedItems = currentCompletedItems,
            previousCompletedItems = prevCompletedItems,
            events = currentEvents,
            previousEvents = prevEvents,
            weeklySnapshots = snapshots,
            allDeadlines = deadlines,
            range = range,
            now = now,
            zone = zone,
            dailyCapacityMinutes = prefs.dailyCapacityMinutes,
            quietHoursStart = prefs.quietHoursStart,
            quietHoursEnd = prefs.quietHoursEnd
        )

        InsightsUiState(
            selectedRange = range,
            summary = summary,
            selectedDayCompletion = selectedDay,
            showCookedReportDialog = showReport,
            isProUser = false, // Free tier by default; ranges beyond 7d soft-lock
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsUiState(isLoading = true)
    )

    fun onRangeSelected(range: InsightRange) {
        _selectedRange.value = range
    }

    fun onSelectDayCompletion(day: DayCompletion?) {
        _selectedDayCompletion.value = day
    }

    fun setShowCookedReport(show: Boolean) {
        _showCookedReportDialog.value = show
    }
}
