package com.cooked.app.feature.settings

import com.cooked.app.core.designsystem.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultNagIntervalMinutes: Int = 10,
    val isOemWarningDismissed: Boolean = false,
    val appVersion: String = "1.0.0"
)
