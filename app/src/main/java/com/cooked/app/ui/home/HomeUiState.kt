package com.cooked.app.ui.home

import com.cooked.app.domain.model.CookedLevel
import com.cooked.app.domain.model.CookedScore
import com.cooked.app.domain.model.PressureLevel
import com.cooked.app.domain.model.Reminder

enum class ReminderFilter {
    ALL,
    NAGGING,
    DEADLINES,
    COMPLETED
}

data class ReminderItemUi(
    val reminder: Reminder,
    val pressureLevel: PressureLevel
)

data class HomeUiState(
    val reminders: List<ReminderItemUi> = emptyList(),
    val filteredReminders: List<ReminderItemUi> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: ReminderFilter = ReminderFilter.ALL,
    val cookedScore: CookedScore = CookedScore(
        score = 0,
        level = CookedLevel.CHILLING,
        totalRequiredHours = 0.0,
        totalAvailableHours = 28.0,
        headline = "Raw & Chilling 🧊",
        advice = "No pending doom."
    ),
    val isOemWarningVisible: Boolean = false,
    val isLoading: Boolean = true
)
