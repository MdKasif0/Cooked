package com.cooked.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.cooked.app.data.entity.ItemEntity
import com.cooked.app.data.entity.ItemWithChecklist
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface ItemDao {

    @Transaction
    @Query("SELECT * FROM items WHERE deletedAt IS NULL AND status = 'ACTIVE' ORDER BY dueAt ASC")
    fun observeActiveItems(): Flow<List<ItemWithChecklist>>

    @Transaction
    @Query("SELECT * FROM items WHERE deletedAt IS NULL AND status = 'ACTIVE' AND dueAt >= :startOfDay AND dueAt <= :endOfDay ORDER BY dueAt ASC")
    fun observeToday(startOfDay: Instant, endOfDay: Instant): Flow<List<ItemWithChecklist>>

    @Transaction
    @Query("SELECT * FROM items WHERE deletedAt IS NULL AND status = 'ACTIVE' AND dueAt < :now ORDER BY dueAt ASC")
    fun observeOverdue(now: Instant): Flow<List<ItemWithChecklist>>

    @Transaction
    @Query("SELECT * FROM items WHERE deletedAt IS NULL AND status = 'ACTIVE' AND dueAt >= :now ORDER BY dueAt ASC LIMIT :limit")
    fun observeUpcoming(now: Instant, limit: Int): Flow<List<ItemWithChecklist>>

    @Transaction
    @Query("SELECT * FROM items WHERE deletedAt IS NULL AND kind = 'DEADLINE' AND status = 'ACTIVE' ORDER BY dueAt ASC")
    fun observeDeadlinesByUrgency(): Flow<List<ItemWithChecklist>>

    @Transaction
    @Query("SELECT * FROM items WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    fun observeItemWithChecklist(id: String): Flow<ItemWithChecklist?>

    @Transaction
    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getItemWithChecklist(id: String): ItemWithChecklist?

    @Transaction
    @Query("SELECT * FROM items WHERE deletedAt IS NULL")
    suspend fun getAllNonDeletedItems(): List<ItemWithChecklist>

    @Upsert
    suspend fun upsertItem(item: ItemEntity)

    @Upsert
    suspend fun upsertItems(items: List<ItemEntity>)

    @Query("UPDATE items SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Instant, updatedAt: Instant)

    @Query("UPDATE items SET deletedAt = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restore(id: String, updatedAt: Instant)

    @Query("UPDATE items SET dueAt = :newDueAt, snoozedUntil = NULL, status = 'ACTIVE', completedAt = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun rollRecurringOccurrence(id: String, newDueAt: Instant, updatedAt: Instant)

    @Query("UPDATE items SET status = 'DONE', completedAt = :completedAt, snoozedUntil = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markCompleted(id: String, completedAt: Instant, updatedAt: Instant)

    @Query("UPDATE items SET status = 'ACTIVE', completedAt = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markActive(id: String, updatedAt: Instant)

    @Query("UPDATE items SET snoozedUntil = :snoozedUntil, updatedAt = :updatedAt WHERE id = :id")
    suspend fun snooze(id: String, snoozedUntil: Instant, updatedAt: Instant)

    @Query("UPDATE items SET dueAt = :newDueAt, snoozedUntil = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun reschedule(id: String, newDueAt: Instant, updatedAt: Instant)

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deletePermanently(id: String)

    @Query("DELETE FROM items")
    suspend fun clearAll()
}

