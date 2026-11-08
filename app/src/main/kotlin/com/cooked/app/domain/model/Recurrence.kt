package com.cooked.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import kotlin.math.min

@Serializable
data class NthWeekday(
    val ordinal: Int, // 1, 2, 3, 4, or -1 for last
    val dayOfWeek: Int // 1..7 for Monday..Sunday
)

@Serializable
sealed class Recurrence {

    @Serializable
    @SerialName("daily")
    data object Daily : Recurrence()

    @Serializable
    @SerialName("weekdays")
    data object Weekdays : Recurrence()

    @Serializable
    @SerialName("weekly")
    data class Weekly(
        val days: Set<Int> // 1..7 (DayOfWeek.MONDAY.value .. DayOfWeek.SUNDAY.value)
    ) : Recurrence()

    @Serializable
    @SerialName("every_n_days")
    data class EveryNDays(
        val n: Int
    ) : Recurrence() {
        init {
            require(n > 0) { "n must be strictly positive" }
        }
    }

    @Serializable
    @SerialName("monthly")
    data class Monthly(
        val dayOfMonth: Int? = null,
        val nthWeekday: NthWeekday? = null
    ) : Recurrence()

    @Serializable
    @SerialName("yearly")
    data object Yearly : Recurrence()

    /**
     * Calculates the next occurrence strictly after [from] in the given [zoneId].
     * Properly respects DST transitions and month-end date clamping.
     */
    fun nextOccurrence(from: Instant, zoneId: ZoneId): Instant {
        val currentZdt = from.atZone(zoneId)

        val nextZdt: ZonedDateTime = when (this) {
            is Daily -> {
                currentZdt.plusDays(1)
            }
            is Weekdays -> {
                var candidate = currentZdt.plusDays(1)
                while (candidate.dayOfWeek == DayOfWeek.SATURDAY || candidate.dayOfWeek == DayOfWeek.SUNDAY) {
                    candidate = candidate.plusDays(1)
                }
                candidate
            }
            is Weekly -> {
                require(days.isNotEmpty()) { "Weekly recurrence must specify at least one day" }
                val targetDays = days.map { DayOfWeek.of(it) }.toSet()
                var candidate = currentZdt.plusDays(1)
                while (!targetDays.contains(candidate.dayOfWeek)) {
                    candidate = candidate.plusDays(1)
                }
                candidate
            }
            is EveryNDays -> {
                currentZdt.plusDays(n.toLong())
            }
            is Monthly -> {
                if (nthWeekday != null) {
                    // e.g. 2nd Tuesday of next month or last Friday of next month
                    val targetDow = DayOfWeek.of(nthWeekday.dayOfWeek)
                    val nextMonthBase = currentZdt.plusMonths(1).withDayOfMonth(1)
                    val adjuster = if (nthWeekday.ordinal == -1) {
                        TemporalAdjusters.lastInMonth(targetDow)
                    } else {
                        TemporalAdjusters.dayOfWeekInMonth(nthWeekday.ordinal, targetDow)
                    }
                    nextMonthBase.with(adjuster)
                } else {
                    // Month-end clamping (e.g. Jan 31 -> Feb 28/29 -> Mar 31 -> Apr 30)
                    val targetDay = dayOfMonth ?: currentZdt.dayOfMonth
                    val nextMonthCandidate = currentZdt.plusMonths(1)
                    val maxDaysInMonth = nextMonthCandidate.toLocalDate().lengthOfMonth()
                    val clampedDay = min(targetDay, maxDaysInMonth)
                    nextMonthCandidate.withDayOfMonth(clampedDay)
                }
            }
            is Yearly -> {
                // Preserves month and day, clamping Feb 29 -> Feb 28 on non-leap years
                currentZdt.plusYears(1)
            }
        }

        return nextZdt.toInstant()
    }
}

