package com.cooked.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cooked.app.data.dao.AnalyticsDao
import com.cooked.app.data.dao.ChecklistStepDao
import com.cooked.app.data.dao.ItemDao
import com.cooked.app.data.dao.ItemEventDao
import com.cooked.app.data.dao.ReminderDao
import com.cooked.app.data.dao.WeeklySnapshotDao
import com.cooked.app.data.entity.ChecklistStepEntity
import com.cooked.app.data.entity.ItemEntity
import com.cooked.app.data.entity.ItemEventEntity
import com.cooked.app.data.entity.ReminderEntity
import com.cooked.app.data.entity.WeeklySnapshotEntity

@Database(
    entities = [
        ItemEntity::class,
        ChecklistStepEntity::class,
        ItemEventEntity::class,
        ReminderEntity::class, // Preserved for Step 1 backward compatibility
        WeeklySnapshotEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CookedDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun checklistStepDao(): ChecklistStepDao
    abstract fun itemEventDao(): ItemEventDao
    abstract fun analyticsDao(): AnalyticsDao
    abstract fun reminderDao(): ReminderDao
    abstract fun weeklySnapshotDao(): WeeklySnapshotDao
}
