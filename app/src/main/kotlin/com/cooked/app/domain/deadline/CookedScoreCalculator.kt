package com.cooked.app.domain.deadline

import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import java.time.Duration
import java.time.Instant
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class DeadlineContributor(
    val item: Item,
    val remainingEffortMinutes: Int,
    val isOverdue: Boolean,
    val loadShare: Double // percentage contribution to total workload
)

data class CookedScoreResult(
    val score: Int, // 0..100
    val load: Double,
    val label: String, // "Chillin'", "Warming up", "Toasty", "Cooked"
    val heatLevel: HeatLevel,
    val activeDeadlinesCount: Int,
    val overdueCount: Int,
    val totalRemainingEffortMinutes: Int,
    val totalCapacityMinutes: Int, // 7 * dailyCapacity
    val topContributors: List<DeadlineContributor>,
    val suggestedAction: String?,
    val workloadWarning: String?,
    val simulatedScoreAfterAction: Int?
)

object CookedScoreCalculator {

    fun calculate(
        items: List<Item>,
        now: Instant,
        dailyCapacityMinutes: Int = 120
    ): CookedScoreResult {
        val totalCapacity = (7 * dailyCapacityMinutes).coerceAtLeast(60)

        // Filter active deadlines
        val activeDeadlines = items.filter {
            it.kind == ItemKind.DEADLINE &&
                it.status == ItemStatus.ACTIVE &&
                !it.isDeleted &&
                it.progress < 100
        }

        val in7DaysWindow = now.plus(Duration.ofDays(7))

        var totalRemainingEffort = 0
        var overdueCount = 0
        val contributorsList = mutableListOf<DeadlineContributor>()

        for (item in activeDeadlines) {
            val isOverdue = now.isAfter(item.dueAt)
            val isDueInNext7Days = !item.dueAt.isAfter(in7DaysWindow)

            if (isOverdue) {
                overdueCount++
            }

            if (isDueInNext7Days || isOverdue) {
                val effort = (item.effortMinutes ?: defaultEffortMinutesFor(item, now)).coerceAtLeast(30)
                val remaining = ceil(effort * (1.0 - (item.progress.coerceIn(0, 100) / 100.0))).toInt()
                totalRemainingEffort += remaining
                contributorsList.add(
                    DeadlineContributor(
                        item = item,
                        remainingEffortMinutes = remaining,
                        isOverdue = isOverdue,
                        loadShare = 0.0 // computed after total is known
                    )
                )
            }
        }

        // load = sum(remainingEffort) / (7 * dailyCapacity) + 0.15 * overdueCount
        val rawLoad = (totalRemainingEffort.toDouble() / totalCapacity.toDouble()) + (0.15 * overdueCount)
        val score = (rawLoad * 100).roundToInt().coerceIn(0, 100)

        val (label, heatLevel) = when (score) {
            in 0..39 -> "Chillin'" to HeatLevel.CHILL
            in 40..69 -> "Warming up" to HeatLevel.WARM
            in 70..89 -> "Toasty" to HeatLevel.TOASTY
            else -> "Cooked" to HeatLevel.COOKED
        }

        // Compute contributor shares and sort descending
        val sortedContributors = contributorsList.map { c ->
            val share = if (totalRemainingEffort > 0) {
                c.remainingEffortMinutes.toDouble() / totalRemainingEffort.toDouble()
            } else 0.0
            c.copy(loadShare = share)
        }.sortedWith(
            compareByDescending<DeadlineContributor> { it.isOverdue }
                .thenByDescending { it.remainingEffortMinutes }
        )

        // Simulate suggested action: simulate 1 day of progress on the biggest contributor
        val (suggestedAction, simulatedScore) = computeSuggestedAction(
            contributors = sortedContributors,
            currentScore = score,
            totalRemainingEffort = totalRemainingEffort,
            overdueCount = overdueCount,
            totalCapacity = totalCapacity,
            dailyCapacity = dailyCapacityMinutes
        )

        // Workload warning when load > 1.0
        val workloadWarning = if (rawLoad > 1.0 && sortedContributors.isNotEmpty()) {
            val count = sortedContributors.size
            val workHours = formatHours(totalRemainingEffort / 60.0)
            val freeHours = formatHours(totalCapacity / 60.0)
            "$count deadlines this week, ~$workHours of work, ~$freeHours free. Something's gotta give."
        } else {
            null
        }

        return CookedScoreResult(
            score = score,
            load = rawLoad,
            label = label,
            heatLevel = heatLevel,
            activeDeadlinesCount = activeDeadlines.size,
            overdueCount = overdueCount,
            totalRemainingEffortMinutes = totalRemainingEffort,
            totalCapacityMinutes = totalCapacity,
            topContributors = sortedContributors.take(5),
            suggestedAction = suggestedAction,
            workloadWarning = workloadWarning,
            simulatedScoreAfterAction = simulatedScore
        )
    }

    private fun computeSuggestedAction(
        contributors: List<DeadlineContributor>,
        currentScore: Int,
        totalRemainingEffort: Int,
        overdueCount: Int,
        totalCapacity: Int,
        dailyCapacity: Int
    ): Pair<String?, Int?> {
        if (contributors.isEmpty() || currentScore == 0) {
            return Pair("You're all clear! Enjoy the chill 🧊", 0)
        }

        val topContributor = contributors.first()
        val effortDeduction = min(dailyCapacity, topContributor.remainingEffortMinutes)
        val simulatedRemainingEffort = max(0, totalRemainingEffort - effortDeduction)
        val simulatedOverdueCount = if (topContributor.isOverdue && topContributor.remainingEffortMinutes <= dailyCapacity) {
            max(0, overdueCount - 1)
        } else {
            overdueCount
        }

        val simulatedLoad = (simulatedRemainingEffort.toDouble() / totalCapacity.toDouble()) + (0.15 * simulatedOverdueCount)
        val simulatedScore = (simulatedLoad * 100).roundToInt().coerceIn(0, 100)

        val targetLabel = when (simulatedScore) {
            in 0..39 -> "Chillin'"
            in 40..69 -> "Warming up"
            in 70..89 -> "Toasty"
            else -> "Cooked"
        }

        val actionText = if (simulatedScore < currentScore) {
            "Start \"${topContributor.item.title}\" today to drop to $targetLabel ($simulatedScore)"
        } else {
            "Start \"${topContributor.item.title}\" today to stay ahead"
        }

        return Pair(actionText, simulatedScore)
    }

    private fun defaultEffortMinutesFor(item: Item, now: Instant): Int {
        val hoursUntilDue = Duration.between(now, item.dueAt).toHours().coerceAtLeast(1)
        return when {
            hoursUntilDue <= 24 -> 120
            hoursUntilDue <= 72 -> 180
            else -> 240
        }
    }

    private fun formatHours(hours: Double): String {
        return if (hours % 1.0 == 0.0) "${hours.toInt()}h" else String.format(java.util.Locale.US, "%.1fh", hours)
    }
}

