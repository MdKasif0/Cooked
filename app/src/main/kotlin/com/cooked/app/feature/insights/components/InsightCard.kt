package com.cooked.app.feature.insights.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.domain.insights.model.GeneratedInsight

@Composable
fun InsightCard(
    insight: GeneratedInsight,
    modifier: Modifier = Modifier
) {
    val primaryColor = CookedTheme.colors.primary
    val onSurface = CookedTheme.colors.onSurface
    val onSurfaceVariant = CookedTheme.colors.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CookedTheme.colors.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${insight.title}: ${insight.body} (${insight.highlightStat})"
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = insight.title,
                        style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CookedTheme.colors.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = insight.highlightStat,
                            style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = insight.body,
                    style = CookedTheme.typography.bodyMedium,
                    color = onSurfaceVariant
                )
            }

            // Tiny Sparkline (if points exist)
            if (insight.sparklinePoints.size >= 2) {
                Spacer(modifier = Modifier.width(12.dp))
                MiniSparkline(
                    points = insight.sparklinePoints,
                    lineColor = primaryColor,
                    modifier = Modifier.size(width = 54.dp, height = 32.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniSparkline(
    points: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val minVal = points.minOrNull() ?: 0f
        val maxVal = (points.maxOrNull() ?: 1f).coerceAtLeast(minVal + 0.001f)

        val stepX = w / (points.size - 1)
        val path = Path()

        points.forEachIndexed { i, p ->
            val normY = (p - minVal) / (maxVal - minVal)
            val x = i * stepX
            val y = h - (normY * (h - 6.dp.toPx()) + 3.dp.toPx())

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

