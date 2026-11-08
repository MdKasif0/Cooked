package com.cooked.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cooked.app.domain.model.ChecklistStep
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.model.NagPolicy
import com.cooked.app.domain.model.Recurrence
import java.time.Instant

@Entity(
    tableName = "items",
    indices = [
        Index(value = ["status", "dueAt"]),
        Index(value = ["kind", "status"]),
        Index(value = ["deletedAt"])
    ]
)
data class ItemEntity(
    @PrimaryKey
    val id: String,
    val kind: ItemKind,
    val title: String,
    val note: String?,
    val emoji: String?,
    val dueAt: Instant,
    val allDay: Boolean,
    val timeZoneId: String,
    val recurrence: Recurrence?,
    val nag: NagPolicy,
    val critical: Boolean,
    val status: ItemStatus,
    val snoozedUntil: Instant?,
    val completedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
    // Deadline-only fields
    val startBy: Instant?,
    val effortMinutes: Int?,
    val progress: Int
) {
    fun toDomain(checklist: List<ChecklistStep> = emptyList()): Item = Item(
        id = id,
        kind = kind,
        title = title,
        note = note,
        emoji = emoji,
        dueAt = dueAt,
        allDay = allDay,
        timeZoneId = timeZoneId,
        recurrence = recurrence,
        nag = nag,
        critical = critical,
        status = status,
        snoozedUntil = snoozedUntil,
        completedAt = completedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        startBy = startBy,
        effortMinutes = effortMinutes,
        progress = progress,
        checklist = checklist
    )

    companion object {
        fun fromDomain(item: Item): ItemEntity = ItemEntity(
            id = item.id,
            kind = item.kind,
            title = item.title,
            note = item.note,
            emoji = item.emoji,
            dueAt = item.dueAt,
            allDay = item.allDay,
            timeZoneId = item.timeZoneId,
            recurrence = item.recurrence,
            nag = item.nag,
            critical = item.critical,
            status = item.status,
            snoozedUntil = item.snoozedUntil,
            completedAt = item.completedAt,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt,
            deletedAt = item.deletedAt,
            startBy = item.startBy,
            effortMinutes = item.effortMinutes,
            progress = item.progress
        )
    }
}

