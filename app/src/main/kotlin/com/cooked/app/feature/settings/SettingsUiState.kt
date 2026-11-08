package com.cooked.app.feature.settings

data class SettingsUiState(
    val defaultNagIntervalMinutes: Int = 10,
    val isOemWarningDismissed: Boolean = false,
    val appVersion: String = "1.0.0"
)
