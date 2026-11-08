package com.cooked.app.feature.detail

import com.cooked.app.domain.model.PressureLevel
import com.cooked.app.domain.model.Reminder

data class ReminderDetailUiState(
    val reminder: Reminder? = null,
    val pressureLevel: PressureLevel = PressureLevel.CHILL,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false
)

