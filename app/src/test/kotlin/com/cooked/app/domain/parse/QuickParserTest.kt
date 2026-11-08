package com.cooked.app.domain.parse

import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.Recurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class QuickParserTest {

    private val baseNow = Instant.parse("2026-11-08T10:00:00Z") // Sunday, 10:00 AM UTC
    private val zone = ZoneId.of("UTC")

    private fun parse(text: String): ParseResult {
        return QuickParser.parse(text = text, now = baseNow, zone = zone)
    }

    // 1-6: Required Prominent Phrasing Tests
    @Test
    fun `test 1 - pay rent 1st every month 10am`() {
        val result = parse("pay rent 1st every month 10am")
        assertTrue(result.title.contains("pay rent"))
        assertTrue(result.recurrence is Recurrence.Monthly)
        assertEquals(1, (result.recurrence as Recurrence.Monthly).dayOfMonth)
        assertNotNull(result.dueAt)
        val zdt = result.dueAt!!.atZone(zone)
        assertEquals(10, zdt.hour)
        assertEquals(0, zdt.minute)
    }

    @Test
    fun `test 2 - dbms assignment due fri 5pm 3h`() {
        val result = parse("dbms assignment due fri 5pm 3h")
        assertTrue(result.title.contains("dbms assignment"))
        assertEquals(ItemKind.DEADLINE, result.kindGuess)
        assertEquals(180, result.effortMinutes)
        assertNotNull(result.dueAt)
        val zdt = result.dueAt!!.atZone(zone)
        assertEquals(DayOfWeek.FRIDAY, zdt.dayOfWeek)
        assertEquals(17, zdt.hour)
    }

    @Test
    fun `test 3 - call mom tonight`() {
        val result = parse("call mom tonight")
        assertEquals("call mom", result.title)
        assertNotNull(result.dueAt)
        val zdt = result.dueAt!!.atZone(zone)
        assertEquals(20, zdt.hour)
        assertEquals(0, zdt.minute)
    }

    @Test
    fun `test 4 - exam 15 dec`() {
        val result = parse("exam 15 dec")
        assertEquals(ItemKind.DEADLINE, result.kindGuess)
        assertNotNull(result.dueAt)
        val zdt = result.dueAt!!.atZone(zone)
        assertEquals(15, zdt.dayOfMonth)
        assertEquals(12, zdt.monthValue)
    }

    @Test
    fun `test 5 - meds 9am daily`() {
        val result = parse("meds 9am daily")
        assertTrue(result.title.contains("meds"))
        assertEquals(Recurrence.Daily, result.recurrence)
        assertNotNull(result.dueAt)
        val zdt = result.dueAt!!.atZone(zone)
        assertEquals(9, zdt.hour)
    }

    @Test
    fun `test 6 - submit invoice by 30th`() {
        val result = parse("submit invoice by 30th")
        assertTrue(result.title.contains("submit invoice"))
        assertEquals(ItemKind.DEADLINE, result.kindGuess)
        assertNotNull(result.dueAt)
        val zdt = result.dueAt!!.atZone(zone)
        assertEquals(30, zdt.dayOfMonth)
    }

    // 7-15: Student Phrasing Tests
    @Test
    fun `test 7 - compiler design lab report due tomorrow 11_59pm`() {
        val res = parse("compiler design lab report due tomorrow 11:59pm")
        assertEquals(ItemKind.DEADLINE, res.kindGuess)
        val zdt = res.dueAt!!.atZone(zone)
        assertEquals(23, zdt.hour)
        assertEquals(59, zdt.minute)
    }

    @Test
    fun `test 8 - gate mock exam sunday 9am`() {
        val res = parse("gate mock exam sunday 9am")
        assertEquals(ItemKind.DEADLINE, res.kindGuess)
        assertEquals(DayOfWeek.SUNDAY, res.dueAt!!.atZone(zone).dayOfWeek)
    }

    @Test
    fun `test 9 - leetcode daily problem 8pm`() {
        val res = parse("leetcode daily problem 8pm")
        assertEquals(Recurrence.Daily, res.recurrence)
        assertEquals(20, res.dueAt!!.atZone(zone).hour)
    }

    @Test
    fun `test 10 - submit upsc prelims application urgent !!`() {
        val res = parse("submit upsc prelims application urgent !!")
        assertEquals(ItemKind.DEADLINE, res.kindGuess)
        assertTrue(res.critical)
    }

    @Test
    fun `test 11 - physics tutorial sheet due next monday 2h`() {
        val res = parse("physics tutorial sheet due next monday 2h")
        assertEquals(120, res.effortMinutes)
        assertEquals(DayOfWeek.MONDAY, res.dueAt!!.atZone(zone).dayOfWeek)
    }

    @Test
    fun `test 12 - chemistry quiz in 30m`() {
        val res = parse("chemistry quiz in 30m")
        assertEquals(baseNow.plus(Duration.ofMinutes(30)), res.dueAt)
    }

    @Test
    fun `test 13 - return library books tmrw noon`() {
        val res = parse("return library books tmrw noon")
        val zdt = res.dueAt!!.atZone(zone)
        assertEquals(12, zdt.hour)
        assertEquals(9, zdt.dayOfMonth) // today is 8th
    }

    @Test
    fun `test 14 - machine learning term paper due 25 nov 4h`() {
        val res = parse("machine learning term paper due 25 nov 4h")
        assertEquals(240, res.effortMinutes)
        assertEquals(25, res.dueAt!!.atZone(zone).dayOfMonth)
        assertEquals(11, res.dueAt!!.atZone(zone).monthValue)
    }

    @Test
    fun `test 15 - dsa revision every day 6am`() {
        val res = parse("dsa revision every day 6am")
        assertEquals(Recurrence.Daily, res.recurrence)
        assertEquals(6, res.dueAt!!.atZone(zone).hour)
    }

    // 16-25: Freelancer Phrasing Tests
    @Test
    fun `test 16 - send client contract asap !!`() {
        val res = parse("send client contract asap !!")
        assertTrue(res.critical)
        assertTrue(res.title.contains("send client contract"))
    }

    @Test
    fun `test 17 - figma design review due fri 4pm`() {
        val res = parse("figma design review due fri 4pm")
        assertEquals(ItemKind.DEADLINE, res.kindGuess)
        assertEquals(16, res.dueAt!!.atZone(zone).hour)
    }

    @Test
    fun `test 18 - send stripe invoice 1st every month`() {
        val res = parse("send stripe invoice 1st every month")
        assertTrue(res.recurrence is Recurrence.Monthly)
    }

    @Test
    fun `test 19 - client kickoff call tomorrow 3pm`() {
        val res = parse("client kickoff call tomorrow 3pm")
        assertEquals(15, res.dueAt!!.atZone(zone).hour)
    }

    @Test
    fun `test 20 - deploy backend to prod tonight`() {
        val res = parse("deploy backend to prod tonight")
        assertEquals(20, res.dueAt!!.atZone(zone).hour)
    }

    @Test
    fun `test 21 - follow up with lead in 2h`() {
        val res = parse("follow up with lead in 2h")
        assertEquals(baseNow.plus(Duration.ofHours(2)), res.dueAt)
    }

    @Test
    fun `test 22 - quarterly tax filing deadline 31 mar`() {
        val res = parse("quarterly tax filing deadline 31 mar")
        assertEquals(ItemKind.DEADLINE, res.kindGuess)
        assertEquals(31, res.dueAt!!.atZone(zone).dayOfMonth)
        assertEquals(3, res.dueAt!!.atZone(zone).monthValue)
    }

    @Test
    fun `test 23 - weekly team sync weekdays 10am`() {
        val res = parse("weekly team sync weekdays 10am")
        assertEquals(Recurrence.Weekdays, res.recurrence)
        assertEquals(10, res.dueAt!!.atZone(zone).hour)
    }

    @Test
    fun `test 24 - code review PR 90m`() {
        val res = parse("code review PR 90m")
        assertEquals(90, res.effortMinutes)
    }

    @Test
    fun `test 25 - renew domain cooked app 15 dec`() {
        val res = parse("renew domain cooked app 15 dec")
        assertEquals(15, res.dueAt!!.atZone(zone).dayOfMonth)
        assertEquals(12, res.dueAt!!.atZone(zone).monthValue)
    }

    // 26-35: Relative & Time Tests
    @Test
    fun `test 26 - quick stretch +15m`() {
        val res = parse("quick stretch +15m")
        assertEquals(baseNow.plus(Duration.ofMinutes(15)), res.dueAt)
    }

    @Test
    fun `test 27 - tea break in 45 mins`() {
        val res = parse("tea break in 45 mins")
        assertEquals(baseNow.plus(Duration.ofMinutes(45)), res.dueAt)
    }

    @Test
    fun `test 28 - call plumber at 5`() {
        // Base now is 10:00 AM -> bare 5 means 17:00 (5pm)
        val res = parse("call plumber at 5")
        assertEquals(17, res.dueAt!!.atZone(zone).hour)
    }

    @Test
    fun `test 29 - submit paper midnight`() {
        val res = parse("submit paper midnight")
        assertEquals(23, res.dueAt!!.atZone(zone).hour)
        assertEquals(59, res.dueAt!!.atZone(zone).minute)
    }

    @Test
    fun `test 30 - standup morning 9`() {
        val res = parse("standup morning 9")
        assertEquals(9, res.dueAt!!.atZone(zone).hour)
    }

    @Test
    fun `test 31 - gym session evening 6`() {
        val res = parse("gym session evening 6")
        assertEquals(18, res.dueAt!!.atZone(zone).hour)
    }

    @Test
    fun `test 32 - water plants in 3 days`() {
        val res = parse("water plants in 3 days")
        assertEquals(baseNow.plus(Duration.ofDays(3)), res.dueAt)
    }

    @Test
    fun `test 33 - meeting at 14_30`() {
        val res = parse("meeting at 14:30")
        assertEquals(14, res.dueAt!!.atZone(zone).hour)
        assertEquals(30, res.dueAt!!.atZone(zone).minute)
    }

    @Test
    fun `test 34 - dinner with friends 8_30pm`() {
        val res = parse("dinner with friends 8:30pm")
        assertEquals(20, res.dueAt!!.atZone(zone).hour)
        assertEquals(30, res.dueAt!!.atZone(zone).minute)
    }

    @Test
    fun `test 35 - wake up 5_30am`() {
        val res = parse("wake up 5:30am")
        assertEquals(5, res.dueAt!!.atZone(zone).hour)
        assertEquals(30, res.dueAt!!.atZone(zone).minute)
    }

    // 36-45: Indian Phrasing (Hindi "kal", Indian date formats)
    @Test
    fun `test 36 - bijli bill pay karo kal`() {
        val res = parse("bijli bill pay karo kal")
        assertEquals(9, res.dueAt!!.atZone(zone).dayOfMonth) // today is 8th
    }

    @Test
    fun `test 37 - submit project kal 5pm`() {
        val res = parse("submit project kal 5pm")
        val zdt = res.dueAt!!.atZone(zone)
        assertEquals(9, zdt.dayOfMonth)
        assertEquals(17, zdt.hour)
    }

    @Test
    fun `test 38 - date format 12 11 day first`() {
        val res = parse("semester exam 12/11")
        assertEquals(12, res.dueAt!!.atZone(zone).dayOfMonth)
        assertEquals(11, res.dueAt!!.atZone(zone).monthValue)
    }

    @Test
    fun `test 39 - fees payment by 10th`() {
        val res = parse("fees payment by 10th")
        assertEquals(10, res.dueAt!!.atZone(zone).dayOfMonth)
    }

    @Test
    fun `test 40 - dentist visit kal 11am`() {
        val res = parse("dentist visit kal 11am")
        assertEquals(11, res.dueAt!!.atZone(zone).hour)
    }

    @Test
    fun `test 41 - upsc essay test sunday 2h`() {
        val res = parse("upsc essay test sunday 2h")
        assertEquals(120, res.effortMinutes)
    }

    @Test
    fun `test 42 - iit jee mock test 18 nov`() {
        val res = parse("iit jee mock test 18 nov")
        assertEquals(18, res.dueAt!!.atZone(zone).dayOfMonth)
    }

    @Test
    fun `test 43 - buy groceries tonight 8pm`() {
        val res = parse("buy groceries tonight 8pm")
        assertEquals(20, res.dueAt!!.atZone(zone).hour)
    }

    @Test
    fun `test 44 - wifi recharge by 28th`() {
        val res = parse("wifi recharge by 28th")
        assertEquals(28, res.dueAt!!.atZone(zone).dayOfMonth)
    }

    @Test
    fun `test 45 - rent payment 5th every month`() {
        val res = parse("rent payment 5th every month")
        assertTrue(res.recurrence is Recurrence.Monthly)
        assertEquals(5, (res.recurrence as Recurrence.Monthly).dayOfMonth)
    }

    // 46-55: Effort & Keyword Edge Cases
    @Test
    fun `test 46 - write thesis chapter ~3h`() {
        val res = parse("write thesis chapter ~3h")
        assertEquals(180, res.effortMinutes)
    }

    @Test
    fun `test 47 - debug production memory leak 2 hrs work`() {
        val res = parse("debug production memory leak 2 hrs work")
        assertEquals(120, res.effortMinutes)
    }

    @Test
    fun `test 48 - read research paper 1_5h`() {
        val res = parse("read research paper 1.5h")
        assertEquals(90, res.effortMinutes)
    }

    @Test
    fun `test 49 - clean room 45 mins`() {
        val res = parse("clean room 45 mins")
        assertEquals(45, res.effortMinutes)
    }

    @Test
    fun `test 50 - critical server migration critical`() {
        val res = parse("critical server migration critical")
        assertTrue(res.critical)
    }

    @Test
    fun `test 51 - submit resume urgent`() {
        val res = parse("submit resume urgent")
        assertTrue(res.critical)
    }

    @Test
    fun `test 52 - project demo due thu 2pm`() {
        val res = parse("project demo due thu 2pm")
        assertEquals(DayOfWeek.THURSDAY, res.dueAt!!.atZone(zone).dayOfWeek)
        assertEquals(14, res.dueAt!!.atZone(zone).hour)
    }

    @Test
    fun `test 53 - take vitamins every day 8am`() {
        val res = parse("take vitamins every day 8am")
        assertEquals(Recurrence.Daily, res.recurrence)
    }

    @Test
    fun `test 54 - plant watering every 3 days`() {
        val res = parse("plant watering every 3 days")
        assertEquals(Recurrence.EveryNDays(3), res.recurrence)
    }

    @Test
    fun `test 55 - sync with mentor every wed`() {
        val res = parse("sync with mentor every wed")
        assertTrue(res.recurrence is Recurrence.Weekly)
        assertTrue((res.recurrence as Recurrence.Weekly).days.contains(DayOfWeek.WEDNESDAY.value))
    }

    // 56-65: Ambiguity & Clean Title Tests
    @Test
    fun `test 56 - bare time in past rolls to tomorrow`() {
        // Base now is 10:00 AM UTC. User specifies 8am without day -> rolls to tomorrow 8am!
        val res = parse("morning jog 8am")
        val zdt = res.dueAt!!.atZone(zone)
        assertEquals(9, zdt.dayOfMonth) // today is 8th
        assertEquals(8, zdt.hour)
    }

    @Test
    fun `test 57 - bare time in future stays today`() {
        // Base now is 10:00 AM UTC. User specifies 4pm -> today 4pm!
        val res = parse("dentist appointment 4pm")
        val zdt = res.dueAt!!.atZone(zone)
        assertEquals(8, zdt.dayOfMonth)
        assertEquals(16, zdt.hour)
    }

    @Test
    fun `test 58 - highlights token ranges match input substring`() {
        val input = "dbms assignment due fri 5pm 3h"
        val res = parse(input)
        assertTrue(res.tokens.isNotEmpty())
        for (token in res.tokens) {
            assertTrue(token.start >= 0)
            assertTrue(token.end <= input.length)
            assertTrue(token.end > token.start)
        }
    }

    @Test
    fun `test 59 - empty input returns clean empty result`() {
        val res = parse("")
        assertEquals("", res.title)
        assertEquals(null, res.dueAt)
        assertEquals(0, res.tokens.size)
    }

    @Test
    fun `test 60 - whitespace only returns clean empty result`() {
        val res = parse("   \t  ")
        assertEquals("", res.title)
        assertEquals(null, res.dueAt)
    }

    @Test
    fun `test 61 - complex mixed sentence`() {
        val res = parse("ai project submission deadline next fri 6pm ~4h urgent !!")
        assertEquals(ItemKind.DEADLINE, res.kindGuess)
        assertEquals(240, res.effortMinutes)
        assertTrue(res.critical)
        val zdt = res.dueAt!!.atZone(zone)
        assertEquals(DayOfWeek.FRIDAY, zdt.dayOfWeek)
        assertEquals(18, zdt.hour)
    }

    @Test
    fun `test 62 - plain reminder without any time tokens`() {
        val res = parse("buy milk and eggs")
        assertEquals("buy milk and eggs", res.title)
        assertEquals(ItemKind.REMINDER, res.kindGuess)
        assertEquals(null, res.effortMinutes)
        assertEquals(false, res.critical)
    }
}
