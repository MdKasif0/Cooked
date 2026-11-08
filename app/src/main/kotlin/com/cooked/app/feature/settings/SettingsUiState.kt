package com.cooked.app.feature.settings

import com.cooked.app.core.designsystem.ThemeMode
import com.cooked.app.domain.model.Personality
import com.cooked.app.scheduling.alarm.ScheduledAlarmInfo
import com.cooked.app.scheduling.battery.ReliabilityReport

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultNagIntervalMinutes: Int = 10,
    val personality: Personality = Personality.SPICY,
    val isOemWarningDismissed: Boolean = false,
    val appVersion: String = "1.0.0",
    val scheduledAlarms: List<ScheduledAlarmInfo> = emptyList(),
    val reliabilityReport: ReliabilityReport? = null
)
