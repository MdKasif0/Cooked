package com.cooked.app.domain.backup

import android.content.Context
import androidx.room.withTransaction
import com.cooked.app.core.time.InstantSerializer
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.data.db.CookedDatabase
import com.cooked.app.data.entity.ChecklistStepEntity
import com.cooked.app.data.entity.ItemEntity
import com.cooked.app.data.entity.ItemEventEntity
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemEvent
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.scheduling.alarm.AlarmScheduler
import com.cooked.app.widget.WidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class BackupUserPrefs(
    val personality: String,
    val defaultNagInterval: Int,
    val dailyCapacityMinutes: Int
)

@Serializable
data class CookedBackupData(
    val schemaVersion: Int = 1,
    val exportedAt: String,
    val items: List<Item>,
    val events: List<ItemEvent> = emptyList(),
    val userPrefs: BackupUserPrefs? = null
)

data class ImportPreview(
    val totalItems: Int,
    val deadlinesCount: Int,
    val remindersCount: Int,
    val exportedAt: String,
    val backupData: CookedBackupData
)

@Singleton
class BackupManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: CookedDatabase,
    private val alarmScheduler: AlarmScheduler,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Serializes all items, checklist steps, events, and key user preferences into a JSON string.
     */
    suspend fun createBackupJson(): String {
        val itemDao = database.itemDao()
        val eventDao = database.itemEventDao()

        val itemsWithChecklist = itemDao.getAllNonDeletedItems()
        val domainItems = itemsWithChecklist.map { it.toDomain() }

        val now = Instant.now()
        val allEvents = eventDao.getEventsInRange(
            from = now.minus(java.time.Duration.ofDays(365)),
            to = now
        ).map { it.toDomain() }

        val prefs = userPreferencesRepository.userPrefs.first()
        val backupPrefs = BackupUserPrefs(
            personality = prefs.personality.name,
            defaultNagInterval = prefs.defaultNagInterval,
            dailyCapacityMinutes = prefs.dailyCapacityMinutes
        )

        val backup = CookedBackupData(
            schemaVersion = 1,
            exportedAt = now.toString(),
            items = domainItems,
            events = allEvents,
            userPrefs = backupPrefs
        )

        return json.encodeToString(backup)
    }

    /**
     * Parses and validates a backup JSON string, returning a preview of what will be imported.
     */
    fun previewImport(jsonString: String): Result<ImportPreview> {
        return try {
            val data = json.decodeFromString<CookedBackupData>(jsonString)
            if (data.schemaVersion > 1) {
                return Result.failure(
                    IllegalArgumentException("Backup format version ${data.schemaVersion} is newer than supported (1).")
                )
            }
            val deadlines = data.items.count { it.kind == ItemKind.DEADLINE }
            val reminders = data.items.count { it.kind == ItemKind.REMINDER }

            Result.success(
                ImportPreview(
                    totalItems = data.items.size,
                    deadlinesCount = deadlines,
                    remindersCount = reminders,
                    exportedAt = data.exportedAt,
                    backupData = data
                )
            )
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Invalid Cooked backup file: ${e.localizedMessage}", e))
        }
    }

    /**
     * Executes the import applying the conflict rule: **newer updatedAt wins**.
     * Automatically reschedules alarms and updates widgets.
     */
    suspend fun executeImport(data: CookedBackupData): Int {
        val itemDao = database.itemDao()
        val checklistStepDao = database.checklistStepDao()
        val eventDao = database.itemEventDao()

        var importedCount = 0

        database.withTransaction {
            for (importedItem in data.items) {
                val existing = itemDao.getItemWithChecklist(importedItem.id)
                val shouldUpsert = if (existing == null) {
                    true
                } else {
                    // Newer updatedAt wins
                    !existing.item.updatedAt.isAfter(importedItem.updatedAt)
                }

                if (shouldUpsert) {
                    itemDao.upsertItem(ItemEntity.fromDomain(importedItem))
                    checklistStepDao.deleteStepsFor(importedItem.id)
                    if (importedItem.checklist.isNotEmpty()) {
                        checklistStepDao.upsertSteps(
                            importedItem.checklist.map { ChecklistStepEntity.fromDomain(it) }
                        )
                    }
                    importedCount++
                }
            }

            if (data.events.isNotEmpty()) {
                val eventEntities = data.events.map { ItemEventEntity.fromDomain(it) }
                eventDao.insertEvents(eventEntities)
            }
        }

        // Reschedule alarms and refresh widgets
        alarmScheduler.rescheduleAll()
        WidgetUpdater.refreshAll(context)

        return importedCount
    }

    /**
     * Wipes all user data completely, cancels all alarms, and resets widgets.
     */
    suspend fun deleteAllData() {
        database.withTransaction {
            database.itemDao().clearAll()
            database.checklistStepDao().clearAll()
            database.itemEventDao().clearAll()
            database.weeklySnapshotDao().clearAll()
        }

        alarmScheduler.rescheduleAll()
        WidgetUpdater.refreshAll(context)
    }
}

