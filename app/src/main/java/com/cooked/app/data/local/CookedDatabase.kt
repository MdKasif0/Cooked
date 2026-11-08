package com.cooked.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cooked.app.data.local.dao.ReminderDao
import com.cooked.app.data.local.entity.ReminderEntity

@Database(
    entities = [ReminderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CookedDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
}
