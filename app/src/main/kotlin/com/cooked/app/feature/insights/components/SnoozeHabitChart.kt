package com.cooked.app.feature.insights.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.domain.insights.model.SnoozeHabit

@Composable
fun SnoozeHabitChart(
    snoozeHabit: SnoozeHabit,
    modifier: Modifier = Modifier
) {
    val totalSnoozes = snoozeHabit.hourlyBuckets.sum()
    val peakHourText = if (snoozeHabit.peakHour != null) formatHour(snoozeHabit.peakHour) else "None"
    val talkBackDesc = "24-hour snooze frequency chart. Total $totalSnoozes snoozes. Peak snooze hour is $peakHourText."

    val primaryColor = CookedTheme.colors.primary
    val peakColor = CookedTheme.colors.heatFor(HeatLevel.WARM).color
    val emptyBarColor = CookedTheme.colors.surfaceContainerHigh
    val onSurface = CookedTheme.colors.onSurface
    val onSurfaceVariant = CookedTheme.colors.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CookedTheme.colors.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = talkBackDesc }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "When You Snooze",
                        style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = onSurface
                    )
                    Text(
                        text = "Hourly delay patterns across 24 hours",
                        style = CookedTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                }

                if (snoozeHabit.peakHour != null && snoozeHabit.peakCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = peakColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Peak: $peakHourText",
                            style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = peakColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 24-Hour Bar Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val w = size.width
                val h = size.height
                val barCount = 24
                val barGap = 2.dp.toPx()
                val barWidth = (w - (barCount - 1) * barGap) / barCount
                val maxCount = (snoozeHabit.hourlyBuckets.maxOrNull() ?: 1).coerceAtLeast(1)

                val cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())

                for (hour in 0 until barCount) {
                    val count = snoozeHabit.hourlyBuckets[hour]
                    val isPeak = snoozeHabit.peakHour == hour && count > 0

                    val barHeight = if (count > 0) {
                        (h * (count.toFloat() / maxCount.toFloat())).coerceAtLeast(8.dp.toPx())
                    } else {
                        4.dp.toPx()
                    }

                    val x = hour * (barWidth + barGap)
                    val y = h - barHeight

                    val barColor = when {
                        isPeak -> peakColor
                        count > 0 -> primaryColor.copy(alpha = 0.6f)
                        else -> emptyBarColor
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }

            // Hour labels (12 AM, 6 AM, 12 PM, 6 PM, 11 PM)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "12 AM", style = CookedTheme.typography.labelSmall, color = onSurfaceVariant, fontSize = 10.sp)
                Text(text = "6 AM", style = CookedTheme.typography.labelSmall, color = onSurfaceVariant, fontSize = 10.sp)
                Text(text = "12 PM", style = CookedTheme.typography.labelSmall, color = onSurfaceVariant, fontSize = 10.sp)
                Text(text = "6 PM", style = CookedTheme.typography.labelSmall, color = onSurfaceVariant, fontSize = 10.sp)
                Text(text = "11 PM", style = CookedTheme.typography.labelSmall, color = onSurfaceVariant, fontSize = 10.sp)
            }

            // Most snoozed item callout
            if (!snoozeHabit.mostSnoozedItemTitle.isNullOrBlank() && snoozeHabit.mostSnoozedCount > 0) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CookedTheme.colors.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Most snoozed: \"${snoozeHabit.mostSnoozedItemTitle}\"",
                            style = CookedTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = onSurface
                        )
                        Text(
                            text = "${snoozeHabit.mostSnoozedCount}x",
                            style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = primaryColor
                        )
                    }
                }
            }
        }
    }
}

private fun formatHour(hour: Int): String {
    return when {
        hour == 0 -> "12 AM"
        hour == 12 -> "12 PM"
        hour > 12 -> "${hour - 12} PM"
        else -> "$hour AM"
    }
}

