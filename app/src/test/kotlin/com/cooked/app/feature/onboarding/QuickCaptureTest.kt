package com.cooked.app.feature.onboarding

import com.cooked.app.domain.model.Recurrence
import com.cooked.app.domain.parse.QuickParser
import com.cooked.app.feature.onboarding.components.QUICK_SUGGESTIONS
import com.cooked.app.feature.onboarding.components.formatParsedDateLabel
import com.cooked.app.feature.onboarding.components.formatParsedTimeLabel
import com.cooked.app.feature.onboarding.components.formatRecurrenceLabel
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class QuickCaptureTest {

    private val testZone = ZoneId.of("UTC")
    // Base time: Monday, Nov 9 2026, 10:00 AM UTC
    private val baseNow: Instant = LocalDate.of(2026, 11, 9)
        .atTime(LocalTime.of(10, 0))
        .atZone(testZone)
        .toInstant()

    @Test
    fun quickSuggestions_containThreePresets() {
        assertThat(QUICK_SUGGESTIONS).hasSize(3)
        assertThat(QUICK_SUGGESTIONS[0].promptText).isEqualTo("Submit assignment Friday 5pm")
        assertThat(QUICK_SUGGESTIONS[1].promptText).isEqualTo("Pay rent on the 1st")
        assertThat(QUICK_SUGGESTIONS[2].promptText).isEqualTo("Drink water in 20 minutes")
    }

    @Test
    fun quickSuggestions_parseAccurately() {
        // Suggestion 1: Friday 5pm
        val parsed1 = QuickParser.parse(QUICK_SUGGESTIONS[0].promptText, baseNow, testZone)
        assertThat(parsed1.title.lowercase()).contains("submit assignment")
        assertThat(parsed1.dueAt).isNotNull()
        val zdt1 = parsed1.dueAt!!.atZone(testZone)
        assertThat(zdt1.dayOfWeek).isEqualTo(DayOfWeek.FRIDAY)
        assertThat(zdt1.hour).isEqualTo(17)
        assertThat(zdt1.minute).isEqualTo(0)

        // Suggestion 2: Pay rent on the 1st
        val parsed2 = QuickParser.parse(QUICK_SUGGESTIONS[1].promptText, baseNow, testZone)
        assertThat(parsed2.title.lowercase()).contains("pay rent")
        assertThat(parsed2.dueAt).isNotNull()
        val zdt2 = parsed2.dueAt!!.atZone(testZone)
        assertThat(zdt2.dayOfMonth).isEqualTo(1)

        // Suggestion 3: In 20 minutes
        val parsed3 = QuickParser.parse(QUICK_SUGGESTIONS[2].promptText, baseNow, testZone)
        assertThat(parsed3.title.lowercase()).contains("drink water")
        assertThat(parsed3.dueAt).isNotNull()
        val diffMinutes = Duration.between(baseNow, parsed3.dueAt!!).toMinutes()
        assertThat(diffMinutes).isEqualTo(20L)
    }

    @Test
    fun dateFormatting_producesExpectedLabels() {
        val today = baseNow
        val tomorrow = baseNow.plus(Duration.ofDays(1))
        val nextFriday = baseNow.plus(Duration.ofDays(4))

        assertThat(formatParsedDateLabel(today, testZone, baseNow)).isEqualTo("Today")
        assertThat(formatParsedDateLabel(tomorrow, testZone, baseNow)).isEqualTo("Tomorrow")
        assertThat(formatParsedDateLabel(nextFriday, testZone, baseNow)).isEqualTo("Friday")
    }

    @Test
    fun timeFormatting_formats12HourWithAmPm() {
        // 5:00 PM
        val fivePm = LocalDate.of(2026, 11, 9)
            .atTime(17, 0)
            .atZone(testZone)
            .toInstant()
        val formattedTime = formatParsedTimeLabel(fivePm, testZone).uppercase()
        assertThat(formattedTime).contains("5:00")
        assertThat(formattedTime).contains("PM")

        // 11:30 AM
        val elevenAm = LocalDate.of(2026, 11, 9)
            .atTime(11, 30)
            .atZone(testZone)
            .toInstant()
        val formattedEleven = formatParsedTimeLabel(elevenAm, testZone).uppercase()
        assertThat(formattedEleven).contains("11:30")
        assertThat(formattedEleven).contains("AM")
    }

    @Test
    fun recurrenceFormatting_formatsLabels() {
        assertThat(formatRecurrenceLabel(Recurrence.Daily)).isEqualTo("Every day")
        assertThat(formatRecurrenceLabel(Recurrence.Weekdays)).isEqualTo("Weekdays")
        assertThat(formatRecurrenceLabel(Recurrence.Weekly(setOf(1)))).isEqualTo("Every week")
        assertThat(formatRecurrenceLabel(Recurrence.Monthly())).isEqualTo("Monthly")
        assertThat(formatRecurrenceLabel(null)).isNull()
    }
}
