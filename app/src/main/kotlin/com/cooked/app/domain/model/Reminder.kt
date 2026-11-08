package com.cooked.app.domain.model

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

