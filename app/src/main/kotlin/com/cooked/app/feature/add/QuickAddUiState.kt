package com.cooked.app.feature.add

data class QuickAddUiState(
    val title: String = "",
    val note: String = "",
    val dueInstantUtc: Long? = null,
    val nagIntervalMinutes: Int = 10,
    val isDeadlineMode: Boolean = false,
    val estimatedEffortMinutes: Int = 60,
    val canSave: Boolean = false,
    val isSaved: Boolean = false
)

