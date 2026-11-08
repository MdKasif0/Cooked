package com.cooked.app.feature.insights

import com.cooked.app.domain.insights.model.AnalyticsSummary
import com.cooked.app.domain.insights.model.DayCompletion
import com.cooked.app.domain.insights.model.InsightRange

data class InsightsUiState(
    val selectedRange: InsightRange = InsightRange.WEEK_7D,
    val summary: AnalyticsSummary? = null,
    val selectedDayCompletion: DayCompletion? = null,
    val showCookedReportDialog: Boolean = false,
    val isProUser: Boolean = false,
    val isLoading: Boolean = false
) {
    val isSoftLocked: Boolean
        get() = selectedRange.isPro && !isProUser
}
