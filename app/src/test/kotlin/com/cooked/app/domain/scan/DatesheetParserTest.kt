package com.cooked.app.domain.scan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class DatesheetParserTest {

    private val zone = ZoneId.of("Asia/Kolkata")
    private val now = LocalDate.of(2026, 11, 1).atStartOfDay(zone).toInstant()

    @Test
    fun parse_chandigarhUniversityDatesheet() {
        val rawOcr = """
            CHANDIGARH UNIVERSITY
            END SEMESTER THEORY EXAMINATIONS DECEMBER 2026
            Date | Day | Course Code | Course Name | Time
            15-12-2026 | Tuesday | CST-301 | Database Management Systems | 09:30 AM
            18-12-2026 | Friday | CST-302 | Computer Networks | 09:30 AM
            22-12-2026 | Tuesday | CST-303 | Operating Systems | 02:00 PM
        """.trimIndent()

        val results = DatesheetParser.parse(rawOcr, now, zone)
        assertEquals(3, results.size)

        val dbms = results[0]
        assertTrue("Expected DBMS in title but got ${dbms.title}", dbms.title.contains("Database Management Systems", ignoreCase = true))
        val dbmsDate = dbms.dueAt.atZone(zone)
        assertEquals(LocalDate.of(2026, 12, 15), dbmsDate.toLocalDate())
        assertEquals(LocalTime.of(9, 30), dbmsDate.toLocalTime())
        assertEquals(600, dbms.effortMinutes)

        val os = results[2]
        assertTrue("Expected Operating Systems in title but got ${os.title}", os.title.contains("Operating Systems", ignoreCase = true))
        val osDate = os.dueAt.atZone(zone)
        assertEquals(LocalDate.of(2026, 12, 22), osDate.toLocalDate())
        assertEquals(LocalTime.of(14, 0), osDate.toLocalTime())
    }

    @Test
    fun parse_aktuSemesterSchedule() {
        val rawOcr = """
            DR. A.P.J. ABDUL KALAM TECHNICAL UNIVERSITY, UTTAR PRADESH
            ODD SEMESTER EXAM SCHEDULE 2026-27
            Date: 10/12/2026 Shift: 1 Time: 09:30 AM Paper: KCS-501 Software Engineering
            Date: 14/12/2026 Shift: 2 Time: 02:00 PM Paper: KCS-502 Compiler Design
        """.trimIndent()

        val results = DatesheetParser.parse(rawOcr, now, zone)
        assertEquals(2, results.size)

        val se = results[0]
        assertTrue(se.title.contains("Software Engineering", ignoreCase = true))
        assertEquals(LocalDate.of(2026, 12, 10), se.dueAt.atZone(zone).toLocalDate())
        assertEquals(LocalTime.of(9, 30), se.dueAt.atZone(zone).toLocalTime())

        val cd = results[1]
        assertTrue(cd.title.contains("Compiler Design", ignoreCase = true))
        assertEquals(LocalDate.of(2026, 12, 14), cd.dueAt.atZone(zone).toLocalDate())
        assertEquals(LocalTime.of(14, 0), cd.dueAt.atZone(zone).toLocalTime())
    }

    @Test
    fun parse_mumbaiUniversityShiftTable() {
        val rawOcr = """
            UNIVERSITY OF MUMBAI
            EXAMINATION TIME TABLE - WINTER 2026
            15 Dec 2026 | Morning Session | Applied Mathematics IV
            19 Dec 2026 | Evening Session | Microprocessors and Applications
        """.trimIndent()

        val results = DatesheetParser.parse(rawOcr, now, zone)
        assertEquals(2, results.size)

        val maths = results[0]
        assertTrue(maths.title.contains("Applied Mathematics", ignoreCase = true))
        assertEquals(LocalDate.of(2026, 12, 15), maths.dueAt.atZone(zone).toLocalDate())
        assertEquals(LocalTime.of(9, 30), maths.dueAt.atZone(zone).toLocalTime())

        val micro = results[1]
        assertTrue(micro.title.contains("Microprocessors", ignoreCase = true))
        assertEquals(LocalDate.of(2026, 12, 19), micro.dueAt.atZone(zone).toLocalDate())
        assertEquals(LocalTime.of(14, 0), micro.dueAt.atZone(zone).toLocalTime())
    }

    @Test
    fun parse_cbseBoardExamTimetable() {
        val rawOcr = """
            CENTRAL BOARD OF SECONDARY EDUCATION
            SENIOR SCHOOL CERTIFICATE EXAMINATION 2027
            15th February 2027 10:30 AM 041 MATHEMATICS
            20th February 2027 10:30 AM 042 PHYSICS
            25th February 2027 10:30 AM 043 CHEMISTRY
        """.trimIndent()

        val results = DatesheetParser.parse(rawOcr, now, zone)
        assertEquals(3, results.size)

        val maths = results[0]
        assertTrue(maths.title.contains("MATHEMATICS", ignoreCase = true))
        assertEquals(LocalDate.of(2027, 2, 15), maths.dueAt.atZone(zone).toLocalDate())
        assertEquals(LocalTime.of(10, 30), maths.dueAt.atZone(zone).toLocalTime())
    }

    @Test
    fun parse_internalAssessmentNotice() {
        val rawOcr = """
            DEPARTMENT OF COMPUTER SCIENCE
            Mid-Term Internal Evaluation Notice
            Machine Learning Assignment submission deadline 25 Nov 2026 5pm
            Cloud Computing Lab Project due 28 Nov 2026 6pm
        """.trimIndent()

        val results = DatesheetParser.parse(rawOcr, now, zone)
        assertEquals(2, results.size)

        val ml = results[0]
        assertTrue(ml.title.contains("Machine Learning", ignoreCase = true))
        assertEquals(LocalDate.of(2026, 11, 25), ml.dueAt.atZone(zone).toLocalDate())
        assertEquals(LocalTime.of(17, 0), ml.dueAt.atZone(zone).toLocalTime())

        val cloud = results[1]
        assertTrue(cloud.title.contains("Cloud Computing", ignoreCase = true))
        assertEquals(LocalDate.of(2026, 11, 28), cloud.dueAt.atZone(zone).toLocalDate())
        assertEquals(LocalTime.of(18, 0), cloud.dueAt.atZone(zone).toLocalTime())
    }
}
