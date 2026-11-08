package com.cooked.app.domain.usecase

import com.cooked.app.core.time.TimeUtils
import com.cooked.app.domain.model.PressureLevel
import com.cooked.app.domain.model.Reminder

class CalculatePressureUseCase {

    operator fun invoke(
        reminder: Reminder,
        nowUtcMillis: Long = TimeUtils.nowUtcMillis()
    ): PressureLevel {
        if (!reminder.isDeadlineMode || reminder.dueInstant == null) {
            return if (reminder.isOverdue) PressureLevel.CRISPY else PressureLevel.CHILL
        }

        val dueInstant = reminder.dueInstant
        val startInstant = reminder.deadlineStartInstant ?: reminder.createdAt

        // If overdue
        if (nowUtcMillis >= dueInstant) {
            return PressureLevel.CRISPY
        }

        val remainingMillis = dueInstant - nowUtcMillis
        val remainingMinutes = remainingMillis / (1000 * 60)
        val effortMinutes = reminder.estimatedEffortMinutes.coerceAtLeast(15)

        // If remaining time is less than or barely equal to required effort -> CRISPY
        if (remainingMinutes <= effortMinutes) {
            return PressureLevel.CRISPY
        }

        // Total window between start and due
        val totalWindowMillis = (dueInstant - startInstant).coerceAtLeast(1)
        val elapsedMillis = (nowUtcMillis - startInstant).coerceAtLeast(0)
        val windowElapsedRatio = (elapsedMillis.toDouble() / totalWindowMillis).coerceIn(0.0, 1.0)

        // Buffer ratio: how much free time compared to effort
        val bufferRatio = remainingMinutes.toDouble() / effortMinutes

        return when {
            bufferRatio <= 1.25 || windowElapsedRatio >= 0.85 -> PressureLevel.CRISPY
            bufferRatio <= 2.0 || windowElapsedRatio >= 0.60 -> PressureLevel.HIGH_HEAT
            bufferRatio <= 3.5 || windowElapsedRatio >= 0.35 -> PressureLevel.HEATING_UP
            else -> PressureLevel.CHILL
        }
    }
}
