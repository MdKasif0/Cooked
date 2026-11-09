package com.cooked.app.data.repository

import androidx.room.withTransaction
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.dao.ChecklistStepDao
import com.cooked.app.data.dao.ItemDao
import com.cooked.app.data.dao.ItemEventDao
import com.cooked.app.data.db.CookedDatabase
import com.cooked.app.data.entity.ChecklistStepEntity
import com.cooked.app.data.entity.ItemEntity
import com.cooked.app.data.entity.ItemEventEntity
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.repository.ItemRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val database: CookedDatabase,
    private val itemDao: ItemDao,
    private val checklistStepDao: ChecklistStepDao,
    private val itemEventDao: ItemEventDao,
    private val clock: CookedClock
) : ItemRepository {

    private val _itemChanged = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val itemChanged = _itemChanged.asSharedFlow()

    override fun observeToday(): Flow<List<Item>> {
        val now = clock.now()
        val zone = clock.zone()
        val today = now.atZone(zone).toLocalDate()
        val startOfDay = today.atStartOfDay(zone).toInstant()
        val endOfDay = today.plusDays(1).atStartOfDay(zone).toInstant().minus(1, ChronoUnit.MILLIS)

        return itemDao.observeToday(startOfDay, endOfDay).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeCompletedToday(): Flow<List<Item>> {
        val now = clock.now()
        val zone = clock.zone()
        val today = now.atZone(zone).toLocalDate()
        val startOfDay = today.atStartOfDay(zone).toInstant()
        val endOfDay = today.plusDays(1).atStartOfDay(zone).toInstant().minus(1, ChronoUnit.MILLIS)

        return itemDao.observeCompletedToday(startOfDay, endOfDay).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeUpcoming(limit: Int): Flow<List<Item>> {
        val now = clock.now()
        return itemDao.observeUpcoming(now, limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeOverdue(): Flow<List<Item>> {
        val now = clock.now()
        return itemDao.observeOverdue(now).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeDeadlines(): Flow<List<Item>> {
        return itemDao.observeDeadlinesByUrgency().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeItem(id: String): Flow<Item?> {
        return itemDao.observeItemWithChecklist(id).map { it?.toDomain() }
    }

    override fun observeEvents(itemId: String): Flow<List<com.cooked.app.domain.model.ItemEvent>> {
        return itemEventDao.observeEventsFor(itemId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getItem(id: String): Item? {
        return itemDao.getItemWithChecklist(id)?.toDomain()
    }

    override suspend fun getActiveItems(): List<Item> {
        return itemDao.getActiveItems().map { it.toDomain() }
    }

    override suspend fun getNagCount(id: String): Int {
        return itemEventDao.getNagCountFor(id)
    }

    override suspend fun hasStartedRecently(id: String): Boolean {
        val event = itemEventDao.getLatestEventFor(id, ItemEventType.STARTED) ?: return false
        val now = clock.now()
        return Duration.between(event.at, now).toHours() < 24
    }

    override suspend fun logEvent(
        itemId: String,
        type: ItemEventType,
        meta: Map<String, String>,
        at: Instant
    ) {
        val now = clock.now()
        val effectiveAt = if (at == Instant.EPOCH) now else at
        itemEventDao.insertEvent(
            ItemEventEntity(
                id = UUID.randomUUID().toString(),
                itemId = itemId,
                type = type,
                at = effectiveAt,
                meta = meta
            )
        )
    }

    override suspend fun create(item: Item) {
        val now = clock.now()
        val itemToSave = item.copy(
            createdAt = if (item.createdAt == Instant.EPOCH) now else item.createdAt,
            updatedAt = now
        )

        database.withTransaction {
            itemDao.upsertItem(ItemEntity.fromDomain(itemToSave))
            if (itemToSave.checklist.isNotEmpty()) {
                checklistStepDao.upsertSteps(itemToSave.checklist.map { ChecklistStepEntity.fromDomain(it) })
            }
            itemEventDao.insertEvent(
                ItemEventEntity(
                    id = UUID.randomUUID().toString(),
                    itemId = itemToSave.id,
                    type = ItemEventType.CREATED,
                    at = now,
                    meta = emptyMap()
                )
            )
        }
        _itemChanged.emit(itemToSave.id)
    }

    override suspend fun update(item: Item) {
        val now = clock.now()
        val itemToSave = item.copy(updatedAt = now)

        database.withTransaction {
            itemDao.upsertItem(ItemEntity.fromDomain(itemToSave))
            checklistStepDao.deleteStepsFor(itemToSave.id)
            if (itemToSave.checklist.isNotEmpty()) {
                checklistStepDao.upsertSteps(itemToSave.checklist.map { ChecklistStepEntity.fromDomain(it) })
            }
            itemEventDao.insertEvent(
                ItemEventEntity(
                    id = UUID.randomUUID().toString(),
                    itemId = itemToSave.id,
                    type = ItemEventType.EDITED,
                    at = now,
                    meta = emptyMap()
                )
            )
        }
        _itemChanged.emit(itemToSave.id)
    }

    override suspend fun markDone(id: String, at: Instant) {
        val existing = itemDao.getItemWithChecklist(id)?.toDomain() ?: return
        val now = clock.now()
        val effectiveAt = if (at == Instant.EPOCH) now else at

        val lateByMinutes = if (effectiveAt.isAfter(existing.dueAt)) {
            Duration.between(existing.dueAt, effectiveAt).toMinutes()
        } else {
            0L
        }

        database.withTransaction {
            if (existing.recurrence != null) {
                // Recurring item: calculate next occurrence and roll dueAt without duplicating row
                val zone = try {
                    ZoneId.of(existing.timeZoneId)
                } catch (_: Exception) {
                    clock.zone()
                }
                val nextDueAt = existing.recurrence.nextOccurrence(existing.dueAt, zone)

                itemDao.rollRecurringOccurrence(
                    id = id,
                    newDueAt = nextDueAt,
                    updatedAt = now
                )

                itemEventDao.insertEvent(
                    ItemEventEntity(
                        id = UUID.randomUUID().toString(),
                        itemId = id,
                        type = ItemEventType.DONE,
                        at = effectiveAt,
                        meta = mapOf(
                            "lateByMinutes" to lateByMinutes.toString(),
                            "recurring" to "true",
                            "previousDueAt" to existing.dueAt.toString(),
                            "rolledDueAt" to nextDueAt.toString()
                        )
                    )
                )
            } else {
                // Non-recurring item: mark DONE
                itemDao.markCompleted(
                    id = id,
                    completedAt = effectiveAt,
                    updatedAt = now
                )

                itemEventDao.insertEvent(
                    ItemEventEntity(
                        id = UUID.randomUUID().toString(),
                        itemId = id,
                        type = ItemEventType.DONE,
                        at = effectiveAt,
                        meta = mapOf(
                            "lateByMinutes" to lateByMinutes.toString()
                        )
                    )
                )
            }
        }
        _itemChanged.emit(id)
    }

    override suspend fun undoDone(id: String) {
        val existing = itemDao.getItemWithChecklist(id)?.toDomain() ?: return
        val now = clock.now()
        val lastDoneEvent = itemEventDao.getLatestEventFor(id, ItemEventType.DONE)

        database.withTransaction {
            val previousDueAtStr = lastDoneEvent?.meta?.get("previousDueAt")
            if (previousDueAtStr != null) {
                // Rolled recurring item: restore previous dueAt
                val restoredDueAt = Instant.parse(previousDueAtStr)
                itemDao.reschedule(id = id, newDueAt = restoredDueAt, updatedAt = now)
            } else {
                // Normal item: set status back to ACTIVE
                itemDao.markActive(id = id, updatedAt = now)
            }

            itemEventDao.insertEvent(
                ItemEventEntity(
                    id = UUID.randomUUID().toString(),
                    itemId = id,
                    type = ItemEventType.UNDONE,
                    at = now,
                    meta = emptyMap()
                )
            )
        }
        _itemChanged.emit(id)
    }

    override suspend fun snooze(id: String, until: Instant) {
        val now = clock.now()
        val snoozeMinutes = Duration.between(now, until).toMinutes().coerceAtLeast(0)

        database.withTransaction {
            itemDao.snooze(id = id, snoozedUntil = until, updatedAt = now)
            itemEventDao.insertEvent(
                ItemEventEntity(
                    id = UUID.randomUUID().toString(),
                    itemId = id,
                    type = ItemEventType.SNOOZED,
                    at = now,
                    meta = mapOf(
                        "snoozeUntil" to until.toString(),
                        "snoozeMinutes" to snoozeMinutes.toString()
                    )
                )
            )
        }
        _itemChanged.emit(id)
    }

    override suspend fun reschedule(id: String, newDue: Instant) {
        val now = clock.now()
        database.withTransaction {
            itemDao.reschedule(id = id, newDueAt = newDue, updatedAt = now)
            itemEventDao.insertEvent(
                ItemEventEntity(
                    id = UUID.randomUUID().toString(),
                    itemId = id,
                    type = ItemEventType.RESCHEDULED,
                    at = now,
                    meta = mapOf(
                        "newDueAt" to newDue.toString()
                    )
                )
            )
        }
        _itemChanged.emit(id)
    }

    override suspend fun delete(id: String) {
        val now = clock.now()
        database.withTransaction {
            itemDao.softDelete(id = id, deletedAt = now, updatedAt = now)
            itemEventDao.insertEvent(
                ItemEventEntity(
                    id = UUID.randomUUID().toString(),
                    itemId = id,
                    type = ItemEventType.EDITED,
                    at = now,
                    meta = mapOf("action" to "soft_delete")
                )
            )
        }
        _itemChanged.emit(id)
    }

    override suspend fun restore(id: String) {
        val now = clock.now()
        database.withTransaction {
            itemDao.restore(id = id, updatedAt = now)
            itemEventDao.insertEvent(
                ItemEventEntity(
                    id = UUID.randomUUID().toString(),
                    itemId = id,
                    type = ItemEventType.EDITED,
                    at = now,
                    meta = mapOf("action" to "restore")
                )
            )
        }
        _itemChanged.emit(id)
    }
}

