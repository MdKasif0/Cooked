package com.cooked.app.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.cooked.app.`data`.local.entity.ReminderEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ReminderDao_Impl(
  __db: RoomDatabase,
) : ReminderDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfReminderEntity: EntityInsertAdapter<ReminderEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfReminderEntity = object : EntityInsertAdapter<ReminderEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `reminders` (`id`,`title`,`note`,`dueInstant`,`nagIntervalMinutes`,`isCompleted`,`completedAt`,`isNaggingActive`,`nagCount`,`snoozedUntil`,`isDeadlineMode`,`deadlineStartInstant`,`estimatedEffortMinutes`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ReminderEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpNote)
        }
        val _tmpDueInstant: Long? = entity.dueInstant
        if (_tmpDueInstant == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpDueInstant)
        }
        statement.bindLong(5, entity.nagIntervalMinutes.toLong())
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        val _tmpCompletedAt: Long? = entity.completedAt
        if (_tmpCompletedAt == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpCompletedAt)
        }
        val _tmp_1: Int = if (entity.isNaggingActive) 1 else 0
        statement.bindLong(8, _tmp_1.toLong())
        statement.bindLong(9, entity.nagCount.toLong())
        val _tmpSnoozedUntil: Long? = entity.snoozedUntil
        if (_tmpSnoozedUntil == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpSnoozedUntil)
        }
        val _tmp_2: Int = if (entity.isDeadlineMode) 1 else 0
        statement.bindLong(11, _tmp_2.toLong())
        val _tmpDeadlineStartInstant: Long? = entity.deadlineStartInstant
        if (_tmpDeadlineStartInstant == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpDeadlineStartInstant)
        }
        statement.bindLong(13, entity.estimatedEffortMinutes.toLong())
        statement.bindLong(14, entity.createdAt)
        statement.bindLong(15, entity.updatedAt)
      }
    }
  }

  public override suspend fun insertOrUpdate(entity: ReminderEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfReminderEntity.insert(_connection, entity)
  }

  public override fun getAllReminders(): Flow<List<ReminderEntity>> {
    val _sql: String =
        "SELECT * FROM reminders ORDER BY isCompleted ASC, dueInstant ASC, createdAt DESC"
    return createFlow(__db, false, arrayOf("reminders")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfDueInstant: Int = getColumnIndexOrThrow(_stmt, "dueInstant")
        val _columnIndexOfNagIntervalMinutes: Int = getColumnIndexOrThrow(_stmt,
            "nagIntervalMinutes")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfIsNaggingActive: Int = getColumnIndexOrThrow(_stmt, "isNaggingActive")
        val _columnIndexOfNagCount: Int = getColumnIndexOrThrow(_stmt, "nagCount")
        val _columnIndexOfSnoozedUntil: Int = getColumnIndexOrThrow(_stmt, "snoozedUntil")
        val _columnIndexOfIsDeadlineMode: Int = getColumnIndexOrThrow(_stmt, "isDeadlineMode")
        val _columnIndexOfDeadlineStartInstant: Int = getColumnIndexOrThrow(_stmt,
            "deadlineStartInstant")
        val _columnIndexOfEstimatedEffortMinutes: Int = getColumnIndexOrThrow(_stmt,
            "estimatedEffortMinutes")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<ReminderEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ReminderEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpDueInstant: Long?
          if (_stmt.isNull(_columnIndexOfDueInstant)) {
            _tmpDueInstant = null
          } else {
            _tmpDueInstant = _stmt.getLong(_columnIndexOfDueInstant)
          }
          val _tmpNagIntervalMinutes: Int
          _tmpNagIntervalMinutes = _stmt.getLong(_columnIndexOfNagIntervalMinutes).toInt()
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpIsNaggingActive: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsNaggingActive).toInt()
          _tmpIsNaggingActive = _tmp_1 != 0
          val _tmpNagCount: Int
          _tmpNagCount = _stmt.getLong(_columnIndexOfNagCount).toInt()
          val _tmpSnoozedUntil: Long?
          if (_stmt.isNull(_columnIndexOfSnoozedUntil)) {
            _tmpSnoozedUntil = null
          } else {
            _tmpSnoozedUntil = _stmt.getLong(_columnIndexOfSnoozedUntil)
          }
          val _tmpIsDeadlineMode: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsDeadlineMode).toInt()
          _tmpIsDeadlineMode = _tmp_2 != 0
          val _tmpDeadlineStartInstant: Long?
          if (_stmt.isNull(_columnIndexOfDeadlineStartInstant)) {
            _tmpDeadlineStartInstant = null
          } else {
            _tmpDeadlineStartInstant = _stmt.getLong(_columnIndexOfDeadlineStartInstant)
          }
          val _tmpEstimatedEffortMinutes: Int
          _tmpEstimatedEffortMinutes = _stmt.getLong(_columnIndexOfEstimatedEffortMinutes).toInt()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item =
              ReminderEntity(_tmpId,_tmpTitle,_tmpNote,_tmpDueInstant,_tmpNagIntervalMinutes,_tmpIsCompleted,_tmpCompletedAt,_tmpIsNaggingActive,_tmpNagCount,_tmpSnoozedUntil,_tmpIsDeadlineMode,_tmpDeadlineStartInstant,_tmpEstimatedEffortMinutes,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getActiveReminders(): Flow<List<ReminderEntity>> {
    val _sql: String =
        "SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY dueInstant ASC, createdAt DESC"
    return createFlow(__db, false, arrayOf("reminders")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfDueInstant: Int = getColumnIndexOrThrow(_stmt, "dueInstant")
        val _columnIndexOfNagIntervalMinutes: Int = getColumnIndexOrThrow(_stmt,
            "nagIntervalMinutes")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfIsNaggingActive: Int = getColumnIndexOrThrow(_stmt, "isNaggingActive")
        val _columnIndexOfNagCount: Int = getColumnIndexOrThrow(_stmt, "nagCount")
        val _columnIndexOfSnoozedUntil: Int = getColumnIndexOrThrow(_stmt, "snoozedUntil")
        val _columnIndexOfIsDeadlineMode: Int = getColumnIndexOrThrow(_stmt, "isDeadlineMode")
        val _columnIndexOfDeadlineStartInstant: Int = getColumnIndexOrThrow(_stmt,
            "deadlineStartInstant")
        val _columnIndexOfEstimatedEffortMinutes: Int = getColumnIndexOrThrow(_stmt,
            "estimatedEffortMinutes")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<ReminderEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ReminderEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpDueInstant: Long?
          if (_stmt.isNull(_columnIndexOfDueInstant)) {
            _tmpDueInstant = null
          } else {
            _tmpDueInstant = _stmt.getLong(_columnIndexOfDueInstant)
          }
          val _tmpNagIntervalMinutes: Int
          _tmpNagIntervalMinutes = _stmt.getLong(_columnIndexOfNagIntervalMinutes).toInt()
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpIsNaggingActive: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsNaggingActive).toInt()
          _tmpIsNaggingActive = _tmp_1 != 0
          val _tmpNagCount: Int
          _tmpNagCount = _stmt.getLong(_columnIndexOfNagCount).toInt()
          val _tmpSnoozedUntil: Long?
          if (_stmt.isNull(_columnIndexOfSnoozedUntil)) {
            _tmpSnoozedUntil = null
          } else {
            _tmpSnoozedUntil = _stmt.getLong(_columnIndexOfSnoozedUntil)
          }
          val _tmpIsDeadlineMode: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsDeadlineMode).toInt()
          _tmpIsDeadlineMode = _tmp_2 != 0
          val _tmpDeadlineStartInstant: Long?
          if (_stmt.isNull(_columnIndexOfDeadlineStartInstant)) {
            _tmpDeadlineStartInstant = null
          } else {
            _tmpDeadlineStartInstant = _stmt.getLong(_columnIndexOfDeadlineStartInstant)
          }
          val _tmpEstimatedEffortMinutes: Int
          _tmpEstimatedEffortMinutes = _stmt.getLong(_columnIndexOfEstimatedEffortMinutes).toInt()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item =
              ReminderEntity(_tmpId,_tmpTitle,_tmpNote,_tmpDueInstant,_tmpNagIntervalMinutes,_tmpIsCompleted,_tmpCompletedAt,_tmpIsNaggingActive,_tmpNagCount,_tmpSnoozedUntil,_tmpIsDeadlineMode,_tmpDeadlineStartInstant,_tmpEstimatedEffortMinutes,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getCompletedReminders(): Flow<List<ReminderEntity>> {
    val _sql: String = "SELECT * FROM reminders WHERE isCompleted = 1 ORDER BY completedAt DESC"
    return createFlow(__db, false, arrayOf("reminders")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfDueInstant: Int = getColumnIndexOrThrow(_stmt, "dueInstant")
        val _columnIndexOfNagIntervalMinutes: Int = getColumnIndexOrThrow(_stmt,
            "nagIntervalMinutes")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfIsNaggingActive: Int = getColumnIndexOrThrow(_stmt, "isNaggingActive")
        val _columnIndexOfNagCount: Int = getColumnIndexOrThrow(_stmt, "nagCount")
        val _columnIndexOfSnoozedUntil: Int = getColumnIndexOrThrow(_stmt, "snoozedUntil")
        val _columnIndexOfIsDeadlineMode: Int = getColumnIndexOrThrow(_stmt, "isDeadlineMode")
        val _columnIndexOfDeadlineStartInstant: Int = getColumnIndexOrThrow(_stmt,
            "deadlineStartInstant")
        val _columnIndexOfEstimatedEffortMinutes: Int = getColumnIndexOrThrow(_stmt,
            "estimatedEffortMinutes")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<ReminderEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ReminderEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpDueInstant: Long?
          if (_stmt.isNull(_columnIndexOfDueInstant)) {
            _tmpDueInstant = null
          } else {
            _tmpDueInstant = _stmt.getLong(_columnIndexOfDueInstant)
          }
          val _tmpNagIntervalMinutes: Int
          _tmpNagIntervalMinutes = _stmt.getLong(_columnIndexOfNagIntervalMinutes).toInt()
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpIsNaggingActive: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsNaggingActive).toInt()
          _tmpIsNaggingActive = _tmp_1 != 0
          val _tmpNagCount: Int
          _tmpNagCount = _stmt.getLong(_columnIndexOfNagCount).toInt()
          val _tmpSnoozedUntil: Long?
          if (_stmt.isNull(_columnIndexOfSnoozedUntil)) {
            _tmpSnoozedUntil = null
          } else {
            _tmpSnoozedUntil = _stmt.getLong(_columnIndexOfSnoozedUntil)
          }
          val _tmpIsDeadlineMode: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsDeadlineMode).toInt()
          _tmpIsDeadlineMode = _tmp_2 != 0
          val _tmpDeadlineStartInstant: Long?
          if (_stmt.isNull(_columnIndexOfDeadlineStartInstant)) {
            _tmpDeadlineStartInstant = null
          } else {
            _tmpDeadlineStartInstant = _stmt.getLong(_columnIndexOfDeadlineStartInstant)
          }
          val _tmpEstimatedEffortMinutes: Int
          _tmpEstimatedEffortMinutes = _stmt.getLong(_columnIndexOfEstimatedEffortMinutes).toInt()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item =
              ReminderEntity(_tmpId,_tmpTitle,_tmpNote,_tmpDueInstant,_tmpNagIntervalMinutes,_tmpIsCompleted,_tmpCompletedAt,_tmpIsNaggingActive,_tmpNagCount,_tmpSnoozedUntil,_tmpIsDeadlineMode,_tmpDeadlineStartInstant,_tmpEstimatedEffortMinutes,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getActiveDeadlines(): Flow<List<ReminderEntity>> {
    val _sql: String =
        "SELECT * FROM reminders WHERE isCompleted = 0 AND isDeadlineMode = 1 ORDER BY dueInstant ASC"
    return createFlow(__db, false, arrayOf("reminders")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfDueInstant: Int = getColumnIndexOrThrow(_stmt, "dueInstant")
        val _columnIndexOfNagIntervalMinutes: Int = getColumnIndexOrThrow(_stmt,
            "nagIntervalMinutes")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfIsNaggingActive: Int = getColumnIndexOrThrow(_stmt, "isNaggingActive")
        val _columnIndexOfNagCount: Int = getColumnIndexOrThrow(_stmt, "nagCount")
        val _columnIndexOfSnoozedUntil: Int = getColumnIndexOrThrow(_stmt, "snoozedUntil")
        val _columnIndexOfIsDeadlineMode: Int = getColumnIndexOrThrow(_stmt, "isDeadlineMode")
        val _columnIndexOfDeadlineStartInstant: Int = getColumnIndexOrThrow(_stmt,
            "deadlineStartInstant")
        val _columnIndexOfEstimatedEffortMinutes: Int = getColumnIndexOrThrow(_stmt,
            "estimatedEffortMinutes")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<ReminderEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ReminderEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpDueInstant: Long?
          if (_stmt.isNull(_columnIndexOfDueInstant)) {
            _tmpDueInstant = null
          } else {
            _tmpDueInstant = _stmt.getLong(_columnIndexOfDueInstant)
          }
          val _tmpNagIntervalMinutes: Int
          _tmpNagIntervalMinutes = _stmt.getLong(_columnIndexOfNagIntervalMinutes).toInt()
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpIsNaggingActive: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsNaggingActive).toInt()
          _tmpIsNaggingActive = _tmp_1 != 0
          val _tmpNagCount: Int
          _tmpNagCount = _stmt.getLong(_columnIndexOfNagCount).toInt()
          val _tmpSnoozedUntil: Long?
          if (_stmt.isNull(_columnIndexOfSnoozedUntil)) {
            _tmpSnoozedUntil = null
          } else {
            _tmpSnoozedUntil = _stmt.getLong(_columnIndexOfSnoozedUntil)
          }
          val _tmpIsDeadlineMode: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsDeadlineMode).toInt()
          _tmpIsDeadlineMode = _tmp_2 != 0
          val _tmpDeadlineStartInstant: Long?
          if (_stmt.isNull(_columnIndexOfDeadlineStartInstant)) {
            _tmpDeadlineStartInstant = null
          } else {
            _tmpDeadlineStartInstant = _stmt.getLong(_columnIndexOfDeadlineStartInstant)
          }
          val _tmpEstimatedEffortMinutes: Int
          _tmpEstimatedEffortMinutes = _stmt.getLong(_columnIndexOfEstimatedEffortMinutes).toInt()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item =
              ReminderEntity(_tmpId,_tmpTitle,_tmpNote,_tmpDueInstant,_tmpNagIntervalMinutes,_tmpIsCompleted,_tmpCompletedAt,_tmpIsNaggingActive,_tmpNagCount,_tmpSnoozedUntil,_tmpIsDeadlineMode,_tmpDeadlineStartInstant,_tmpEstimatedEffortMinutes,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getReminderById(id: String): ReminderEntity? {
    val _sql: String = "SELECT * FROM reminders WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfDueInstant: Int = getColumnIndexOrThrow(_stmt, "dueInstant")
        val _columnIndexOfNagIntervalMinutes: Int = getColumnIndexOrThrow(_stmt,
            "nagIntervalMinutes")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfIsNaggingActive: Int = getColumnIndexOrThrow(_stmt, "isNaggingActive")
        val _columnIndexOfNagCount: Int = getColumnIndexOrThrow(_stmt, "nagCount")
        val _columnIndexOfSnoozedUntil: Int = getColumnIndexOrThrow(_stmt, "snoozedUntil")
        val _columnIndexOfIsDeadlineMode: Int = getColumnIndexOrThrow(_stmt, "isDeadlineMode")
        val _columnIndexOfDeadlineStartInstant: Int = getColumnIndexOrThrow(_stmt,
            "deadlineStartInstant")
        val _columnIndexOfEstimatedEffortMinutes: Int = getColumnIndexOrThrow(_stmt,
            "estimatedEffortMinutes")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: ReminderEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpDueInstant: Long?
          if (_stmt.isNull(_columnIndexOfDueInstant)) {
            _tmpDueInstant = null
          } else {
            _tmpDueInstant = _stmt.getLong(_columnIndexOfDueInstant)
          }
          val _tmpNagIntervalMinutes: Int
          _tmpNagIntervalMinutes = _stmt.getLong(_columnIndexOfNagIntervalMinutes).toInt()
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpIsNaggingActive: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsNaggingActive).toInt()
          _tmpIsNaggingActive = _tmp_1 != 0
          val _tmpNagCount: Int
          _tmpNagCount = _stmt.getLong(_columnIndexOfNagCount).toInt()
          val _tmpSnoozedUntil: Long?
          if (_stmt.isNull(_columnIndexOfSnoozedUntil)) {
            _tmpSnoozedUntil = null
          } else {
            _tmpSnoozedUntil = _stmt.getLong(_columnIndexOfSnoozedUntil)
          }
          val _tmpIsDeadlineMode: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsDeadlineMode).toInt()
          _tmpIsDeadlineMode = _tmp_2 != 0
          val _tmpDeadlineStartInstant: Long?
          if (_stmt.isNull(_columnIndexOfDeadlineStartInstant)) {
            _tmpDeadlineStartInstant = null
          } else {
            _tmpDeadlineStartInstant = _stmt.getLong(_columnIndexOfDeadlineStartInstant)
          }
          val _tmpEstimatedEffortMinutes: Int
          _tmpEstimatedEffortMinutes = _stmt.getLong(_columnIndexOfEstimatedEffortMinutes).toInt()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _result =
              ReminderEntity(_tmpId,_tmpTitle,_tmpNote,_tmpDueInstant,_tmpNagIntervalMinutes,_tmpIsCompleted,_tmpCompletedAt,_tmpIsNaggingActive,_tmpNagCount,_tmpSnoozedUntil,_tmpIsDeadlineMode,_tmpDeadlineStartInstant,_tmpEstimatedEffortMinutes,_tmpCreatedAt,_tmpUpdatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markCompleted(id: String, completedAt: Long) {
    val _sql: String =
        "UPDATE reminders SET isCompleted = 1, completedAt = ?, isNaggingActive = 0, updatedAt = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, completedAt)
        _argIndex = 2
        _stmt.bindLong(_argIndex, completedAt)
        _argIndex = 3
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setSnooze(
    id: String,
    snoozeUntil: Long,
    updatedAt: Long,
  ) {
    val _sql: String = "UPDATE reminders SET snoozedUntil = ?, updatedAt = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, snoozeUntil)
        _argIndex = 2
        _stmt.bindLong(_argIndex, updatedAt)
        _argIndex = 3
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setNagging(
    id: String,
    isNagging: Boolean,
    incrementNag: Boolean,
    updatedAt: Long,
  ) {
    val _sql: String =
        "UPDATE reminders SET isNaggingActive = ?, nagCount = CASE WHEN ? = 1 THEN nagCount + 1 ELSE nagCount END, updatedAt = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (isNagging) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        val _tmp_1: Int = if (incrementNag) 1 else 0
        _stmt.bindLong(_argIndex, _tmp_1.toLong())
        _argIndex = 3
        _stmt.bindLong(_argIndex, updatedAt)
        _argIndex = 4
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: String) {
    val _sql: String = "DELETE FROM reminders WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
