package com.cooked.app.domain.insights

import com.cooked.app.domain.insights.model.GeneratedInsight
import com.cooked.app.domain.insights.model.SnoozeHabit
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

object InsightGenerator {

    private const val MIN_DATA_POINTS = 5

    fun generate(
        doneCount: Int,
        onTimeRate: Double,
        onTimeDelta: Double?,
        nagsPerWin: Double,
        nagsDelta: Double?,
        snoozeHabit: SnoozeHabit,
        latenessMedianMinutes: Int?,
        bestWeekday: DayOfWeek?,
        bestWeekdayOnTimeRate: Double?,
        startLeadTimeHours: Double?,
        doneCountByDay: List<Int>
    ): List<GeneratedInsight> {
        val candidates = mutableListOf<GeneratedInsight>()

        // 1. On-Time Rate Change (if minimum 5 samples and has delta)
        if (doneCount >= MIN_DATA_POINTS && onTimeDelta != null && abs(onTimeDelta) >= 0.04) {
            val deltaPoints = (onTimeDelta * 100).roundToInt()
            val (title, body) = if (deltaPoints > 0) {
                Pair(
                    "Locked In",
                    "Your on-time completion rate jumped +$deltaPoints% compared to the previous period. Genuine momentum."
                )
            } else {
                Pair(
                    "Heavy Stretch",
                    "On-time rate dipped ${abs(deltaPoints)}%. A noticeably heavier workload than usual, but you survived."
                )
            }
            candidates.add(
                GeneratedInsight(
                    id = "ontime_delta",
                    title = title,
                    body = body,
                    highlightStat = "${(onTimeRate * 100).roundToInt()}% on-time",
                    sparklinePoints = doneCountByDay.takeLast(7).map { it.toFloat() },
                    numbers = mapOf("rate" to onTimeRate, "delta" to onTimeDelta)
                )
            )
        }

        // 2. Best Weekday Rhythm (minimum 5 samples)
        if (bestWeekday != null && bestWeekdayOnTimeRate != null && doneCount >= MIN_DATA_POINTS) {
            val dayName = bestWeekday.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val ratePct = (bestWeekdayOnTimeRate * 100).roundToInt()
            candidates.add(
                GeneratedInsight(
                    id = "best_weekday",
                    title = "$dayName Peak",
                    body = "${dayName}s are your highest-performing day with an $ratePct% on-time rate. Your brain agrees with ${dayName}s.",
                    highlightStat = "$ratePct% on $dayName",
                    sparklinePoints = listOf(0.4f, 0.6f, 0.5f, 0.8f, 1.0f, 0.7f),
                    numbers = mapOf("weekday" to bestWeekday.name, "rate" to bestWeekdayOnTimeRate)
                )
            )
        }

        // 3. Nag Tolerance Reality Check (minimum 5 samples)
        if (doneCount >= MIN_DATA_POINTS && nagsPerWin > 0.0) {
            val nagsFormatted = String.format(Locale.US, "%.1f", nagsPerWin)
            val deltaText = if (nagsDelta != null && abs(nagsDelta) >= 0.2) {
                if (nagsDelta < 0) " (down ${String.format(Locale.US, "%.1f", abs(nagsDelta))} nags!)" else ""
            } else ""
            candidates.add(
                GeneratedInsight(
                    id = "nags_tolerance",
                    title = "The Nag Threshold",
                    body = "You usually cave after $nagsFormatted nags$deltaText. At least you give in before you're fully cooked.",
                    highlightStat = "~$nagsFormatted nags",
                    sparklinePoints = listOf(3f, 2.5f, 2.8f, 2.2f, nagsPerWin.toFloat()),
                    numbers = mapOf("nags" to nagsPerWin)
                )
            )
        }

        // 4. Snooze Habit Clock (minimum 5 snoozes)
        val totalSnoozes = snoozeHabit.hourlyBuckets.sum()
        if (totalSnoozes >= MIN_DATA_POINTS && snoozeHabit.peakHour != null) {
            val hour = snoozeHabit.peakHour
            val hourFormatted = formatHour(hour)
            val mostSnoozedExtra = if (!snoozeHabit.mostSnoozedItemTitle.isNullOrBlank()) {
                " Most-snoozed victim: \"${snoozeHabit.mostSnoozedItemTitle}\"."
            } else ""

            candidates.add(
                GeneratedInsight(
                    id = "snooze_peak",
                    title = "Scheduled Resistance",
                    body = "Your peak snooze window is $hourFormatted.$mostSnoozedExtra Procrastination has a strict calendar.",
                    highlightStat = "$hourFormatted peak",
                    sparklinePoints = snoozeHabit.hourlyBuckets.map { it.toFloat() }.takeLast(8),
                    numbers = mapOf("peakHour" to hour, "totalSnoozes" to totalSnoozes)
                )
            )
        }

        // 5. Deadline Start Lead Time (minimum 5 samples)
        if (startLeadTimeHours != null && startLeadTimeHours > 0.0 && doneCount >= MIN_DATA_POINTS) {
            val hours = startLeadTimeHours.roundToInt()
            candidates.add(
                GeneratedInsight(
                    id = "lead_time",
                    title = "Buffer Zone",
                    body = "You typically start working on deadlines ~$hours hours before they're due. Cutting it fine, but calculated.",
                    highlightStat = "~${hours}h buffer",
                    sparklinePoints = listOf(12f, 18f, 15f, 24f, startLeadTimeHours.toFloat()),
                    numbers = mapOf("hours" to hours)
                )
            )
        }

        // 6. Lateness Reality Check (minimum 5 late items)
        if (latenessMedianMinutes != null && latenessMedianMinutes > 0 && doneCount >= MIN_DATA_POINTS) {
            candidates.add(
                GeneratedInsight(
                    id = "lateness_median",
                    title = "The Delay Cost",
                    body = "When you're late, you're ~${latenessMedianMinutes}m late. Respectable delay, very recoverable.",
                    highlightStat = "~${latenessMedianMinutes}m late",
                    sparklinePoints = listOf(20f, 45f, 30f, 40f, latenessMedianMinutes.toFloat()),
                    numbers = mapOf("medianMinutes" to latenessMedianMinutes)
                )
            )
        }

        // Select top 3 most interesting
        return candidates.take(3)
    }

    private fun formatHour(hour: Int): String {
        return when {
            hour == 0 -> "12 AM"
            hour == 12 -> "12 PM"
            hour > 12 -> "${hour - 12} PM"
            else -> "$hour AM"
        }
    }
}
