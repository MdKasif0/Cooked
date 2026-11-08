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

    @Query("SELECT count(*) FROM item_events WHERE itemId = :itemId AND type = 'NAGGED'")
    suspend fun getNagCountFor(itemId: String): Int

    @Query("SELECT * FROM item_events WHERE type = :type AND at >= :from AND at <= :to")
    suspend fun getEventsByTypeInRange(type: ItemEventType, from: Instant, to: Instant): List<ItemEventEntity>

    @Query("DELETE FROM item_events WHERE id = :id")
    suspend fun deleteEvent(id: String)

    @Query("DELETE FROM item_events")
    suspend fun clearAll()
}

