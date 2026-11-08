package com.cooked.app.domain.deadline

import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.rescue.TimeBlock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.min

data class HeatResult(
    val heatLevel: HeatLevel,
    val heatProgress: Float, // 0f..1f for smooth HeatBar interpolation
    val remainingEffortMinutes: Int,
    val availableMinutes: Int,
    val slackMinutes: Int, // can be negative if overloaded
    val reason: String
)

object HeatCalculator {

    /**
     * Calculates the urgency heat level, available waking capacity, slack minutes,
     * and a human-readable reason string for a deadline item.
     */
    fun calculate(
        item: Item,
        now: Instant,
        dailyCapacityMinutes: Int = 120,
        quietHoursStart: LocalTime = LocalTime.of(22, 0),
        quietHoursEnd: LocalTime = LocalTime.of(8, 0),
        calendarBusyBlocks: List<TimeBlock> = emptyList()
    ): HeatResult {
        val zone = try {
            ZoneId.of(item.timeZoneId)
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }

        // 1. Progress = 100% or DONE
        if (item.progress >= 100 || item.status == ItemStatus.DONE) {
            return HeatResult(
                heatLevel = HeatLevel.CHILL,
                heatProgress = 0f,
                remainingEffortMinutes = 0,
                availableMinutes = calculateAvailableMinutes(now, item.dueAt, dailyCapacityMinutes, quietHoursStart, quietHoursEnd, zone, calendarBusyBlocks),
                slackMinutes = 0,
                reason = "Done and dusted! 🧊"
            )
        }

        // 2. Remaining effort
        val effort = (item.effortMinutes ?: 0).coerceAtLeast(0)
        val remainingEffort = (effort * (1.0 - (item.progress.coerceIn(0, 100) / 100.0))).coerceAtLeast(0.0)
        val remainingEffortMinutes = ceil(remainingEffort).toInt()

        // 3. Available waking minutes until dueAt
        val availableMinutes = calculateAvailableMinutes(
            now = now,
            dueAt = item.dueAt,
            dailyCapacityMinutes = dailyCapacityMinutes,
            quietHoursStart = quietHoursStart,
            quietHoursEnd = quietHoursEnd,
            zone = zone,
            calendarBusyBlocks = calendarBusyBlocks
        )

        // 4. Overdue check
        val isOverdue = now.isAfter(item.dueAt) || now == item.dueAt
        if (isOverdue) {
            val lateDuration = Duration.between(item.dueAt, now)
            val lateFormatted = formatDuration(lateDuration)
            return HeatResult(
                heatLevel = HeatLevel.COOKED,
                heatProgress = 1f,
                remainingEffortMinutes = remainingEffortMinutes,
                availableMinutes = 0,
                slackMinutes = -remainingEffortMinutes,
                reason = "Overdue by $lateFormatted! Maximum heat 🔥"
            )
        }

        val hasEffort = item.effortMinutes != null && item.effortMinutes > 0

        if (hasEffort) {
            // Ratio = remainingEffort / availableMinutes
            val slack = availableMinutes - remainingEffortMinutes
            val ratio = if (availableMinutes <= 0) {
                999.0
            } else {
                remainingEffort / availableMinutes.toDouble()
            }

            val heatLevel = when {
                ratio < 0.40 -> HeatLevel.CHILL
                ratio < 0.70 -> HeatLevel.WARM
                ratio < 1.00 -> HeatLevel.TOASTY
                else -> HeatLevel.COOKED
            }

            val heatProgress = (ratio / 1.0).toFloat().coerceIn(0f, 1f)

            val dueDayName = item.dueAt.atZone(zone).dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val effortHoursStr = formatHoursMinutes(remainingEffortMinutes)
            val availableHoursStr = formatHoursMinutes(availableMinutes)
            val reason = "~$effortHoursStr of work, ~$availableHoursStr free before $dueDayName"

            return HeatResult(
                heatLevel = heatLevel,
                heatProgress = heatProgress,
                remainingEffortMinutes = remainingEffortMinutes,
                availableMinutes = availableMinutes,
                slackMinutes = slack,
                reason = reason
            )
        } else {
            // Effort unknown: calculate heat purely by time left
            val durationLeft = Duration.between(now, item.dueAt)
            val hoursLeft = durationLeft.toHours()

            val heatLevel = when {
                hoursLeft > 7 * 24 -> HeatLevel.CHILL
                hoursLeft >= 3 * 24 -> HeatLevel.WARM
                hoursLeft >= 24 -> HeatLevel.TOASTY
                else -> HeatLevel.COOKED
            }

            val daysLeft = durationLeft.toMinutes() / (24 * 60.0)
            val heatProgress = (1.0 - (daysLeft / 7.0)).toFloat().coerceIn(0f, 1f)
            val timeFormatted = formatDuration(durationLeft)

            return HeatResult(
                heatLevel = heatLevel,
                heatProgress = heatProgress,
                remainingEffortMinutes = 0,
                availableMinutes = availableMinutes,
                slackMinutes = availableMinutes,
                reason = "Due in $timeFormatted (no effort estimate)"
            )
        }
    }

