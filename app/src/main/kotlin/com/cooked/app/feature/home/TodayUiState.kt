package com.cooked.app.feature.home

import com.cooked.app.domain.model.CookedScore
import com.cooked.app.domain.model.Reminder

data class TodayUiState(
    val reminders: List<Reminder> = emptyList(),
    val cookedScore: CookedScore? = null,
    val isLoading: Boolean = false,
    val emptyMessage: String = "Nothing due. Suspiciously calm."
)
