package com.cooked.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.deadline.HeatCalculator
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.repository.ItemRepository
import com.cooked.app.scheduling.battery.ReliabilityChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val reliabilityChecker: ReliabilityChecker,
    private val clock: CookedClock
) : ViewModel() {

    private val _selectedItemIds = MutableStateFlow<Set<String>>(emptySet())
    private val _showSnoozeSheetForItemId = MutableStateFlow<String?>(null)
    private val _showBatchSnoozeSheet = MutableStateFlow(false)
    private val _showCelebration = MutableStateFlow(false)

    val uiState: StateFlow<TodayUiState> = combine(
        itemRepository.observeToday(),
        itemRepository.observeOverdue(),
        itemRepository.observeUpcoming(limit = 20),
        itemRepository.observeDeadlines(),
        userPreferencesRepository.userPrefs,
        _selectedItemIds,
        _showSnoozeSheetForItemId,
        _showBatchSnoozeSheet,
        _showCelebration
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val todayItems = args[0] as List<Item>
        @Suppress("UNCHECKED_CAST")
        val overdueItemsRaw = args[1] as List<Item>
        @Suppress("UNCHECKED_CAST")
        val upcomingItems = args[2] as List<Item>
        @Suppress("UNCHECKED_CAST")
        val allDeadlines = args[3] as List<Item>
        val prefs = args[4] as com.cooked.app.data.datastore.UserPrefs
        @Suppress("UNCHECKED_CAST")
        val selectedIds = args[5] as Set<String>
        val snoozeItemId = args[6] as String?
        val showBatchSnooze = args[7] as Boolean
        val showCelebration = args[8] as Boolean

        val now = clock.now()
        val zone = clock.zone()
        val zdt = now.atZone(zone)
        val today = zdt.toLocalDate()

        val dateTitle = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()).format(zdt)
        val endOfToday = today.plusDays(1).atStartOfDay(zone).toInstant().minusMillis(1)
        val startOfTomorrow = today.plusDays(1).atStartOfDay(zone).toInstant()
        val endOfTomorrow = today.plusDays(2).atStartOfDay(zone).toInstant().minusMillis(1)
        val oneHourFromNow = now.plus(Duration.ofHours(1))

        // 1. Overdue Items
        val overdueItems = overdueItemsRaw.filter { it.status == ItemStatus.ACTIVE && it.dueAt.isBefore(now) }

        // 2. Now Items (due in < 1h)
        val nowItems = todayItems.filter {
            it.status == ItemStatus.ACTIVE &&
                    !it.dueAt.isBefore(now) &&
                    !it.dueAt.isAfter(oneHourFromNow)
        }

        // 3. Later Today Items (due after 1h and before end of today)
        val laterTodayItems = todayItems.filter {
            it.status == ItemStatus.ACTIVE &&
                    it.dueAt.isAfter(oneHourFromNow) &&
                    !it.dueAt.isAfter(endOfToday)
        }

        // 4. Tomorrow Peek Items (max 3)
        val tomorrowPeekItems = upcomingItems.filter {
            it.status == ItemStatus.ACTIVE &&
                    it.dueAt.isAfter(startOfTomorrow) &&
                    !it.dueAt.isAfter(endOfTomorrow)
        }.take(3)

        // 5. Top 2 Hottest Deadlines
        val activeDeadlines = allDeadlines.filter { it.status == ItemStatus.ACTIVE }
        val hottestDeadlines = activeDeadlines.map { deadline ->
            val heat = HeatCalculator.calculate(
                item = deadline,
                now = now,
                dailyCapacityMinutes = prefs.dailyCapacityMinutes,
                quietHoursStart = prefs.quietHoursStart,
                quietHoursEnd = prefs.quietHoursEnd
            )
            Pair(deadline, heat.heatLevel)
        }.sortedByDescending { it.second.ordinal }
            .take(2)

        // 6. Voice Status Line
        val totalTodayCount = overdueItems.size + nowItems.size + laterTodayItems.size
        val voiceStatus = when {
            overdueItems.size == 1 -> "1 overdue. Deep breath, knock it out."
            overdueItems.size > 1 -> "${overdueItems.size} items overdue. Don't panic, just pick one."
            totalTodayCount == 0 -> "Nothing due. Suspiciously calm."
            totalTodayCount == 1 -> "1 thing today. Very doable."
            totalTodayCount in 2..4 -> "$totalTodayCount things today. Very doable."
            else -> "$totalTodayCount items today. Pace yourself, you got this."
        }

        // 7. Exact Alarm Check
        val canScheduleExact = try {
            reliabilityChecker.check().canScheduleExactAlarms
        } catch (_: Exception) {
            true
        }

        TodayUiState(
            dateTitle = dateTitle,
            voiceStatus = voiceStatus,
            overdueItems = overdueItems,
            nowItems = nowItems,
            laterTodayItems = laterTodayItems,
            tomorrowPeekItems = tomorrowPeekItems,
            hottestDeadlines = hottestDeadlines,
            selectedItemIds = selectedIds,
            isMultiSelectMode = selectedIds.isNotEmpty(),
            showSnoozeSheetForItemId = snoozeItemId,
            showBatchSnoozeSheet = showBatchSnooze,
            hasExactAlarmWarning = !canScheduleExact,
            showCelebration = showCelebration,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodayUiState(isLoading = true)
    )

    fun onDone(itemId: String) {
        viewModelScope.launch {
            val item = itemRepository.getItem(itemId)
            if (item != null) {
                val now = clock.now()
                val isUrgent = item.isOverdue || item.critical ||
                    (item.kind == ItemKind.DEADLINE && HeatCalculator.calculate(item, now).heatLevel in listOf(HeatLevel.TOASTY, HeatLevel.COOKED))
                if (isUrgent) {
                    _showCelebration.value = true
                }
            }
            itemRepository.markDone(itemId, clock.now())
        }
    }

    fun dismissCelebration() {
        _showCelebration.value = false
    }

    fun onSnooze(itemId: String, until: Instant) {
        viewModelScope.launch {
            itemRepository.snooze(itemId, until)
            _showSnoozeSheetForItemId.value = null
        }
    }

    fun onDelete(itemId: String) {
        viewModelScope.launch {
            itemRepository.delete(itemId)
        }
    }

    fun openSnoozeSheet(itemId: String) {
        _showSnoozeSheetForItemId.value = itemId
    }

    fun dismissSnoozeSheet() {
        _showSnoozeSheetForItemId.value = null
    }

    fun openBatchSnoozeSheet() {
        _showBatchSnoozeSheet.value = true
    }

    fun dismissBatchSnoozeSheet() {
        _showBatchSnoozeSheet.value = false
    }

    fun toggleSelection(itemId: String) {
        _selectedItemIds.update { set ->
            if (set.contains(itemId)) set - itemId else set + itemId
        }
    }

    fun clearSelection() {
        _selectedItemIds.value = emptySet()
    }

    fun onBatchDone() {
        val ids = _selectedItemIds.value
        viewModelScope.launch {
            ids.forEach { itemRepository.markDone(it, clock.now()) }
            clearSelection()
        }
    }

    fun onBatchSnooze(until: Instant) {
        val ids = _selectedItemIds.value
        viewModelScope.launch {
            ids.forEach { itemRepository.snooze(it, until) }
            clearSelection()
            _showBatchSnoozeSheet.value = false
        }
    }

    fun onBatchDelete() {
        val ids = _selectedItemIds.value
        viewModelScope.launch {
            ids.forEach { itemRepository.delete(it) }
            clearSelection()
        }
    }
}
