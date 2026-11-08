package com.cooked.app.ui.addedit

data class AddEditUiState(
    val reminderId: String? = null,
    val title: String = "",
    val note: String = "",
    val dueInstantUtc: Long? = null,
    val nagIntervalMinutes: Int = 10,
    val isDeadlineMode: Boolean = false,
    val deadlineStartInstantUtc: Long? = null,
    val estimatedEffortMinutes: Int = 60,
    val isSaved: Boolean = false,
    val canSave: Boolean = false,
    val isDeleteVisible: Boolean = false
)
