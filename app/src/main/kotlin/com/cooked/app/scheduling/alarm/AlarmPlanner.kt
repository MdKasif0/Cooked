package com.cooked.app.scheduling.alarm

import com.cooked.app.data.datastore.UserPrefs
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
        nagCount: Int = 0
    ): NextAlarm? {
        // 1. Soft-deleted, completed, or archived items do not receive alarms
        if (item.isDeleted || item.status != ItemStatus.ACTIVE) {
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

        // 4. Pre-due date checks
        if (now.isBefore(item.dueAt)) {
            // Deadline pre-start alert (Step 5 feature hook)
            if (item.kind == ItemKind.DEADLINE && item.startBy != null && now.isBefore(item.startBy)) {
                return NextAlarm(time = item.startBy, type = AlarmType.PRE_START)
            }
            // Standard first alert at dueAt
            return NextAlarm(time = item.dueAt, type = AlarmType.FIRST)
        }

        // 5. Nagging Phase (now >= dueAt)
        val intervalMinutes = item.nag.intervalMinutes.toLong().coerceAtLeast(1L)
        val baseNextTime = if (nagCount == 0 && now.isAfter(item.dueAt)) {
            now.plus(Duration.ofMinutes(intervalMinutes))
        } else {
            now.plus(Duration.ofMinutes(intervalMinutes))
        }

        // Resolve item timezone for quiet hours evaluation
        val zone = try {
            ZoneId.of(item.timeZoneId)
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }

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
