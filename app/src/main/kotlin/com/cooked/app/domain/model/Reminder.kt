package com.cooked.app.domain.model

import java.time.Instant
import java.util.UUID

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val note: String? = null,
    val dueInstant: Long? = null,              // Epoch millis UTC
    val nagIntervalMinutes: Int = 10,          // Re-alert interval until done
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,             // Epoch millis UTC
    val isNaggingActive: Boolean = false,
    val nagCount: Int = 0,
    val snoozedUntil: Long? = null,            // Epoch millis UTC
    val isDeadlineMode: Boolean = false,
    val deadlineStartInstant: Long? = null,    // Epoch millis UTC
    val estimatedEffortMinutes: Int = 60,      // Default 1 hour
    val createdAt: Long,                       // Epoch millis UTC
    val updatedAt: Long                        // Epoch millis UTC
) {
    val isOverdue: Boolean
        get() = dueInstant != null && !isCompleted && System.currentTimeMillis() > dueInstant

    val isNagging: Boolean
        get() = !isCompleted && (isNaggingActive || isOverdue)
}

fun Item.toReminder(): Reminder = Reminder(
    id = id,
    title = title,
    note = note,
    dueInstant = dueAt.toEpochMilli(),
    nagIntervalMinutes = nag.intervalMinutes,
    isCompleted = isCompleted,
    completedAt = completedAt?.toEpochMilli(),
    isNaggingActive = !isCompleted && isOverdue,
    snoozedUntil = snoozedUntil?.toEpochMilli(),
    isDeadlineMode = kind == ItemKind.DEADLINE,
    deadlineStartInstant = startBy?.toEpochMilli(),
    estimatedEffortMinutes = effortMinutes ?: 60,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli()
)

fun Reminder.toItem(): Item = Item(
    id = id,
    kind = if (isDeadlineMode) ItemKind.DEADLINE else ItemKind.REMINDER,
    title = title,
    note = note,
    dueAt = dueInstant?.let { Instant.ofEpochMilli(it) } ?: Instant.now(),
    nag = NagPolicy(intervalMinutes = nagIntervalMinutes),
    status = if (isCompleted) ItemStatus.DONE else ItemStatus.ACTIVE,
    snoozedUntil = snoozedUntil?.let { Instant.ofEpochMilli(it) },
    completedAt = completedAt?.let { Instant.ofEpochMilli(it) },
    startBy = deadlineStartInstant?.let { Instant.ofEpochMilli(it) },
    effortMinutes = estimatedEffortMinutes,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt)
)
