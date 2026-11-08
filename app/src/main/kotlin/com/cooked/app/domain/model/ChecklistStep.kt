package com.cooked.app.domain.model

import com.cooked.app.core.time.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class ChecklistStep(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val text: String,
    val done: Boolean = false,
    val order: Int = 0,
    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant = Instant.now()
)

