package com.cooked.app.ui.deadline

import com.cooked.app.domain.model.CookedLevel
import com.cooked.app.domain.model.CookedScore
import com.cooked.app.domain.model.PressureLevel
import com.cooked.app.domain.model.Reminder

data class DeadlineItemUi(
    val reminder: Reminder,
    val pressureLevel: PressureLevel,
    val remainingMillis: Long,
    val bufferHours: Double
)

data class DeadlineUiState(
    val deadlines: List<DeadlineItemUi> = emptyList(),
    val cookedScore: CookedScore = CookedScore(
        score = 0,
        level = CookedLevel.CHILLING,
        totalRequiredHours = 0.0,
        totalAvailableHours = 28.0,
        headline = "Raw & Chilling",
        advice = "No pending deadlines."
    ),
    val isLoading: Boolean = true
)

