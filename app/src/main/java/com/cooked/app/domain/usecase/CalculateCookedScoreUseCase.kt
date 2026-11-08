package com.cooked.app.domain.usecase

import com.cooked.app.core.time.TimeUtils
import com.cooked.app.domain.model.CookedLevel
import com.cooked.app.domain.model.CookedScore
import com.cooked.app.domain.model.Reminder

class CalculateCookedScoreUseCase {

    operator fun invoke(
        activeReminders: List<Reminder>,
        nowUtcMillis: Long = TimeUtils.nowUtcMillis(),
        weeklyProductiveCapacityHours: Double = 28.0 // ~4 focused hours/day realistic capacity
    ): CookedScore {
        val sevenDaysAhead = nowUtcMillis + (7 * 24 * 60 * 60 * 1000L)

        // Filter deadlines due in the next 7 days or currently overdue
        val upcomingDeadlines = activeReminders.filter { reminder ->
            !reminder.isCompleted &&
                    reminder.dueInstant != null &&
                    reminder.dueInstant <= sevenDaysAhead
        }

        if (upcomingDeadlines.isEmpty()) {
            return CookedScore(
                score = 0,
                level = CookedLevel.CHILLING,
                totalRequiredHours = 0.0,
                totalAvailableHours = weeklyProductiveCapacityHours,
                headline = "Raw & Chilling 🧊",
                advice = "Zero pending doom on your horizon. Go drink water or start something bold."
            )
        }

        val totalRequiredMinutes = upcomingDeadlines.sumOf { reminder ->
            if (reminder.isDeadlineMode) {
                reminder.estimatedEffortMinutes.coerceAtLeast(30)
            } else {
                // Quick reminders assume 15 mins effort
                15
            }
        }
        val totalRequiredHours = totalRequiredMinutes / 60.0

        // Ratio of required focused work vs capacity
        val loadRatio = totalRequiredHours / weeklyProductiveCapacityHours
        val rawScore = (loadRatio * 100).toInt().coerceIn(0, 100)

        // Additional penalty if any item is actively overdue
        val overdueCount = upcomingDeadlines.count { it.isOverdue }
        val finalScore = (rawScore + (overdueCount * 12)).coerceIn(0, 100)

        val (level, headline, advice) = when {
            finalScore <= 25 -> Triple(
                CookedLevel.CHILLING,
                "Raw & Chilling 🧊",
                "Workload is super breezy. You're genuinely thriving right now."
            )
            finalScore <= 55 -> Triple(
                CookedLevel.SIMMERING,
                "Simmering Gently 🍲",
                "Things are heating up, but totally doable. Knock out the next item and keep your rhythm."
            )
            finalScore <= 80 -> Triple(
                CookedLevel.COOKED,
                "Properly Cooked 🔥",
                "Your schedule is getting spicy. Lock in, mute distractions, and finish the nearest deadline."
            )
            else -> Triple(
                CookedLevel.DEEP_FRIED,
                "Deep Fried & Crispy 🚨",
                "Emergency overload! Don't panic or doomscroll. Drop the non-essentials and attack one 25-minute chunk."
            )
        }

        return CookedScore(
            score = finalScore,
            level = level,
            totalRequiredHours = (kotlin.math.round(totalRequiredHours * 10) / 10.0),
            totalAvailableHours = weeklyProductiveCapacityHours,
            headline = headline,
            advice = advice
        )
    }
}

