package com.cooked.app.domain.insights.model

import com.cooked.app.domain.model.Item
import java.time.DayOfWeek
import java.time.LocalDate

data class DayCompletion(
    val date: LocalDate,
    val count: Int,
    val items: List<Item> = emptyList()
)

data class AnalyticsSummary(
    val range: InsightRange,
    val onTimeRate: Double,
    val onTimeDelta: Double?,
    val onTimeDeadlinesRate: Double,
    val onTimeRemindersRate: Double,
    val doneCount: Int,
    val doneDelta: Int?,
    val doneByDay: List<DayCompletion>,
    val nagsPerWin: Double,
    val nagsDelta: Double?,
    val snoozeHabit: SnoozeHabit,
    val latenessMedianMinutes: Int?,
    val bestWeekday: DayOfWeek?,
    val bestWeekdayOnTimeRate: Double?,
    val cookedScoreHistory: List<WeeklyScorePoint>,
    val startLeadTimeHours: Double?,
    val insights: List<GeneratedInsight>
) {
    val totalSamples: Int
        get() = doneCount
}
