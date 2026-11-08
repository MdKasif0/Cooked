package com.cooked.app.domain.model

import com.cooked.app.core.time.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

enum class ItemEventType {
    CREATED,
    EDITED,
    FIRED,
    NAGGED,
    SNOOZED,
    DONE,
    UNDONE,
    RESCHEDULED,
    MISSED,
    STARTED
}

@Serializable
data class ItemEvent(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val type: ItemEventType,
    @Serializable(with = InstantSerializer::class)
    val at: Instant = Instant.now(),
    val meta: Map<String, String> = emptyMap()
)

