package com.cooked.app.feature.settings.reliability

import com.cooked.app.scheduling.battery.ReliabilityReport

data class ReliabilityUiState(
    val report: ReliabilityReport? = null,
    val isTestingAlarm: Boolean = false,
    val testScheduledAtEpochMillis: Long? = null,
    val testResultMessage: String? = null,
    val isLoading: Boolean = false
)

