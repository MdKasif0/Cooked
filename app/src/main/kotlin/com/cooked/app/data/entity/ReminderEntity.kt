package com.cooked.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cooked.app.domain.model.Reminder

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey
    val id: String,                         // UUID string
    val title: String,
    val note: String?,
    val dueInstant: Long?,                  // Epoch millis UTC
    val nagIntervalMinutes: Int,            // Nag frequency (minutes)
    val isCompleted: Boolean,
    val completedAt: Long?,                 // Epoch millis UTC
    val isNaggingActive: Boolean,
    val nagCount: Int,
    val snoozedUntil: Long?,                // Epoch millis UTC
    val isDeadlineMode: Boolean,
    val deadlineStartInstant: Long?,        // Epoch millis UTC
    val estimatedEffortMinutes: Int,
    val createdAt: Long,                    // Epoch millis UTC
    val updatedAt: Long                     // Epoch millis UTC
) {
    fun toDomain(): Reminder = Reminder(
        id = id,
        title = title,
        note = note,
        dueInstant = dueInstant,
        nagIntervalMinutes = nagIntervalMinutes,
        isCompleted = isCompleted,
        completedAt = completedAt,
        isNaggingActive = isNaggingActive,
        nagCount = nagCount,
        snoozedUntil = snoozedUntil,
        isDeadlineMode = isDeadlineMode,
        deadlineStartInstant = deadlineStartInstant,
        estimatedEffortMinutes = estimatedEffortMinutes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(domain: Reminder): ReminderEntity = ReminderEntity(
            id = domain.id,
            title = domain.title,
            note = domain.note,
            dueInstant = domain.dueInstant,
            nagIntervalMinutes = domain.nagIntervalMinutes,
            isCompleted = domain.isCompleted,
            completedAt = domain.completedAt,
            isNaggingActive = domain.isNaggingActive,
            nagCount = domain.nagCount,
            snoozedUntil = domain.snoozedUntil,
            isDeadlineMode = domain.isDeadlineMode,
            deadlineStartInstant = domain.deadlineStartInstant,
            estimatedEffortMinutes = domain.estimatedEffortMinutes,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}

