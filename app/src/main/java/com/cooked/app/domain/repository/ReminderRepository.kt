package com.cooked.app.domain.repository

import com.cooked.app.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getAllReminders(): Flow<List<Reminder>>
    fun getActiveReminders(): Flow<List<Reminder>>
    fun getCompletedReminders(): Flow<List<Reminder>>
    fun getActiveDeadlines(): Flow<List<Reminder>>
    suspend fun getReminderById(id: String): Reminder?
    suspend fun insertOrUpdate(reminder: Reminder)
    suspend fun completeReminder(id: String, completedAtUtcMillis: Long)
    suspend fun snoozeReminder(id: String, snoozeUntilUtcMillis: Long)
    suspend fun setNaggingActive(id: String, isNagging: Boolean, incrementNagCount: Boolean = false)
    suspend fun deleteReminder(id: String)
}

