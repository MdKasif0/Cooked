package com.cooked.app.feature.deadlines

import com.cooked.app.domain.deadline.CookedScoreResult
import com.cooked.app.domain.deadline.HeatResult
import com.cooked.app.domain.model.Item
import java.time.Instant

data class DeadlineCardUiModel(
    val item: Item,
    val heatResult: HeatResult,
    val suggestedStartBy: Instant,
    val hasStartedRecently: Boolean
)

data class DeadlinesUiState(
    val scoreResult: CookedScoreResult? = null,
    val deadlines: List<DeadlineCardUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val emptyMessage: String = "No deadlines. Either you're on top of life or you haven't added them yet."
)
