package com.cooked.app.feature.home

import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.domain.model.Item
import java.time.Instant

data class TodayUiState(
    val dateTitle: String = "",
    val voiceStatus: String = "Nothing due. Suspiciously calm.",
    val overdueItems: List<Item> = emptyList(),
    val nowItems: List<Item> = emptyList(),
    val laterTodayItems: List<Item> = emptyList(),
    val tomorrowPeekItems: List<Item> = emptyList(),
    val hottestDeadlines: List<Pair<Item, HeatLevel>> = emptyList(),
    val selectedItemIds: Set<String> = emptySet(),
    val isMultiSelectMode: Boolean = false,
    val showSnoozeSheetForItemId: String? = null,
    val showBatchSnoozeSheet: Boolean = false,
    val hasExactAlarmWarning: Boolean = false,
    val isLoading: Boolean = false
) {
    val totalTodayCount: Int
        get() = overdueItems.size + nowItems.size + laterTodayItems.size

    val isEmpty: Boolean
        get() = totalTodayCount == 0 && tomorrowPeekItems.isEmpty() && hottestDeadlines.isEmpty()
}
