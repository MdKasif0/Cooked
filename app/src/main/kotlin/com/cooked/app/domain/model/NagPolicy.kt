package com.cooked.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NagPolicy(
    val intervalMinutes: Int = 10,
    val escalate: Boolean = false,
    val maxNags: Int? = null, // null means nag until done
    val respectQuietHours: Boolean = true
)

