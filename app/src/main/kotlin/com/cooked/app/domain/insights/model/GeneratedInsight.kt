package com.cooked.app.domain.insights.model

data class GeneratedInsight(
    val id: String,
    val title: String,
    val body: String,
    val highlightStat: String,
    val sparklinePoints: List<Float> = emptyList(),
    val numbers: Map<String, Any> = emptyMap()
)
