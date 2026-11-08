package com.cooked.app.domain.insights.model

enum class InsightRange(
    val label: String,
    val days: Int,
    val isPro: Boolean
) {
    WEEK_7D("7d", 7, isPro = false),
    MONTH_30D("30d", 30, isPro = true),
    MONTHS_90D("90d", 90, isPro = true),
    ALL("All", 365, isPro = true)
}

