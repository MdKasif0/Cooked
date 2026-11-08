package com.cooked.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cooked.app.domain.model.ChecklistStep
import java.time.Instant

@Entity(
    tableName = "checklist_steps",
    indices = [
        Index(value = ["itemId"]),
        Index(value = ["itemId", "orderIndex"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ChecklistStepEntity(
    @PrimaryKey
    val id: String,
    val itemId: String,
    val text: String,
    val done: Boolean,
    val orderIndex: Int,
    val updatedAt: Instant
) {
    fun toDomain(): ChecklistStep = ChecklistStep(
        id = id,
        itemId = itemId,
        text = text,
        done = done,
        order = orderIndex,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(step: ChecklistStep): ChecklistStepEntity = ChecklistStepEntity(
            id = step.id,
            itemId = step.itemId,
            text = step.text,
            done = step.done,
            orderIndex = step.order,
            updatedAt = step.updatedAt
        )
    }
}

