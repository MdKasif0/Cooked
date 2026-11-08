package com.cooked.app.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.cooked.app.`data`.dao.ReminderDao
import com.cooked.app.`data`.dao.ReminderDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CookedDatabase_Impl : CookedDatabase() {
  private val _reminderDao: Lazy<ReminderDao> = lazy {
    ReminderDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "0484cbe01282653bbc8689dd5c31002a", "1825a2beb1ec3d0796ce1bdfd5fbf879") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `reminders` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `note` TEXT, `dueInstant` INTEGER, `nagIntervalMinutes` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, `completedAt` INTEGER, `isNaggingActive` INTEGER NOT NULL, `nagCount` INTEGER NOT NULL, `snoozedUntil` INTEGER, `isDeadlineMode` INTEGER NOT NULL, `deadlineStartInstant` INTEGER, `estimatedEffortMinutes` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '0484cbe01282653bbc8689dd5c31002a')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `reminders`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsReminders: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsReminders.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("note", TableInfo.Column("note", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("dueInstant", TableInfo.Column("dueInstant", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("nagIntervalMinutes", TableInfo.Column("nagIntervalMinutes",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("isCompleted", TableInfo.Column("isCompleted", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("completedAt", TableInfo.Column("completedAt", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("isNaggingActive", TableInfo.Column("isNaggingActive", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("nagCount", TableInfo.Column("nagCount", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("snoozedUntil", TableInfo.Column("snoozedUntil", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("isDeadlineMode", TableInfo.Column("isDeadlineMode", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("deadlineStartInstant", TableInfo.Column("deadlineStartInstant",
            "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("estimatedEffortMinutes", TableInfo.Column("estimatedEffortMinutes",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysReminders: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesReminders: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoReminders: TableInfo = TableInfo("reminders", _columnsReminders,
            _foreignKeysReminders, _indicesReminders)
        val _existingReminders: TableInfo = read(connection, "reminders")
        if (!_infoReminders.equals(_existingReminders)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |reminders(com.cooked.app.data.entity.ReminderEntity).
              | Expected:
              |""".trimMargin() + _infoReminders + """
              |
              | Found:
              |""".trimMargin() + _existingReminders)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "reminders")
  }

  public override fun clearAllTables() {
    super.performClear(false, "reminders")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(ReminderDao::class, ReminderDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun reminderDao(): ReminderDao = _reminderDao.value
}
