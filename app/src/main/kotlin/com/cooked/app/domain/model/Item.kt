package com.cooked.app.domain.model

import com.cooked.app.core.time.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@Serializable
data class Item(
    val id: String = UUID.randomUUID().toString(),
    val kind: ItemKind,
    val title: String,
    val note: String? = null,
    val emoji: String? = null,
    @Serializable(with = InstantSerializer::class)
    val dueAt: Instant,
    val allDay: Boolean = false,
    val timeZoneId: String = ZoneId.systemDefault().id,
    val recurrence: Recurrence? = null,
    val nag: NagPolicy = NagPolicy(),
    val critical: Boolean = false,
    val status: ItemStatus = ItemStatus.ACTIVE,
    @Serializable(with = InstantSerializer::class)
    val snoozedUntil: Instant? = null,
    @Serializable(with = InstantSerializer::class)
    val completedAt: Instant? = null,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant = Instant.now(),
    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant = Instant.now(),
    @Serializable(with = InstantSerializer::class)
    val deletedAt: Instant? = null,
    // Deadline-only fields
    @Serializable(with = InstantSerializer::class)
    val startBy: Instant? = null,
    val effortMinutes: Int? = null,
    val progress: Int = 0,
    val checklist: List<ChecklistStep> = emptyList()
) {
    val isOverdue: Boolean
        get() = status == ItemStatus.ACTIVE && Instant.now().isAfter(dueAt)

    val isCompleted: Boolean
        get() = status == ItemStatus.DONE

    val isDeleted: Boolean
        get() = deletedAt != null

    val isSnoozed: Boolean
        get() = snoozedUntil != null && snoozedUntil.isAfter(Instant.now())
}

