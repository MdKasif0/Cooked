package com.cooked.app.domain.scan

import com.cooked.app.domain.parse.QuickParser
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

data class ScannedDeadlineCandidate(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val dueAt: Instant,
    val effortMinutes: Int = 600, // Default 10h for exams, 180 for assignments
    val isExam: Boolean = true,
    val isLowConfidence: Boolean = false,
    val originalLine: String = "",
    val isSelected: Boolean = true
)

object DatesheetParser {

    private val dateRegexes = listOf(
        // 15-12-2026 or 15/12/2026 or 15.12.2026
        Regex("""\b(\d{1,2})[-/.](?:(\d{1,2})|([a-zA-Z]{3,9}))[-/.](\d{2,4})\b"""),
        // 15th Dec 2026, 15 Dec 2026, Dec 15 2026
        Regex("""\b(\d{1,2})(?:st|nd|rd|th)?\s+([a-zA-Z]{3,9})(?:\s+(\d{2,4}))?\b""", RegexOption.IGNORE_CASE),
        Regex("""\b([a-zA-Z]{3,9})\s+(\d{1,2})(?:st|nd|rd|th)?(?:\s+(\d{2,4}))?\b""", RegexOption.IGNORE_CASE)
    )

    private val timeRegex = Regex("""\b(\d{1,2})(?::(\d{2}))?\s*(am|pm)\b""", RegexOption.IGNORE_CASE)
    private val shiftMorningRegex = Regex("""\bmorning|shift\s*1|session\s*1\b""", RegexOption.IGNORE_CASE)
    private val shiftEveningRegex = Regex("""\bevening|afternoon|shift\s*2|session\s*2\b""", RegexOption.IGNORE_CASE)

    private val noiseWords = setOf(
        "date", "day", "session", "shift", "time", "subject", "code", "paper", "name",
        "course", "exam", "examinations", "schedule", "datesheet", "date-sheet", "semester",
        "university", "college", "theory", "practical", "regular", "reappear", "page"
    )

    /**
     * Parses raw OCR lines/text extracted from a camera photo, screenshot, or document.
     */
    fun parse(
        rawText: String,
        now: Instant = Instant.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): List<ScannedDeadlineCandidate> {
        val lines = rawText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length > 4 }

        val candidates = mutableListOf<ScannedDeadlineCandidate>()

        for (line in lines) {
            val candidate = parseLine(line, now, zone)
            if (candidate != null) {
                candidates.add(candidate)
            }
        }

