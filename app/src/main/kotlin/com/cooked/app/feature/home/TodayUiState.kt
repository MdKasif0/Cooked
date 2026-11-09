package com.cooked.app.feature.home

import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.domain.model.Item
import java.time.Instant

data class TodayUiState(
    val timeOfDayGreeting: String = "Late night",
    val dateTitle: String = "",
    val greetingHeadline: String = "Nothing on fire today.\nEnjoy it.",
    val greetingHighlight: String = "Enjoy it.",
    val voiceStatus: String = "Nothing due. Suspiciously calm.",
    val overdueItems: List<Item> = emptyList(),
    val nowItems: List<Item> = emptyList(),
    val laterTodayItems: List<Item> = emptyList(),
    val upcomingItems: List<Item> = emptyList(),
    val tomorrowPeekItems: List<Item> = emptyList(),
    val completedTodayItems: List<Item> = emptyList(),
    val completedTodayCount: Int = 0,
    val hottestDeadlines: List<Pair<Item, HeatLevel>> = emptyList(),
    val selectedItemIds: Set<String> = emptySet(),
    val isMultiSelectMode: Boolean = false,
    val showSnoozeSheetForItemId: String? = null,
    val showBatchSnoozeSheet: Boolean = false,
    val hasExactAlarmWarning: Boolean = false,
    val showCelebration: Boolean = false,
    val isLoading: Boolean = false
) {
    val totalTodayDueCount: Int
        get() = overdueItems.size + nowItems.size + laterTodayItems.size

    val totalTodayCount: Int
        get() = totalTodayDueCount

    val totalActiveToday: Int
        get() = nowItems.size + laterTodayItems.size

    val donePercentage: Int
        get() {
            val total = totalTodayDueCount + completedTodayCount
            return if (total > 0) {
                ((completedTodayCount * 100) / total).coerceIn(0, 100)
            } else if (completedTodayCount > 0) {
                100
            } else {
                0
            }
        }

    val isEmpty: Boolean
        get() = totalTodayDueCount == 0 && upcomingItems.isEmpty() && hottestDeadlines.isEmpty() && completedTodayCount == 0
}