    /**
     * Calculates waking minutes available between [now] and [dueAt] excluding quiet hours,
     * capped at [dailyCapacityMinutes] per day.
     */
    fun calculateAvailableMinutes(
        now: Instant,
        dueAt: Instant,
        dailyCapacityMinutes: Int,
        quietHoursStart: LocalTime,
        quietHoursEnd: LocalTime,
        zone: ZoneId,
        calendarBusyBlocks: List<TimeBlock> = emptyList()
    ): Int {
        if (!now.isBefore(dueAt)) return 0
        if (dailyCapacityMinutes <= 0) return 0

        val startZdt = now.atZone(zone)
        val endZdt = dueAt.atZone(zone)

        var currentDate = startZdt.toLocalDate()
        val lastDate = endZdt.toLocalDate()
        var totalAvailable = 0L

        while (!currentDate.isAfter(lastDate)) {
            val dayStartInstant = currentDate.atStartOfDay(zone).toInstant()
            val dayEndInstant = currentDate.plusDays(1).atStartOfDay(zone).toInstant()

            val windowStart = if (now.isAfter(dayStartInstant)) now else dayStartInstant
            val windowEnd = if (dueAt.isBefore(dayEndInstant)) dueAt else dayEndInstant

            if (windowStart.isBefore(windowEnd)) {
                val wakingMinutes = calculateWakingMinutesInWindow(
                    windowStart = windowStart,
                    windowEnd = windowEnd,
                    currentDate = currentDate,
                    quietHoursStart = quietHoursStart,
                    quietHoursEnd = quietHoursEnd,
                    zone = zone
                )

                // Subtract calendar busy overlap in this day window
                val busyMinutes = calendarBusyBlocks.sumOf { block ->
                    val bStart = if (block.start.isAfter(windowStart)) block.start else windowStart
                    val bEnd = if (block.end.isBefore(windowEnd)) block.end else windowEnd
                    if (bStart.isBefore(bEnd)) {
                        Duration.between(bStart, bEnd).toMinutes()
                    } else 0L
                }

                val freeWakingMinutes = (wakingMinutes - busyMinutes).coerceAtLeast(0L)
                val dayCapacity = min(dailyCapacityMinutes.toLong(), freeWakingMinutes)
                totalAvailable += dayCapacity
            }

            currentDate = currentDate.plusDays(1)
        }

        return totalAvailable.toInt()
    }

