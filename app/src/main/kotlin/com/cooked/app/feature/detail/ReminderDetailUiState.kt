package com.cooked.app.feature.detail

import com.cooked.app.domain.deadline.HeatResult
import com.cooked.app.domain.model.Item
import java.time.Instant

data class ReminderDetailUiState(
    val item: Item? = null,
    val heatResult: HeatResult? = null,
    val suggestedStartBy: Instant? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val isProgressManuallyOverridden: Boolean = false,
    val showRescheduleSheet: Boolean = false
)
