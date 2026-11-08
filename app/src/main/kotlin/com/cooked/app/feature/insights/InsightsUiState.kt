package com.cooked.app.feature.insights

import com.cooked.app.domain.model.CookedLevel
import com.cooked.app.domain.model.CookedScore

data class InsightsUiState(
    val cookedScore: CookedScore = CookedScore(
        score = 0,
        level = CookedLevel.CHILLING,
        totalRequiredHours = 0.0,
        totalAvailableHours = 28.0,
        headline = "Raw & Chilling 🧊",
        advice = "No pending overload."
    ),
    val emptyMessage: String = "Cooked Index: 0%. You are thriving, no cap.",
    val isLoading: Boolean = false
)

