package com.cooked.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cooked.app.data.entity.WeeklySnapshotEntity

@Dao
interface WeeklySnapshotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: WeeklySnapshotEntity)

    @Query("SELECT * FROM weekly_snapshots ORDER BY weekStartEpochDay DESC LIMIT :limit")
    suspend fun getRecentSnapshots(limit: Int = 12): List<WeeklySnapshotEntity>

    @Query("SELECT * FROM weekly_snapshots ORDER BY weekStartEpochDay ASC")
    suspend fun getAllSnapshots(): List<WeeklySnapshotEntity>

    @Query("SELECT * FROM weekly_snapshots WHERE weekStartEpochDay = :weekStart")
    suspend fun getSnapshot(weekStart: Long): WeeklySnapshotEntity?
}

