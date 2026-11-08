package com.cooked.app.domain.repository

import com.cooked.app.domain.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import java.time.Instant

interface ItemRepository {
    val itemChanged: SharedFlow<String>

    fun observeToday(): Flow<List<Item>>
    fun observeUpcoming(limit: Int = 50): Flow<List<Item>>
    fun observeOverdue(): Flow<List<Item>>
    fun observeDeadlines(): Flow<List<Item>>
    fun observeItem(id: String): Flow<Item?>

    suspend fun getItem(id: String): Item?
    suspend fun getActiveItems(): List<Item>
    suspend fun getNagCount(id: String): Int
    suspend fun hasStartedRecently(id: String): Boolean
    suspend fun logEvent(
        itemId: String,
        type: com.cooked.app.domain.model.ItemEventType,
        meta: Map<String, String> = emptyMap(),
        at: Instant = Instant.now()
    )
    suspend fun create(item: Item)
    suspend fun update(item: Item)
    suspend fun markDone(id: String, at: Instant = Instant.now())
    suspend fun undoDone(id: String)
    suspend fun snooze(id: String, until: Instant)
    suspend fun reschedule(id: String, newDue: Instant)
    suspend fun delete(id: String)
    suspend fun restore(id: String)
}

