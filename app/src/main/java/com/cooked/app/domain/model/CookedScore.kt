package com.cooked.app.domain.model

enum class CookedLevel {
    CHILLING,     // 0 - 25: Raw & Chilling
    SIMMERING,    // 26 - 55: Simmering Gently
    COOKED,       // 56 - 80: Properly Cooked
    DEEP_FRIED    // 81 - 100: Deep Fried & Crispy
}

data class CookedScore(
    val score: Int,
    val level: CookedLevel,
    val totalRequiredHours: Double,
    val totalAvailableHours: Double,
    val headline: String,
    val advice: String
)

