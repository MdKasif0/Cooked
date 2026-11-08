package com.cooked.app.domain.rescue

import com.cooked.app.domain.deadline.HeatCalculator
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class TimeBlock(
    val start: Instant,
    val end: Instant,
    val title: String = ""
)

data class RescueSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val scheduledStart: Instant,
    val durationMinutes: Int,
    val stepDescription: String
)

data class PushRecommendation(
    val item: Item,
    val reason: String,
    val freedHours: Float
)

data class RescuePlan(
    val targetItem: Item,
    val sessions: List<RescueSession>,
    val pushRecommendations: List<PushRecommendation>,
    val totalMinutesPlanned: Int,
    val isOverloaded: Boolean
)

object RescuePlanner {

    fun generatePlan(
        target: Item,
        allItems: List<Item>,
        now: Instant = Instant.now(),
        dailyCapacityMinutes: Int = 120,
        quietHoursStart: LocalTime = LocalTime.of(22, 0),
        quietHoursEnd: LocalTime = LocalTime.of(8, 0),
        calendarBusyBlocks: List<TimeBlock> = emptyList(),
        zone: ZoneId = ZoneId.systemDefault()
    ): RescuePlan {
        val remainingEffort = if (target.effortMinutes != null && target.effortMinutes > 0) {
            val progressFraction = target.progress.coerceIn(0, 100) / 100f
            (target.effortMinutes * (1f - progressFraction)).toInt().coerceAtLeast(30)
        } else {
            300 // 5 hours default when effort unknown
        }

        val sessions = mutableListOf<RescueSession>()
        var minutesAllocated = 0

        var currentZdt = now.atZone(zone).plusMinutes(15) // Start 15m from now
        val dueZdt = target.dueAt.atZone(zone)

        val sessionDuration = 45 // 45m Pomodoro sessions
        val breakDuration = 15   // 15m buffer

        var stepIndex = 1

        while (minutesAllocated < remainingEffort && currentZdt.isBefore(dueZdt)) {
            val currentLocalTime = currentZdt.toLocalTime()

            // 1. Skip Quiet Hours
            if (isInQuietHours(currentLocalTime, quietHoursStart, quietHoursEnd)) {
                // Advance past quiet hours to quietHoursEnd
                currentZdt = currentZdt.toLocalDate().atTime(quietHoursEnd).atZone(zone)
                if (currentZdt.isBefore(now.atZone(zone))) {
                    currentZdt = currentZdt.plusDays(1)
                }
                continue
            }

            // 2. Check Calendar Busy Blocks
            val sessionEnd = currentZdt.plusMinutes(sessionDuration.toLong())
            val conflicts = calendarBusyBlocks.any { block ->
                val blockStart = block.start.atZone(zone)
                val blockEnd = block.end.atZone(zone)
                !(sessionEnd.isBefore(blockStart) || currentZdt.isAfter(blockEnd))
            }

            if (conflicts) {
                currentZdt = currentZdt.plusMinutes(30)
                continue
            }

            // 3. Create Session
            val sessionTitle = "Session $stepIndex: ${target.title}"
            val stepDesc = generateStepName(target.title, stepIndex, remainingEffort / sessionDuration)
            sessions.add(
                RescueSession(
                    title = sessionTitle,
                    scheduledStart = currentZdt.toInstant(),
                    durationMinutes = sessionDuration,
                    stepDescription = stepDesc
                )
            )

            minutesAllocated += sessionDuration
            stepIndex++

            // Advance with break
            currentZdt = currentZdt.plusMinutes((sessionDuration + breakDuration).toLong())
        }

        val isOverloaded = minutesAllocated < remainingEffort

        // 4. Recommendations to push if overloaded or tight
        val pushRecommendations = mutableListOf<PushRecommendation>()
        if (isOverloaded || sessions.size > 4) {
            val otherItems = allItems.filter {
                it.id != target.id &&
                it.kind == ItemKind.DEADLINE &&
                !it.isCompleted &&
                !it.isDeleted &&
                it.dueAt.isAfter(target.dueAt)
            }.sortedByDescending { it.dueAt }

            for (other in otherItems.take(2)) {
                val freedHours = ((other.effortMinutes ?: 120) / 60f)
                val dateFmt = DateTimeFormatter.ofPattern("EEE, dd MMM")
                pushRecommendations.add(
                    PushRecommendation(
                        item = other,
                        reason = "Push '${other.title}' to ${dateFmt.format(other.dueAt.plusSeconds(86400 * 2).atZone(zone))} to clear ~${"%.1f".format(freedHours)}h",
                        freedHours = freedHours
                    )
                )
            }
        }

        return RescuePlan(
            targetItem = target,
            sessions = sessions,
            pushRecommendations = pushRecommendations,
            totalMinutesPlanned = minutesAllocated,
            isOverloaded = isOverloaded
        )
    }

    private fun isInQuietHours(time: LocalTime, start: LocalTime, end: LocalTime): Boolean {
        return if (start.isBefore(end)) {
            time >= start && time < end
        } else {
            time >= start || time < end
        }
    }

    private fun generateStepName(title: String, index: Int, totalSteps: Int): String {
        return when (index) {
            1 -> "Outline key requirements & initial setup"
            2 -> "Core drafting & heavy problem solving"
            3 -> "Implementation & detail execution"
            4 -> "Refining, debugging & error checking"
            else -> "Final polish, proofreading & formatting"
        }
    }
}
