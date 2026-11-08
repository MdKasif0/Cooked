package com.cooked.app.domain.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.data.db.CookedDatabase
import com.cooked.app.data.entity.ChecklistStepEntity
import com.cooked.app.data.entity.ItemEntity
import com.cooked.app.domain.model.ChecklistStep
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.model.NagPolicy
import com.cooked.app.scheduling.alarm.AlarmScheduler
import com.cooked.app.scheduling.alarm.ScheduledAlarmInfo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupManagerTest {

    private lateinit var context: Context
    private lateinit var database: CookedDatabase
    private lateinit var userPrefsRepo: UserPreferencesRepository
    private lateinit var alarmScheduler: FakeAlarmScheduler
    private lateinit var backupManager: BackupManager

    private class FakeAlarmScheduler : AlarmScheduler {
        var rescheduleCount = 0
        override suspend fun schedule(item: Item, nagCount: Int?) {}
        override suspend fun schedule(itemId: String) {}
        override fun cancel(itemId: String) {}
        override suspend fun rescheduleAll() {
            rescheduleCount++
        }
        override fun getScheduledAlarms(): List<ScheduledAlarmInfo> = emptyList()
        override suspend fun fireNow(itemId: String) {}
    }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, CookedDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userPrefsRepo = UserPreferencesRepository(context)
        alarmScheduler = FakeAlarmScheduler()
        backupManager = BackupManager(
            context = context,
            database = database,
            alarmScheduler = alarmScheduler,
            userPreferencesRepository = userPrefsRepo
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun createBackupJson_exportsExistingItemsAndPreferences() = runTest {
        val now = Instant.parse("2026-11-08T12:00:00Z")
        val item = Item(
            id = "test-item-1",
            kind = ItemKind.DEADLINE,
            title = "Final Project Report",
            dueAt = now.plus(2, ChronoUnit.DAYS),
            effortMinutes = 120,
            status = ItemStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
            checklist = listOf(
                ChecklistStep(
                    id = "step-1",
                    itemId = "test-item-1",
                    text = "Write introduction",
                    done = false,
                    order = 0,
                    updatedAt = now
                )
            )
        )

        database.itemDao().upsertItem(ItemEntity.fromDomain(item))
        database.checklistStepDao().upsertSteps(item.checklist.map { ChecklistStepEntity.fromDomain(it) })

        val jsonString = backupManager.createBackupJson()

        assertThat(jsonString).contains("Final Project Report")
        assertThat(jsonString).contains("test-item-1")
        assertThat(jsonString).contains("schemaVersion")

        val previewResult = backupManager.previewImport(jsonString)
        assertThat(previewResult.isSuccess).isTrue()
        val preview = previewResult.getOrThrow()
        assertThat(preview.totalItems).isEqualTo(1)
        assertThat(preview.deadlinesCount).isEqualTo(1)
        assertThat(preview.remindersCount).isEqualTo(0)
    }

    @Test
    fun previewImport_rejectsHigherSchemaVersion() {
        val futureJson = """
            {
                "schemaVersion": 99,
                "exportedAt": "2026-11-08T12:00:00Z",
                "items": []
            }
        """.trimIndent()

        val result = backupManager.previewImport(futureJson)
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("is newer than supported")
    }

    @Test
    fun previewImport_rejectsMalformedJson() {
        val malformedJson = "{ not valid json }"

        val result = backupManager.previewImport(malformedJson)
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Invalid Cooked backup file")
    }

    @Test
    fun executeImport_mergesItemsWithNewerUpdatedAtWinning() = runTest {
        val baseTime = Instant.parse("2026-11-08T10:00:00Z")
        val olderTime = baseTime.minus(1, ChronoUnit.HOURS)
        val newerTime = baseTime.plus(1, ChronoUnit.HOURS)

        // 1. Existing item in local DB: "Original Title", updatedAt = baseTime
        val existingItem = Item(
            id = "item-conflict",
            kind = ItemKind.REMINDER,
            title = "Original Title",
            dueAt = baseTime.plus(1, ChronoUnit.DAYS),
            status = ItemStatus.ACTIVE,
            createdAt = olderTime,
            updatedAt = baseTime
        )
        database.itemDao().upsertItem(ItemEntity.fromDomain(existingItem))

        // 2. Existing item that is newer in local DB: updatedAt = newerTime
        val localNewerItem = Item(
            id = "item-local-newer",
            kind = ItemKind.REMINDER,
            title = "Local Newer Title",
            dueAt = baseTime.plus(1, ChronoUnit.DAYS),
            status = ItemStatus.ACTIVE,
            createdAt = olderTime,
            updatedAt = newerTime
        )
        database.itemDao().upsertItem(ItemEntity.fromDomain(localNewerItem))

        // 3. Backup contains:
        // - "item-conflict" with updatedAt = newerTime -> SHOULD OVERWRITE
        // - "item-local-newer" with updatedAt = baseTime (older than local) -> SHOULD BE SKIPPED
        // - "item-brand-new" -> SHOULD INSERT
        val backupItems = listOf(
            existingItem.copy(title = "Updated from Backup", updatedAt = newerTime),
            localNewerItem.copy(title = "Stale Backup Title", updatedAt = baseTime),
            Item(
                id = "item-brand-new",
                kind = ItemKind.DEADLINE,
                title = "Brand New Task",
                dueAt = baseTime.plus(3, ChronoUnit.DAYS),
                status = ItemStatus.ACTIVE,
                createdAt = baseTime,
                updatedAt = baseTime
            )
        )

        val backupData = CookedBackupData(
            schemaVersion = 1,
            exportedAt = newerTime.toString(),
            items = backupItems
        )

        val importedCount = backupManager.executeImport(backupData)
        assertThat(importedCount).isEqualTo(2) // item-conflict and item-brand-new

        // Verify "item-conflict" was updated
        val retrievedConflict = database.itemDao().getItemWithChecklist("item-conflict")
        assertThat(retrievedConflict?.item?.title).isEqualTo("Updated from Backup")

        // Verify "item-local-newer" kept local newer version
        val retrievedLocalNewer = database.itemDao().getItemWithChecklist("item-local-newer")
        assertThat(retrievedLocalNewer?.item?.title).isEqualTo("Local Newer Title")

        // Verify alarms were rescheduled
        assertThat(alarmScheduler.rescheduleCount).isAtLeast(1)
    }

    @Test
    fun deleteAllData_wipesAllTablesAndReschedules() = runTest {
        val now = Instant.now()
        val item = Item(
            id = "to-be-deleted",
            kind = ItemKind.REMINDER,
            title = "Wipe me",
            dueAt = now.plus(1, ChronoUnit.DAYS),
            status = ItemStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )
        database.itemDao().upsertItem(ItemEntity.fromDomain(item))

        assertThat(database.itemDao().getAllNonDeletedItems()).isNotEmpty()

        backupManager.deleteAllData()

        assertThat(database.itemDao().getAllNonDeletedItems()).isEmpty()
        assertThat(alarmScheduler.rescheduleCount).isAtLeast(1)
    }
}
