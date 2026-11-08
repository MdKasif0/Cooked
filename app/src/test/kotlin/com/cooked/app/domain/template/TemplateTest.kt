package com.cooked.app.domain.template

import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.Recurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplateTest {

    @Test
    fun builtInTemplates_hasAllSevenTemplates() {
        val templates = BuiltInTemplates.all
        assertEquals(7, templates.size)

        val assignment = templates.find { it.id == "template_assignment" }
        assertNotNull(assignment)
        assertEquals(ItemKind.DEADLINE, assignment?.kind)
        assertEquals(180, assignment?.effortMinutes)
        assertEquals(15, assignment?.nagIntervalMinutes)

        val exam = templates.find { it.id == "template_exam" }
        assertNotNull(exam)
        assertEquals(ItemKind.DEADLINE, exam?.kind)
        assertEquals(600, exam?.effortMinutes)
        assertEquals(7, exam?.startByDaysBefore)

        val billRent = templates.find { it.id == "template_bill_rent" }
        assertNotNull(billRent)
        assertEquals(ItemKind.REMINDER, billRent?.kind)
        assertTrue(billRent?.recurrence is Recurrence.Monthly)
        assertTrue(billRent?.isCritical == true)

        val renewal = templates.find { it.id == "template_renewal" }
        assertNotNull(renewal)
        assertEquals(ItemKind.REMINDER, renewal?.kind)
        assertEquals(Recurrence.Yearly, renewal?.recurrence)

        val recharge = templates.find { it.id == "template_recharge" }
        assertNotNull(recharge)
        assertEquals(ItemKind.REMINDER, recharge?.kind)
        assertEquals(Recurrence.EveryNDays(28), recharge?.recurrence)

        val routine = templates.find { it.id == "template_routine" }
        assertNotNull(routine)
        assertEquals(ItemKind.REMINDER, routine?.kind)
        assertEquals(Recurrence.Daily, routine?.recurrence)
        assertEquals(5, routine?.nagIntervalMinutes)

        val call = templates.find { it.id == "template_call" }
        assertNotNull(call)
        assertEquals(ItemKind.REMINDER, call?.kind)
        assertEquals(30, call?.nagIntervalMinutes)
    }
}
