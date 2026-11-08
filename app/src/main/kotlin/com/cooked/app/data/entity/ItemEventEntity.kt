package com.cooked.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cooked.app.domain.model.ItemEvent
import com.cooked.app.domain.model.ItemEventType
import java.time.Instant

@Entity(
    tableName = "item_events",
    indices = [
        Index(value = ["itemId", "at"]),
        Index(value = ["type", "at"])
    ]
)
data class ItemEventEntity(
    @PrimaryKey
    val id: String,
    val itemId: String,
    val type: ItemEventType,
    val at: Instant,
    val meta: Map<String, String>
) {
    fun toDomain(): ItemEvent = ItemEvent(
        id = id,
        itemId = itemId,
        type = type,
        at = at,
        meta = meta
    )

    companion object {
        fun fromDomain(event: ItemEvent): ItemEventEntity = ItemEventEntity(
            id = event.id,
            itemId = event.itemId,
            type = event.type,
            at = event.at,
            meta = event.meta
        )
    }
}

