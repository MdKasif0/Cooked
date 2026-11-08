package com.cooked.app.scheduling.alarm

import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.data.datastore.UserPrefs
import com.cooked.app.domain.deadline.HeatCalculator
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

enum class AlarmType {
    PRE_START,
    FIRST,
    NAG,
    FINAL_CALL
}

data class NextAlarm(
    val time: Instant,
    val type: AlarmType
)

object AlarmPlanner {

    fun plan(
        item: Item,
        now: Instant,
        prefs: UserPrefs,
        nagCount: Int = 0,
        hasStartedRecently: Boolean = false
    ): NextAlarm? {
        // 1. Soft-deleted, completed, or archived items do not receive alarms
        if (item.isDeleted || item.status != ItemStatus.ACTIVE || item.progress >= 100) {
            return null
        }

        // 2. Active snoozed item: alarm must fire exactly at snoozedUntil
        if (item.snoozedUntil != null && item.snoozedUntil.isAfter(now)) {
            return NextAlarm(time = item.snoozedUntil, type = AlarmType.NAG)
        }

        // 3. Max nags limit reached
        if (item.nag.maxNags != null && nagCount >= item.nag.maxNags) {
            return null
        }

        val zone = try {
            ZoneId.of(item.timeZoneId)
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }

        // 4. Pre-due date scheduling
        if (now.isBefore(item.dueAt)) {
            if (item.kind == ItemKind.DEADLINE) {
                return planDeadlinePreAlert(
                    item = item,
                    now = now,
                    prefs = prefs,
                    zone = zone,
                    hasStartedRecently = hasStartedRecently
                )
            } else {
                // Standard reminder first alert at dueAt
                return NextAlarm(time = item.dueAt, type = AlarmType.FIRST)
            }
        }

        // 5. Nagging Phase (now >= dueAt)
        val intervalMinutes = item.nag.intervalMinutes.toLong().coerceAtLeast(1L)
        val baseNextTime = now.plus(Duration.ofMinutes(intervalMinutes))

        // 6. Quiet Hours Evaluation
        val fireTime = if (item.nag.respectQuietHours && !item.critical) {
            deferIfInQuietHours(
                targetInstant = baseNextTime,
                zone = zone,
                start = prefs.quietHoursStart,
                end = prefs.quietHoursEnd
            )
        } else {
            baseNextTime
        }

        // 7. Escalation classification
        val type = when {
            item.critical && nagCount >= 3 -> AlarmType.FINAL_CALL
            else -> AlarmType.NAG
        }

        return NextAlarm(time = fireTime, type = type)
    }

    private fun planDeadlinePreAlert(
        item: Item,
        now: Instant,
        prefs: UserPrefs,
        zone: ZoneId,
        hasStartedRecently: Boolean
    ): NextAlarm {
        val heatResult = HeatCalculator.calculate(
            item = item,
            now = now,
            dailyCapacityMinutes = prefs.dailyCapacityMinutes,
            quietHoursStart = prefs.quietHoursStart,
            quietHoursEnd = prefs.quietHoursEnd
        )

        val isRelaxed = hasStartedRecently || item.progress > 0
        val effectiveHeat = if (isRelaxed) HeatCalculator.relaxHeatLevel(heatResult.heatLevel) else heatResult.heatLevel

        val finalCallTime = item.dueAt.minus(Duration.ofHours(2))
        val isWithin24h = Duration.between(now, item.dueAt).toHours() <= 24

        // Cooked or within last 24h: FINAL_CALL 2h before dueAt
        if (effectiveHeat == HeatLevel.COOKED || isWithin24h) {
            if (now.isBefore(finalCallTime)) {
                return NextAlarm(time = finalCallTime, type = AlarmType.FINAL_CALL)
            }
            return NextAlarm(time = item.dueAt, type = AlarmType.FINAL_CALL)
        }

        val nudgeTime = prefs.preferredNudgeTime
        val nowZdt = now.atZone(zone)
        val today = nowZdt.toLocalDate()

        return when (effectiveHeat) {
            HeatLevel.TOASTY -> {
                // Two nudges per day: 3h earlier and at nudgeTime
                val earlyTime = nudgeTime.minusHours(3)
                val earlyToday = today.atTime(earlyTime).atZone(zone).toInstant()
                val nudgeToday = today.atTime(nudgeTime).atZone(zone).toInstant()

                val nextCandidate = when {
                    now.isBefore(earlyToday) -> earlyToday
                    now.isBefore(nudgeToday) -> nudgeToday
                    else -> today.plusDays(1).atTime(earlyTime).atZone(zone).toInstant()
                }

                if (nextCandidate.isBefore(item.dueAt)) {
                    NextAlarm(time = nextCandidate, type = AlarmType.PRE_START)
                } else {
                    NextAlarm(time = item.dueAt, type = AlarmType.FIRST)
                }
            }

            HeatLevel.WARM -> {
                // One nudge per day at nudge time
                val nudgeToday = today.atTime(nudgeTime).atZone(zone).toInstant()
                val nextCandidate = if (now.isBefore(nudgeToday)) {
                    nudgeToday
                } else {
                    today.plusDays(1).atTime(nudgeTime).atZone(zone).toInstant()
                }

                if (nextCandidate.isBefore(item.dueAt)) {
                    NextAlarm(time = nextCandidate, type = AlarmType.PRE_START)
                } else {
                    NextAlarm(time = item.dueAt, type = AlarmType.FIRST)
                }
            }

            HeatLevel.CHILL, HeatLevel.DONE -> {
                // Nudge at startBy if in the future, otherwise first alert at dueAt
                val effectiveStartBy = item.startBy ?: HeatCalculator.suggestStartBy(
                    item = item,
                    now = now,
                    dailyCapacityMinutes = prefs.dailyCapacityMinutes,
                    preferredNudgeTime = prefs.preferredNudgeTime,
                    zone = zone
                )

                if (now.isBefore(effectiveStartBy)) {
                    NextAlarm(time = effectiveStartBy, type = AlarmType.PRE_START)
                } else {
                    NextAlarm(time = item.dueAt, type = AlarmType.FIRST)
                }
            }

            HeatLevel.COOKED -> {
                NextAlarm(time = item.dueAt, type = AlarmType.FINAL_CALL)
            }
        }
    }

    /**
     * Checks if [targetInstant] falls within quiet hours and defers it to the end of quiet hours.
     */
    fun deferIfInQuietHours(
        targetInstant: Instant,
        zone: ZoneId,
        start: LocalTime,
        end: LocalTime
    ): Instant {
        val zdt = targetInstant.atZone(zone)
        val localTime = zdt.toLocalTime()

        val inQuietHours = if (start.isBefore(end)) {
            // e.g. 01:00 to 06:00
            !localTime.isBefore(start) && localTime.isBefore(end)
        } else {
            // Spans midnight, e.g. 22:00 to 08:00
            !localTime.isBefore(start) || localTime.isBefore(end)
        }

        if (!inQuietHours) {
            return targetInstant
        }

        // Defer to end of quiet hours
        val deferredZdt: ZonedDateTime = if (start.isBefore(end)) {
            zdt.toLocalDate().atTime(end).atZone(zone)
        } else {
            if (!localTime.isBefore(start)) {
                // e.g. 23:00 -> next day at 08:00
                zdt.toLocalDate().plusDays(1).atTime(end).atZone(zone)
            } else {
                // e.g. 03:00 -> today at 08:00
                zdt.toLocalDate().atTime(end).atZone(zone)
            }
        }

        return deferredZdt.toInstant()
    }
}
