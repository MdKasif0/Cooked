package com.cooked.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cooked.app.data.entity.ItemEventEntity
import com.cooked.app.domain.model.ItemEventType
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface ItemEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: ItemEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<ItemEventEntity>)

    @Query("SELECT * FROM item_events WHERE itemId = :itemId ORDER BY at ASC")
    fun observeEventsFor(itemId: String): Flow<List<ItemEventEntity>>

    @Query("SELECT * FROM item_events WHERE at >= :from AND at <= :to ORDER BY at ASC")
    fun observeEventsInRange(from: Instant, to: Instant): Flow<List<ItemEventEntity>>

    @Query("SELECT * FROM item_events WHERE at >= :from AND at <= :to ORDER BY at ASC")
    suspend fun getEventsInRange(from: Instant, to: Instant): List<ItemEventEntity>

    @Query("SELECT * FROM item_events WHERE itemId = :itemId AND type = :type ORDER BY at DESC LIMIT 1")
    suspend fun getLatestEventFor(itemId: String, type: ItemEventType): ItemEventEntity?

    /**
     * Number of times the alarm has fired for the item's *current* occurrence.
     *
     * Counts both the first fire (FIRED) and follow-up nags (NAGGED), but only those at or after
     * the item's current dueAt. Pre-due nudges are excluded, and rolling a recurring item to its
     * next occurrence (which moves dueAt forward) naturally resets the count.
     */
    @Query(
        """
        SELECT count(*) FROM item_events e
        INNER JOIN items i ON e.itemId = i.id
        WHERE e.itemId = :itemId AND e.type IN ('FIRED', 'NAGGED') AND e.at >= i.dueAt
        """
    )
    suspend fun getNagCountFor(itemId: String): Int

    @Query("SELECT * FROM item_events WHERE type = :type AND at >= :from AND at <= :to")
    suspend fun getEventsByTypeInRange(type: ItemEventType, from: Instant, to: Instant): List<ItemEventEntity>

    @Query("DELETE FROM item_events WHERE id = :id")
    suspend fun deleteEvent(id: String)

    @Query("DELETE FROM item_events")
    suspend fun clearAll()
}

