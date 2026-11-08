package com.cooked.app.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.cooked.app.data.entity.ChecklistStepEntity

@Dao
interface ChecklistStepDao {

    @Upsert
    suspend fun upsertStep(step: ChecklistStepEntity)

    @Upsert
    suspend fun upsertSteps(steps: List<ChecklistStepEntity>)

    @Query("DELETE FROM checklist_steps WHERE itemId = :itemId")
    suspend fun deleteStepsFor(itemId: String)

    @Query("DELETE FROM checklist_steps WHERE id = :id")
    suspend fun deleteStep(id: String)

    @Query("DELETE FROM checklist_steps")
    suspend fun clearAll()
}

