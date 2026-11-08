package com.cooked.app.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object TimeUtils {

    fun nowUtcMillis(): Long = Instant.now().toEpochMilli()

    fun instantToLocalDate(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
        return Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate()
    }

    fun instantToLocalTime(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): LocalTime {
        return Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalTime()
    }

    fun localDateTimeToUtcMillis(
        dateTime: LocalDateTime,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        return dateTime.atZone(zoneId).toInstant().toEpochMilli()
    }

    fun localDateAndTimeToUtcMillis(
        date: LocalDate,
        time: LocalTime,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        return LocalDateTime.of(date, time).atZone(zoneId).toInstant().toEpochMilli()
    }

    fun formatTimeOnly(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return Instant.ofEpochMilli(epochMillis).atZone(zoneId).format(formatter)
    }

    fun formatDateTime(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
        return Instant.ofEpochMilli(epochMillis).atZone(zoneId).format(formatter)
    }

    fun formatRelativeTime(
        targetUtcMillis: Long,
        nowUtcMillis: Long = nowUtcMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val diffSeconds = (targetUtcMillis - nowUtcMillis) / 1000
        val isPast = diffSeconds < 0
        val absSec = kotlin.math.abs(diffSeconds)

        val minutes = absSec / 60
        val hours = minutes / 60
        val days = hours / 24

        val timeString = when {
            minutes < 1 -> "just now"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h ${minutes % 60}m"
            else -> "${days}d ${hours % 24}h"
        }

        return if (isPast) {
            "Overdue by $timeString"
        } else {
            "In $timeString"
        }
    }

    fun formatCountdown(
        targetUtcMillis: Long,
        nowUtcMillis: Long = nowUtcMillis()
    ): String {
        val diffMillis = targetUtcMillis - nowUtcMillis
        if (diffMillis <= 0) return "00:00:00"

        val totalSeconds = diffMillis / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
