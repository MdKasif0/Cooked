package com.cooked.app.domain.insights.model

import java.time.LocalDate

data class WeeklyScorePoint(
    val weekStart: LocalDate,
    val weekLabel: String,
    val score: Int,
    val isCurrentWeek: Boolean
)

