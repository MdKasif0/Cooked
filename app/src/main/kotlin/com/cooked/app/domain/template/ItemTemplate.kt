package com.cooked.app.domain.template

import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.Recurrence
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ItemTemplate(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String,
    val kind: ItemKind,
    val defaultTitle: String = "",
    val effortMinutes: Int? = null,
    val recurrence: Recurrence? = null,
    val nagIntervalMinutes: Int? = null,
    val isCritical: Boolean = false,
    val isCustom: Boolean = false,
    val startByDaysBefore: Int? = null
)

object BuiltInTemplates {

    val all: List<ItemTemplate> = listOf(
        ItemTemplate(
            id = "template_assignment",
            name = "Assignment",
            emoji = "assignment",
            kind = ItemKind.DEADLINE,
            defaultTitle = "Assignment submission",
            effortMinutes = 180, // 3h
            nagIntervalMinutes = 15,
            startByDaysBefore = 2
        ),
        ItemTemplate(
            id = "template_exam",
            name = "Exam",
            emoji = "exam",
            kind = ItemKind.DEADLINE,
            defaultTitle = "Exam preparation",
            effortMinutes = 600, // 10h
            nagIntervalMinutes = 15,
            startByDaysBefore = 7
        ),
        ItemTemplate(
            id = "template_bill_rent",
            name = "Bill / Rent",
            emoji = "bill",
            kind = ItemKind.REMINDER,
            defaultTitle = "Pay rent / utility bill",
            recurrence = Recurrence.Monthly(dayOfMonth = 1),
            isCritical = true
        ),
        ItemTemplate(
            id = "template_renewal",
            name = "Renewal",
            emoji = "renewal",
            kind = ItemKind.REMINDER,
            defaultTitle = "Annual subscription renewal",
            recurrence = Recurrence.Yearly
        ),
        ItemTemplate(
            id = "template_recharge",
            name = "Mobile Recharge",
            emoji = "mobile",
            kind = ItemKind.REMINDER,
            defaultTitle = "Recharge mobile plan",
            recurrence = Recurrence.EveryNDays(28)
        ),
        ItemTemplate(
            id = "template_routine",
            name = "Daily Routine",
            emoji = "routine",
            kind = ItemKind.REMINDER,
            defaultTitle = "Daily scheduled routine",
            recurrence = Recurrence.Daily,
            nagIntervalMinutes = 5
        ),
        ItemTemplate(
            id = "template_call",
            name = "Call someone",
            emoji = "call",
            kind = ItemKind.REMINDER,
            defaultTitle = "Call ",
            nagIntervalMinutes = 30
        )
    )
}

