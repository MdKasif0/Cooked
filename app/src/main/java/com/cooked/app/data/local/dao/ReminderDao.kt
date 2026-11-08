package com.cooked.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cooked.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, dueInstant ASC, createdAt DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY dueInstant ASC, createdAt DESC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND isDeadlineMode = 1 ORDER BY dueInstant ASC")
    fun getActiveDeadlines(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: String): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: ReminderEntity)

    @Query("UPDATE reminders SET isCompleted = 1, completedAt = :completedAt, isNaggingActive = 0, updatedAt = :completedAt WHERE id = :id")
    suspend fun markCompleted(id: String, completedAt: Long)

    @Query("UPDATE reminders SET snoozedUntil = :snoozeUntil, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setSnooze(id: String, snoozeUntil: Long, updatedAt: Long)

    @Query("UPDATE reminders SET isNaggingActive = :isNagging, nagCount = CASE WHEN :incrementNag = 1 THEN nagCount + 1 ELSE nagCount END, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setNagging(id: String, isNagging: Boolean, incrementNag: Boolean, updatedAt: Long)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: String)
}
