package com.cooked.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "weekly_snapshots")
data class WeeklySnapshotEntity(
    @PrimaryKey
    val weekStartEpochDay: Long,
    val cookedScore: Int,
    val recordedAt: Instant
)
