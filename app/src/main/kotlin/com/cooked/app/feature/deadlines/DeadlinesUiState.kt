package com.cooked.app.feature.deadlines

import com.cooked.app.domain.model.CookedScore
import com.cooked.app.domain.model.PressureLevel
import com.cooked.app.domain.model.Reminder

data class DeadlineItem(
    val reminder: Reminder,
    val pressureLevel: PressureLevel,
    val remainingMillis: Long
)

data class DeadlinesUiState(
    val deadlines: List<DeadlineItem> = emptyList(),
    val cookedScore: CookedScore? = null,
    val isLoading: Boolean = false,
    val emptyMessage: String = "Zero hard deadlines on the radar. Don't jinx it."
)
