package com.cooked.app.feature.settings

import com.cooked.app.core.designsystem.ThemeMode
import com.cooked.app.domain.backup.ImportPreview
import com.cooked.app.domain.model.Personality
import com.cooked.app.scheduling.alarm.ScheduledAlarmInfo
import com.cooked.app.scheduling.battery.ReliabilityReport
import java.time.DayOfWeek
import java.time.LocalTime

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val defaultNagIntervalMinutes: Int = 10,
    val quietHoursStart: LocalTime = LocalTime.of(22, 0),
    val quietHoursEnd: LocalTime = LocalTime.of(8, 0),
    val personality: Personality = Personality.SPICY,
    val dailyCapacityMinutes: Int = 120,
    val preferredNudgeTime: LocalTime = LocalTime.of(18, 0),
    val weekStartsOn: DayOfWeek = DayOfWeek.MONDAY,
    val isPro: Boolean = false,
    val appVersion: String = "1.0.0",
    val scheduledAlarms: List<ScheduledAlarmInfo> = emptyList(),
    val reliabilityReport: ReliabilityReport? = null,
    val importPreview: ImportPreview? = null,
    val showDeleteConfirmDialog: Boolean = false,
    val showDeleteDoubleConfirmDialog: Boolean = false,
    val showPrivacyDialog: Boolean = false,
    val snackbarMessage: String? = null
)
