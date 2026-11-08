package com.cooked.app.data.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.cooked.app.domain.model.Item

data class ItemWithChecklist(
    @Embedded
    val item: ItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "itemId"
    )
    val checklist: List<ChecklistStepEntity> = emptyList()
) {
    fun toDomain(): Item {
        val sortedSteps = checklist.sortedBy { it.orderIndex }.map { it.toDomain() }
        return item.toDomain(sortedSteps)
    }
}

