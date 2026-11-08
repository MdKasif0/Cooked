package com.cooked.app.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Builds a real v1 database from the exported schema (app/schemas/.../1.json), then opens it
 * with the production Room setup. Room runs the migrations and validates the result against
 * the current entities, failing loudly if a migration is missing or wrong.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val dbName = "migration-test.db"

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    private fun schemaFile(version: Int): File {
        val relative = "schemas/com.cooked.app.data.db.CookedDatabase/$version.json"
        return listOf(File(relative), File("app/$relative")).first { it.exists() }
    }

    private fun createDatabaseFromSchema(version: Int): SQLiteDatabase {
        val schema = Json.parseToJsonElement(schemaFile(version).readText())
            .jsonObject.getValue("database").jsonObject
        val file = context.getDatabasePath(dbName).apply { parentFile?.mkdirs() }
        val db = SQLiteDatabase.openOrCreateDatabase(file, null)
        for (entity in schema.getValue("entities").jsonArray) {
            val obj = entity.jsonObject
            val table = obj.getValue("tableName").jsonPrimitive.content
            db.execSQL(obj.getValue("createSql").jsonPrimitive.content.replace("\${TABLE_NAME}", table))
            obj["indices"]?.jsonArray?.forEach { index ->
                db.execSQL(index.jsonObject.getValue("createSql").jsonPrimitive.content.replace("\${TABLE_NAME}", table))
            }
        }
        schema.getValue("setupQueries").jsonArray.forEach { db.execSQL(it.jsonPrimitive.content) }
        db.version = version
        return db
    }

    @Test
    fun migrate1To2_keepsExistingItemsAndAddsSnapshots() = runTest {
        createDatabaseFromSchema(1).apply {
            execSQL(
                """
                INSERT INTO items (id, kind, title, note, emoji, dueAt, allDay, timeZoneId, recurrence,
                    nag, critical, status, snoozedUntil, completedAt, createdAt, updatedAt, deletedAt,
                    startBy, effortMinutes, progress)
                VALUES ('item-1', 'REMINDER', 'Pay rent', NULL, NULL, 1000, 0, 'UTC', NULL,
                    '{"intervalMinutes":10}', 0, 'ACTIVE', NULL, NULL, 1, 1, NULL, NULL, NULL, 0)
                """.trimIndent()
            )
            close()
        }

        val db = Room.databaseBuilder(context, CookedDatabase::class.java, dbName)
            .addMigrations(*Migrations.ALL_MIGRATIONS)
            .allowMainThreadQueries()
            .build()
        try {
            val item = db.itemDao().getItemWithChecklist("item-1")
            assertThat(item).isNotNull()
            assertThat(item!!.item.title).isEqualTo("Pay rent")
            // New table from v2 exists and is usable
            assertThat(db.weeklySnapshotDao().getAllSnapshots()).isEmpty()
            assertThat(db.openHelper.readableDatabase.version).isEqualTo(2)
        } finally {
            db.close()
        }
    }
}