        // Deduplicate candidates with nearly identical titles on same day
        return candidates.distinctBy { "${it.title.lowercase().trim()}_${it.dueAt.atZone(zone).toLocalDate()}" }
    }

    private fun parseLine(
        line: String,
        now: Instant,
        zone: ZoneId
    ): ScannedDeadlineCandidate? {
        val lowerLine = line.lowercase()

        // 1. Check if line contains a date pattern
        var extractedDate: LocalDate? = null
        var dateMatchRange: IntRange? = null

        for (regex in dateRegexes) {
            val match = regex.find(line)
            if (match != null) {
                extractedDate = parseMatchedDate(match, now, zone)
                if (extractedDate != null) {
                    dateMatchRange = match.range
                    break
                }
            }
        }

        if (extractedDate == null) {
            // Also attempt QuickParser on the line to see if it catches relative or conversational dates
            val quickParsed = QuickParser.parse(line, now, zone)
            if (quickParsed.dueAt != null && quickParsed.title.isNotBlank()) {
                val isHeaderNoise = isNoiseOnly(quickParsed.title)
                if (!isHeaderNoise) {
                    return ScannedDeadlineCandidate(
                        title = cleanTitle(quickParsed.title),
                        dueAt = quickParsed.dueAt,
                        effortMinutes = quickParsed.effortMinutes ?: 180,
                        isExam = lowerLine.contains("exam") || lowerLine.contains("paper"),
                        isLowConfidence = quickParsed.title.length < 5,
                        originalLine = line
                    )
                }
            }
            return null
        }

        // 2. Extract Time
        var extractedTime: LocalTime = LocalTime.of(9, 30) // Default university morning exam
        val timeMatch = timeRegex.find(line)
        if (timeMatch != null) {
            val hour = timeMatch.groupValues[1].toIntOrNull() ?: 9
            val min = timeMatch.groupValues[2].toIntOrNull() ?: 0
            val amPm = timeMatch.groupValues[3].lowercase()
            val hour24 = when {
                amPm == "pm" && hour < 12 -> hour + 12
                amPm == "am" && hour == 12 -> 0
                else -> hour
            }
            extractedTime = LocalTime.of(hour24, min)
        } else if (shiftEveningRegex.containsMatchIn(line)) {
            extractedTime = LocalTime.of(14, 0)
        } else if (shiftMorningRegex.containsMatchIn(line)) {
            extractedTime = LocalTime.of(9, 30)
        }

        // 3. Extract Subject / Title by stripping date, time, delimiters (| - :), and noise words
        val cleanedLine = if (dateMatchRange != null) {
            line.removeRange(dateMatchRange)
        } else {
            line
        }

        val subjectTitle = cleanSubjectText(cleanedLine)
        if (subjectTitle.isBlank() || isNoiseOnly(subjectTitle)) {
            return null
        }

        val dueInstant = extractedDate.atTime(extractedTime).atZone(zone).toInstant()
        val isLowConfidence = subjectTitle.length < 4 || subjectTitle.all { it.isDigit() || it.isWhitespace() }

        return ScannedDeadlineCandidate(
            title = subjectTitle,
            dueAt = dueInstant,
            effortMinutes = 600, // 10 hours effort for typical university exam
            isExam = true,
            isLowConfidence = isLowConfidence,
            originalLine = line
        )
    }

    private fun parseMatchedDate(match: MatchResult, now: Instant, zone: ZoneId): LocalDate? {
        val currentYear = now.atZone(zone).year
        return try {
            val groups = match.groupValues
            val g1 = groups[1]
            val g2 = groups.getOrNull(2) ?: ""
            val g3 = groups.getOrNull(3) ?: ""
            val g4 = groups.getOrNull(4) ?: ""

            when {
                // Format: 15-12-2026 or 15/12/2026 (day-first for India)
                g1.all { it.isDigit() } && g2.all { it.isDigit() } && g2.isNotBlank() -> {
                    val day = g1.toInt()
                    val month = g2.toInt()
                    val year = if (g4.isNotBlank()) parseYear(g4, currentYear) else currentYear
                    LocalDate.of(year, month, day)
                }
                // Format: 15 Dec 2026 or 15th Dec
                g1.all { it.isDigit() } && g2.isNotBlank() && g2.any { it.isLetter() } -> {
                    val day = g1.toInt()
                    val month = parseMonthName(g2) ?: return null
                    val year = if (g3.isNotBlank() && g3.all { it.isDigit() }) parseYear(g3, currentYear) else currentYear
                    LocalDate.of(year, month, day)
                }
                // Format: Dec 15 2026
                g1.any { it.isLetter() } && g2.isNotBlank() && g2.all { it.isDigit() } -> {
                    val month = parseMonthName(g1) ?: return null
                    val day = g2.toInt()
                    val year = if (g3.isNotBlank() && g3.all { it.isDigit() }) parseYear(g3, currentYear) else currentYear
                    LocalDate.of(year, month, day)
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseYear(rawYear: String, currentYear: Int): Int {
        val y = rawYear.toIntOrNull() ?: currentYear
        return if (y < 100) 2000 + y else y
    }

    private fun parseMonthName(name: String): Int? {
        val lower = name.lowercase().take(3)
        return when (lower) {
            "jan" -> 1
            "feb" -> 2
            "mar" -> 3
            "apr" -> 4
            "may" -> 5
            "jun" -> 6
            "jul" -> 7
            "aug" -> 8
            "sep" -> 9
            "oct" -> 10
            "nov" -> 11
            "dec" -> 12
            else -> null
        }
    }

    private fun cleanSubjectText(text: String): String {
        var result = text
            // Strip table pipes, hyphens, colons, brackets
            .replace("|", " ")
            .replace(timeRegex, " ")
            .replace(Regex("""\b(?:morning|afternoon|evening|shift\s*\d|session\s*\d)\b""", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("""\b(?:mon|tue|wed|thu|fri|sat|sun|monday|tuesday|wednesday|thursday|friday|saturday|sunday)\b""", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        // Strip leading code headers like "Paper Code: KCS501" -> "KCS501", "Course Code:"
        result = result.replace(Regex("""^(?:course\s*code|paper\s*code|subject\s*name|course\s*name|subject)\s*[:=-]\s*""", RegexOption.IGNORE_CASE), "")
        result = result.replace(Regex("""\b(?:paper\s*code|course\s*code|subject)\s*[:=-]\s*""", RegexOption.IGNORE_CASE), " ")

        return cleanTitle(result)
    }

    private fun cleanTitle(title: String): String {
        return title
            .trim(' ', '-', '|', ':', ',', ';', '.')
            .replace(Regex("""\s+"""), " ")
    }

    private fun isNoiseOnly(text: String): Boolean {
        val words = text.lowercase().split(Regex("""\W+""")).filter { it.isNotBlank() }
        if (words.isEmpty()) return true
        val noiseCount = words.count { noiseWords.contains(it) }
        return noiseCount.toFloat() / words.size.toFloat() >= 0.7f
    }
}

