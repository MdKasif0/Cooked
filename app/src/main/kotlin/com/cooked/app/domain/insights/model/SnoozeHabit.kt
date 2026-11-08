package com.cooked.app.domain.insights.model

data class SnoozeHabit(
    val hourlyBuckets: List<Int> = List(24) { 0 },
    val peakHour: Int? = null,
    val peakCount: Int = 0,
    val mostSnoozedItemTitle: String? = null,
    val mostSnoozedCount: Int = 0
)