    private fun calculateWakingMinutesInWindow(
        windowStart: Instant,
        windowEnd: Instant,
        currentDate: LocalDate,
        quietHoursStart: LocalTime,
        quietHoursEnd: LocalTime,
        zone: ZoneId
    ): Long {
        if (quietHoursStart == quietHoursEnd) {
            // No quiet hours defined
            return Duration.between(windowStart, windowEnd).toMinutes().coerceAtLeast(0)
        }

        val spansMidnight = !quietHoursStart.isBefore(quietHoursEnd) // e.g. 22:00 to 08:00

        if (spansMidnight) {
            // Waking window on day D is between quietHoursEnd and quietHoursStart (e.g. 08:00 to 22:00)
            val wakingStart = currentDate.atTime(quietHoursEnd).atZone(zone).toInstant()
            val wakingEnd = currentDate.atTime(quietHoursStart).atZone(zone).toInstant()

            val overlapStart = if (windowStart.isAfter(wakingStart)) windowStart else wakingStart
            val overlapEnd = if (windowEnd.isBefore(wakingEnd)) windowEnd else wakingEnd

            return if (overlapStart.isBefore(overlapEnd)) {
                Duration.between(overlapStart, overlapEnd).toMinutes()
            } else {
                0L
            }
        } else {
            // Quiet hours during day e.g. 01:00 to 06:00
            // Waking is [00:00, 01:00] and [06:00, 24:00]
            val quietStart = currentDate.atTime(quietHoursStart).atZone(zone).toInstant()
            val quietEnd = currentDate.atTime(quietHoursEnd).atZone(zone).toInstant()

            var waking = 0L
            // Segment 1: start of day to quietStart
            val s1Start = windowStart
            val s1End = if (windowEnd.isBefore(quietStart)) windowEnd else quietStart
            if (s1Start.isBefore(s1End)) {
                waking += Duration.between(s1Start, s1End).toMinutes()
            }

            // Segment 2: quietEnd to end of window
            val s2Start = if (windowStart.isAfter(quietEnd)) windowStart else quietEnd
            val s2End = windowEnd
            if (s2Start.isBefore(s2End)) {
                waking += Duration.between(s2Start, s2End).toMinutes()
            }

            return waking
        }
    }

    /**
     * Suggests a start-by Instant based on effort and daily capacity:
     * dueAt - ceil(remainingEffort / dailyCapacity) days - 1 day buffer,
     * at preferredNudgeTime (default 18:00), never in the past (clamped to now + 1 hour).
     */
    fun suggestStartBy(
        item: Item,
        now: Instant,
        dailyCapacityMinutes: Int = 120,
        preferredNudgeTime: LocalTime = LocalTime.of(18, 0),
        zone: ZoneId? = null
    ): Instant {
        val resolvedZone = zone ?: try {
            ZoneId.of(item.timeZoneId)
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }

        val effort = (item.effortMinutes ?: 0).coerceAtLeast(0)
        val remainingEffort = (effort * (1.0 - (item.progress.coerceIn(0, 100) / 100.0))).coerceAtLeast(0.0)
        val capacity = dailyCapacityMinutes.coerceAtLeast(30)
        val daysNeeded = ceil(remainingEffort / capacity.toDouble()).toLong().coerceAtLeast(1L)
        val totalDaysBack = daysNeeded + 1L // plus 1-day buffer

        val dueZdt = item.dueAt.atZone(resolvedZone)
        val tentativeDate = dueZdt.toLocalDate().minusDays(totalDaysBack)
        val tentativeStartBy = tentativeDate.atTime(preferredNudgeTime).atZone(resolvedZone).toInstant()

        val minAllowed = now.plus(Duration.ofHours(1))
        return if (tentativeStartBy.isBefore(minAllowed)) {
            minAllowed
        } else if (tentativeStartBy.isAfter(item.dueAt)) {
            minAllowed
        } else {
            tentativeStartBy
        }
    }

    /**
     * Relaxes heat level by one step for 24h if STARTED was logged or progress > 0.
     */
    fun relaxHeatLevel(level: HeatLevel): HeatLevel {
        return when (level) {
            HeatLevel.COOKED -> HeatLevel.TOASTY
            HeatLevel.TOASTY -> HeatLevel.WARM
            HeatLevel.WARM -> HeatLevel.CHILL
            HeatLevel.CHILL, HeatLevel.DONE -> HeatLevel.CHILL
        }
    }

    private fun formatHoursMinutes(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0 && m > 0) "${h}h ${m}m"
        else if (h > 0) "${h}h"
        else "${m}m"
    }

    private fun formatDuration(duration: Duration): String {
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60

        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes.coerceAtLeast(1)}m"
        }
    }
}

