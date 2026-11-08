package com.cooked.app.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class RecurrenceTest {

    private val kolkataZone = ZoneId.of("Asia/Kolkata")
    private val londonZone = ZoneId.of("Europe/London")

    @Test
    fun daily_advancesByOneDayPreservingTime() {
        val start = ZonedDateTime.of(2025, 4, 10, 9, 30, 0, 0, kolkataZone).toInstant()
        val next = Recurrence.Daily.nextOccurrence(start, kolkataZone)

        val nextZdt = next.atZone(kolkataZone)
        assertThat(nextZdt.year).isEqualTo(2025)
        assertThat(nextZdt.monthValue).isEqualTo(4)
        assertThat(nextZdt.dayOfMonth).isEqualTo(11)
        assertThat(nextZdt.hour).isEqualTo(9)
        assertThat(nextZdt.minute).isEqualTo(30)
    }

    @Test
    fun weekdays_skipsWeekends() {
        // Friday morning -> should roll to Monday morning
        val friday = ZonedDateTime.of(2025, 4, 11, 10, 0, 0, 0, kolkataZone).toInstant()
        val nextFromFriday = Recurrence.Weekdays.nextOccurrence(friday, kolkataZone)
        val nextZdt = nextFromFriday.atZone(kolkataZone)

        assertThat(nextZdt.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
        assertThat(nextZdt.dayOfMonth).isEqualTo(14)
        assertThat(nextZdt.hour).isEqualTo(10)
    }

    @Test
    fun weekly_advancesToNextSpecifiedDay() {
        // Monday -> Next target days: Wednesday (3) and Friday (5)
        val weekly = Recurrence.Weekly(setOf(DayOfWeek.WEDNESDAY.value, DayOfWeek.FRIDAY.value))
        val monday = ZonedDateTime.of(2025, 4, 7, 8, 0, 0, 0, kolkataZone).toInstant()

        val next1 = weekly.nextOccurrence(monday, kolkataZone).atZone(kolkataZone)
        assertThat(next1.dayOfWeek).isEqualTo(DayOfWeek.WEDNESDAY)

        val next2 = weekly.nextOccurrence(next1.toInstant(), kolkataZone).atZone(kolkataZone)
        assertThat(next2.dayOfWeek).isEqualTo(DayOfWeek.FRIDAY)

        val next3 = weekly.nextOccurrence(next2.toInstant(), kolkataZone).atZone(kolkataZone)
        assertThat(next3.dayOfWeek).isEqualTo(DayOfWeek.WEDNESDAY)
        assertThat(next3.dayOfMonth).isEqualTo(16) // next week
    }

    @Test
    fun everyNDays_advancesByN() {
        val start = ZonedDateTime.of(2025, 5, 1, 14, 0, 0, 0, kolkataZone).toInstant()
        val next = Recurrence.EveryNDays(3).nextOccurrence(start, kolkataZone).atZone(kolkataZone)

        assertThat(next.dayOfMonth).isEqualTo(4)
        assertThat(next.hour).isEqualTo(14)
    }

    @Test
    fun monthly_monthEndClamping_nonLeapYear() {
        // Jan 31, 2025 -> Feb 28, 2025 (2025 is not leap year)
        val jan31 = ZonedDateTime.of(2025, 1, 31, 11, 0, 0, 0, kolkataZone).toInstant()
        val febNext = Recurrence.Monthly(dayOfMonth = 31).nextOccurrence(jan31, kolkataZone).atZone(kolkataZone)

        assertThat(febNext.monthValue).isEqualTo(2)
        assertThat(febNext.dayOfMonth).isEqualTo(28)
        assertThat(febNext.hour).isEqualTo(11)

        // Aug 31 -> Sep 30
        val aug31 = ZonedDateTime.of(2025, 8, 31, 11, 0, 0, 0, kolkataZone).toInstant()
        val sepNext = Recurrence.Monthly(dayOfMonth = 31).nextOccurrence(aug31, kolkataZone).atZone(kolkataZone)

        assertThat(sepNext.monthValue).isEqualTo(9)
        assertThat(sepNext.dayOfMonth).isEqualTo(30)
    }

    @Test
    fun monthly_monthEndClamping_leapYear() {
        // Jan 31, 2024 -> Feb 29, 2024 (2024 is a leap year)
        val jan31Leap = ZonedDateTime.of(2024, 1, 31, 11, 0, 0, 0, kolkataZone).toInstant()
        val febNext = Recurrence.Monthly(dayOfMonth = 31).nextOccurrence(jan31Leap, kolkataZone).atZone(kolkataZone)

        assertThat(febNext.monthValue).isEqualTo(2)
        assertThat(febNext.dayOfMonth).isEqualTo(29)
    }

    @Test
    fun yearly_leapYearToNonLeapYear() {
        // Feb 29, 2024 -> Feb 28, 2025
        val feb29Leap = ZonedDateTime.of(2024, 2, 29, 15, 0, 0, 0, kolkataZone).toInstant()
        val nextYear = Recurrence.Yearly.nextOccurrence(feb29Leap, kolkataZone).atZone(kolkataZone)

        assertThat(nextYear.year).isEqualTo(2025)
        assertThat(nextYear.monthValue).isEqualTo(2)
        assertThat(nextYear.dayOfMonth).isEqualTo(28)
    }

    @Test
    fun dst_london_springForwardPreservesWallClockTime() {
        // In London, BST begins on Sunday, 31 March 2024 (clocks skip from 01:00 GMT to 02:00 BST)
        // Alarm set for 09:00 AM on 30 March 2024
        val march30 = ZonedDateTime.of(2024, 3, 30, 9, 0, 0, 0, londonZone).toInstant()
        val march31 = Recurrence.Daily.nextOccurrence(march30, londonZone).atZone(londonZone)

        // Local wall-clock time must remain 09:00 AM even though offset shifted GMT -> BST (+1)
        assertThat(march31.hour).isEqualTo(9)
        assertThat(march31.minute).isEqualTo(0)
        assertThat(march31.zone).isEqualTo(londonZone)
    }

    @Test
    fun dst_london_fallBackPreservesWallClockTime() {
        // In London, BST ends on Sunday, 27 October 2024 (clocks fall back from 02:00 BST to 01:00 GMT)
        // Alarm set for 09:00 AM on 26 October 2024
        val oct26 = ZonedDateTime.of(2024, 10, 26, 9, 0, 0, 0, londonZone).toInstant()
        val oct27 = Recurrence.Daily.nextOccurrence(oct26, londonZone).atZone(londonZone)

        assertThat(oct27.hour).isEqualTo(9)
        assertThat(oct27.minute).isEqualTo(0)
    }

    @Test
    fun asiaKolkata_noDstSteady24Hours() {
        val day1 = ZonedDateTime.of(2025, 6, 1, 7, 0, 0, 0, kolkataZone).toInstant()
        val day2 = Recurrence.Daily.nextOccurrence(day1, kolkataZone)

        val diffSeconds = day2.epochSecond - day1.epochSecond
        assertThat(diffSeconds).isEqualTo(86400L) // exactly 24 hours
    }
}

