package com.cooked.app.domain.parse

import com.cooked.app.data.datastore.UserPrefs
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.Recurrence
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import java.util.regex.Pattern

enum class HighlightType {
    TIME,
    RECURRENCE,
    EFFORT,
    DEADLINE,
    CRITICAL
}

data class Highlight(
    val start: Int,
    val end: Int,
    val type: HighlightType,
    val stripFromTitle: Boolean = true
)

data class ParseResult(
    val title: String,
    val dueAt: Instant?,
    val allDay: Boolean,
    val kindGuess: ItemKind,
    val effortMinutes: Int?,
    val recurrence: Recurrence?,
    val critical: Boolean,
    val tokens: List<Highlight>
)

object QuickParser {

    fun parse(
        text: String,
        now: Instant,
        zone: ZoneId = ZoneId.systemDefault(),
        prefs: UserPrefs = UserPrefs(),
        locale: Locale = Locale.getDefault()
    ): ParseResult {
        if (text.isBlank()) {
            return ParseResult(
                title = "",
                dueAt = null,
                allDay = false,
                kindGuess = ItemKind.REMINDER,
                effortMinutes = null,
                recurrence = null,
                critical = false,
                tokens = emptyList()
            )
        }

        val highlights = mutableListOf<Highlight>()
        val zdt = now.atZone(zone)
        val today = zdt.toLocalDate()
        val currentTime = zdt.toLocalTime()

        // 1. Critical markers: "!!", "urgent", "asap", "critical"
        var isCritical = false
        val criticalRegex = Regex("""(?i)\b(urgent|asap|critical|high priority)\b|!{2,}""")
        criticalRegex.findAll(text).forEach { match ->
            isCritical = true
            highlights.add(Highlight(match.range.first, match.range.last + 1, HighlightType.CRITICAL))
        }

        // 2. Deadline markers: "due", "deadline", "submit", "submission", "exam", "assignment", "project", "by"
        var isDeadline = false
        val deadlineRegex = Regex("""(?i)\b(due(?:\s+date)?|deadline|submit|submission|exam|test|assignment|project)\b""")
        deadlineRegex.findAll(text).forEach { match ->
            isDeadline = true
            val token = match.value.lowercase()
            val shouldStrip = token == "due" || token == "due date" || token == "deadline"
            highlights.add(Highlight(match.range.first, match.range.last + 1, HighlightType.DEADLINE, stripFromTitle = shouldStrip))
        }

        // 3. Effort Parsing: "3h", "~2h", "90m", "45 mins", "2 hrs work", "1.5h"
        var effortMinutes: Int? = null
        val effortRegex = Regex("""(?i)(?:~?\s*(\d+(?:\.\d+)?)\s*(?:hrs?|hours?|h)(?:\s*(?:of\s+)?work)?)|(?:\b(\d+)\s*(?:mins?|minutes?|m)\b)""")
        effortRegex.findAll(text).forEach { match ->
            // Avoid matching relative time like "in 20m" or "+30m"
            val prefixIndex = (match.range.first - 3).coerceAtLeast(0)
            val preceding = text.substring(prefixIndex, match.range.first).lowercase()
            if (!preceding.contains("in") && !preceding.contains("+")) {
                val hourGroup = match.groupValues[1]
                val minGroup = match.groupValues[2]
                val mins = when {
                    hourGroup.isNotEmpty() -> (hourGroup.toDouble() * 60).toInt()
                    minGroup.isNotEmpty() -> minGroup.toInt()
                    else -> null
                }
                if (mins != null && mins > 0) {
                    effortMinutes = mins
                    highlights.add(Highlight(match.range.first, match.range.last + 1, HighlightType.EFFORT))
                }
            }
        }

        // 4. Recurrence Parsing: "every day", "daily", "weekdays", "every mon", "every 2 days", "monthly on 1st", "1st every month"
        var recurrence: Recurrence? = null
        val dailyRegex = Regex("""(?i)\b(every\s+day|daily)\b""")
        val weekdaysRegex = Regex("""(?i)\b(weekdays?)\b""")
        val everyNDaysRegex = Regex("""(?i)\bevery\s+(\d+)\s+days?\b""")
        val weeklyDayRegex = Regex("""(?i)\bevery\s+(mon(?:day)?|tue(?:sday)?|wed(?:nesday)?|thu(?:rsday)?|fri(?:day)?|sat(?:urday)?|sun(?:day)?)\b""")
        val monthlyRegex = Regex("""(?i)(?:monthly(?:\s+on\s+(\d+)(?:st|nd|rd|th)?)?)|(?:(\d+)(?:st|nd|rd|th)?\s+every\s+month)""")

        dailyRegex.find(text)?.let {
            recurrence = Recurrence.Daily
            highlights.add(Highlight(it.range.first, it.range.last + 1, HighlightType.RECURRENCE))
        } ?: weekdaysRegex.find(text)?.let {
            recurrence = Recurrence.Weekdays
            highlights.add(Highlight(it.range.first, it.range.last + 1, HighlightType.RECURRENCE))
        } ?: everyNDaysRegex.find(text)?.let {
            val n = it.groupValues[1].toIntOrNull() ?: 1
            recurrence = Recurrence.EveryNDays(n)
            highlights.add(Highlight(it.range.first, it.range.last + 1, HighlightType.RECURRENCE))
        } ?: weeklyDayRegex.find(text)?.let {
            val dayName = it.groupValues[1].lowercase()
            val dow = parseDayOfWeek(dayName)
            if (dow != null) {
                recurrence = Recurrence.Weekly(setOf(dow.value))
                highlights.add(Highlight(it.range.first, it.range.last + 1, HighlightType.RECURRENCE))
            }
        } ?: monthlyRegex.find(text)?.let {
            val dayStr = it.groupValues[1].ifEmpty { it.groupValues[2] }
            val day = dayStr.toIntOrNull() ?: 1
            recurrence = Recurrence.Monthly(dayOfMonth = day.coerceIn(1, 31))
            highlights.add(Highlight(it.range.first, it.range.last + 1, HighlightType.RECURRENCE))
        }

        // 5. Date & Time Parsing
        var targetDate: LocalDate? = null
        var targetTime: LocalTime? = null
        var isAllDay = false

        // Relative time: "in 20m", "+30m", "in 2h", "+1d", "in 3 days", "in 20 minutes"
        val relativeRegex = Regex("""(?i)(?:\bin|(?<=\s|^)\+)\s*(\d+)\s*(minutes?|mins?|m|hours?|hrs?|h|days?|d)\b""")
        val relativeMatch = relativeRegex.find(text)
        if (relativeMatch != null) {
            val amount = relativeMatch.groupValues[1].toLongOrNull() ?: 0L
            val unit = relativeMatch.groupValues[2].lowercase()
            val targetInstant = when {
                unit.startsWith("m") -> now.plus(Duration.ofMinutes(amount))
                unit.startsWith("h") -> now.plus(Duration.ofHours(amount))
                unit.startsWith("d") -> now.plus(Duration.ofDays(amount))
                else -> now
            }
            val targetZdt = targetInstant.atZone(zone)
            targetDate = targetZdt.toLocalDate()
            targetTime = targetZdt.toLocalTime().withSecond(0).withNano(0)
            highlights.add(Highlight(relativeMatch.range.first, relativeMatch.range.last + 1, HighlightType.TIME))
        }

        // Keyword dates: "tonight" (20:00), "today", "tomorrow", "tmrw", "kal"
        val tonightRegex = Regex("""(?i)\btonight\b""")
        val todayRegex = Regex("""(?i)\btoday\b""")
        val tomorrowRegex = Regex("""(?i)\b(tomorrow|tmrw|kal)\b""")

        if (targetDate == null) {
            tonightRegex.find(text)?.let {
                targetDate = today
                targetTime = LocalTime.of(20, 0)
                highlights.add(Highlight(it.range.first, it.range.last + 1, HighlightType.TIME))
            } ?: todayRegex.find(text)?.let {
                targetDate = today
                highlights.add(Highlight(it.range.first, it.range.last + 1, HighlightType.TIME))
            } ?: tomorrowRegex.find(text)?.let {
                targetDate = today.plusDays(1)
                highlights.add(Highlight(it.range.first, it.range.last + 1, HighlightType.TIME))
            }
        }

        // Weekday names: "next fri", "fri", "monday"
        val nextWeekdayRegex = Regex("""(?i)\b(?:next\s+)?(mon(?:day)?|tue(?:sday)?|wed(?:nesday)?|thu(?:rsday)?|fri(?:day)?|sat(?:urday)?|sun(?:day)?)\b""")
        if (targetDate == null) {
            nextWeekdayRegex.findAll(text).forEach { match ->
                // Avoid capturing weekday if it's already part of recurrence "every monday"
                val matchRange = match.range
                val overlapsRecurrence = highlights.any {
                    it.type == HighlightType.RECURRENCE && (matchRange.first in it.start..it.end || matchRange.last in it.start..it.end)
                }
                if (!overlapsRecurrence) {
                    val fullMatch = match.value.lowercase()
                    val isNext = fullMatch.startsWith("next")
                    val dayStr = match.groupValues[1].lowercase()
                    val dow = parseDayOfWeek(dayStr)
                    if (dow != null) {
                        targetDate = if (isNext) {
                            today.with(TemporalAdjusters.next(dow))
                        } else {
                            if (today.dayOfWeek == dow) {
                                today.plusDays(7)
                            } else {
                                today.with(TemporalAdjusters.next(dow))
                            }
                        }
                        highlights.add(Highlight(match.range.first, match.range.last + 1, HighlightType.TIME))
                    }
                }
            }
        }

        // Specific dates: "12 nov", "nov 12", "15 dec", "12/11", "by 30th"
        val monthNames = "jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:tember)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?"
        val dayMonthRegex = Regex("""(?i)\b(\d{1,2})(?:st|nd|rd|th)?\s+($monthNames)\b""")
        val monthDayRegex = Regex("""(?i)\b($monthNames)\s+(\d{1,2})(?:st|nd|rd|th)?\b""")
        val slashDateRegex = Regex("""\b(\d{1,2})/(\d{1,2})(?:/(\d{2,4}))?\b""")
        val standaloneDayRegex = Regex("""(?i)\b(?:by\s+|on\s+)?(\d{1,2})(?:st|nd|rd|th)\b""")

        if (targetDate == null) {
            dayMonthRegex.find(text)?.let { match ->
                val day = match.groupValues[1].toInt()
                val month = parseMonth(match.groupValues[2])
                if (month != null) {
                    var date = LocalDate.of(today.year, month, day.coerceIn(1, month.length(false)))
                    if (date.isBefore(today)) date = date.plusYears(1)
                    targetDate = date
                    highlights.add(Highlight(match.range.first, match.range.last + 1, HighlightType.TIME))
                }
            } ?: monthDayRegex.find(text)?.let { match ->
                val month = parseMonth(match.groupValues[1])
                val day = match.groupValues[2].toInt()
                if (month != null) {
                    var date = LocalDate.of(today.year, month, day.coerceIn(1, month.length(false)))
                    if (date.isBefore(today)) date = date.plusYears(1)
                    targetDate = date
                    highlights.add(Highlight(match.range.first, match.range.last + 1, HighlightType.TIME))
                }
            } ?: slashDateRegex.find(text)?.let { match ->
                val p1 = match.groupValues[1].toInt()
                val p2 = match.groupValues[2].toInt()
                val year = match.groupValues[3].toIntOrNull()?.let {
                    if (it < 100) 2000 + it else it
                } ?: today.year

                // Locale check: India, UK, Europe use day/month; US uses month/day
                val isDayFirst = locale.country.uppercase() in listOf("IN", "GB", "UK", "AU", "NZ", "CA") || locale.country.isEmpty()
                val (day, monthVal) = if (isDayFirst) Pair(p1, p2) else Pair(p2, p1)
                val month = try { Month.of(monthVal) } catch (_: Exception) { Month.JANUARY }
                var date = LocalDate.of(year, month, day.coerceIn(1, month.length(false)))
                if (match.groupValues[3].isEmpty() && date.isBefore(today)) date = date.plusYears(1)
                targetDate = date
                highlights.add(Highlight(match.range.first, match.range.last + 1, HighlightType.TIME))
            } ?: standaloneDayRegex.find(text)?.let { match ->
                val day = match.groupValues[1].toInt()
                var date = today.withDayOfMonth(day.coerceIn(1, today.lengthOfMonth()))
                if (date.isBefore(today)) date = today.plusMonths(1).withDayOfMonth(day.coerceIn(1, today.plusMonths(1).lengthOfMonth()))
                targetDate = date
                highlights.add(Highlight(match.range.first, match.range.last + 1, HighlightType.TIME))
            }
        }

        // 6. Time of Day Parsing: "5pm", "17:30", "5:30", "noon", "midnight", "morning 9", "evening 18"
        val specialTimeRegex = Regex("""(?i)\b(noon|midnight)\b""")
        val amPmTimeRegex = Regex("""(?i)\b(\d{1,2})(?::(\d{2}))?\s*(am|pm)\b""")
        val colonTimeRegex = Regex("""\b(\d{1,2}):(\d{2})\b""")
        val naturalTimeRegex = Regex("""(?i)\b(morning|evening|night)\s+(\d{1,2})\b""")
        val bareHourRegex = Regex("""\b(?:at\s+)?(\d{1,2})\b""")

        if (targetTime == null) {
            specialTimeRegex.find(text)?.let { match ->
                targetTime = if (match.value.lowercase() == "noon") LocalTime.of(12, 0) else LocalTime.of(23, 59)
                highlights.add(Highlight(match.range.first, match.range.last + 1, HighlightType.TIME))
            } ?: amPmTimeRegex.find(text)?.let { match ->
                var h = match.groupValues[1].toInt()
                val m = match.groupValues[2].toIntOrNull() ?: 0
                val ampm = match.groupValues[3].lowercase()
                if (ampm == "pm" && h < 12) h += 12
                if (ampm == "am" && h == 12) h = 0
                targetTime = LocalTime.of(h.coerceIn(0, 23), m.coerceIn(0, 59))
                highlights.add(Highlight(match.range.first, match.range.last + 1, HighlightType.TIME))
            } ?: colonTimeRegex.find(text)?.let { match ->
                val h = match.groupValues[1].toInt()
                val m = match.groupValues[2].toInt()
                if (h in 0..23 && m in 0..59) {
                    targetTime = LocalTime.of(h, m)
                    highlights.add(Highlight(match.range.first, match.range.last + 1, HighlightType.TIME))
                }
            } ?: naturalTimeRegex.find(text)?.let { match ->
                val part = match.groupValues[1].lowercase()
                var h = match.groupValues[2].toInt()
                if (part == "evening" && h < 12) h += 12
                if (part == "night" && h < 12 && h < 6) h += 12 // night 9 -> 21:00
                else if (part == "night" && h in 8..11) h += 12
                targetTime = LocalTime.of(h.coerceIn(0, 23), 0)
                highlights.add(Highlight(match.range.first, match.range.last + 1, HighlightType.TIME))
            } ?: run {
                // Check for bare time like "5" or "at 5" (only if after a keyword like at/by or at the end of text)
                val atRegex = Regex("""(?i)\b(?:at|by)\s+(\d{1,2})\b""")
                atRegex.find(text)?.let { match ->
                    val bare = match.groupValues[1].toInt()
                    // Bare "5" = 17:00 if now is after 05:00
                    val h = if (bare in 1..11 && currentTime.hour >= bare) bare + 12 else bare
                    targetTime = LocalTime.of(h.coerceIn(0, 23), 0)
                    highlights.add(Highlight(match.range.first, match.range.last + 1, HighlightType.TIME))
                }
            }
        }

        // 7. Resolve Final Due Instant & Ambiguity Rules
        val resolvedDate = targetDate ?: today
        val resolvedTime = targetTime ?: when {
            targetDate != null -> {
                isAllDay = true
                LocalTime.of(18, 0)
            }
            else -> null
        }

        var finalInstant: Instant? = null
        if (resolvedTime != null) {
            var zdtCandidate = resolvedDate.atTime(resolvedTime).atZone(zone)
            // Ambiguity rule: a bare time earlier than now = tomorrow
            if (targetDate == null && zdtCandidate.toInstant().isBefore(now)) {
                zdtCandidate = zdtCandidate.plusDays(1)
            }
            finalInstant = zdtCandidate.toInstant()
        } else if (targetDate != null) {
            isAllDay = true
            finalInstant = resolvedDate.atTime(18, 0).atZone(zone).toInstant()
        }

        // 8. Extract clean title by removing highlighted tokens
        val sortedHighlights = highlights.filter { it.stripFromTitle }.sortedBy { it.start }
        val titleBuilder = StringBuilder()
        var lastIndex = 0
        for (h in sortedHighlights) {
            if (h.start > lastIndex) {
                titleBuilder.append(text.substring(lastIndex, h.start))
            }
            lastIndex = maxOf(lastIndex, h.end)
        }
        if (lastIndex < text.length) {
            titleBuilder.append(text.substring(lastIndex))
        }

        var cleanedTitle = titleBuilder.toString()
            .replace(Regex("""\s+"""), " ")
            .replace(Regex("""(?i)\b(at|on|by|in|due)\s*$"""), "")
            .replace(Regex("""^[,\-–—\s]+|[,\-–—\s]+$"""), "")
            .trim()

        if (cleanedTitle.isEmpty()) {
            cleanedTitle = text.trim()
        }

        return ParseResult(
            title = cleanedTitle,
            dueAt = finalInstant,
            allDay = isAllDay,
            kindGuess = if (isDeadline) ItemKind.DEADLINE else ItemKind.REMINDER,
            effortMinutes = effortMinutes,
            recurrence = recurrence,
            critical = isCritical,
            tokens = sortedHighlights
        )
    }

    private fun parseDayOfWeek(str: String): DayOfWeek? {
        return when (str.lowercase().take(3)) {
            "mon" -> DayOfWeek.MONDAY
            "tue" -> DayOfWeek.TUESDAY
            "wed" -> DayOfWeek.WEDNESDAY
            "thu" -> DayOfWeek.THURSDAY
            "fri" -> DayOfWeek.FRIDAY
            "sat" -> DayOfWeek.SATURDAY
            "sun" -> DayOfWeek.SUNDAY
            else -> null
        }
    }

    private fun parseMonth(str: String): Month? {
        return when (str.lowercase().take(3)) {
            "jan" -> Month.JANUARY
            "feb" -> Month.FEBRUARY
            "mar" -> Month.MARCH
            "apr" -> Month.APRIL
            "may" -> Month.MAY
            "jun" -> Month.JUNE
            "jul" -> Month.JULY
            "aug" -> Month.AUGUST
            "sep" -> Month.SEPTEMBER
            "oct" -> Month.OCTOBER
            "nov" -> Month.NOVEMBER
            "dec" -> Month.DECEMBER
            else -> null
        }
    }
}
