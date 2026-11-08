package com.cooked.app.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.FontFeatureSettingsTabular
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import kotlin.math.abs

/**
 * Formats epoch millis into a compact countdown string:
 * e.g., "2d 4h", "38m", "overdue 12m".
 */
fun formatCountdown(targetEpochMillis: Long, currentEpochMillis: Long = System.currentTimeMillis()): String {
    val diffMillis = targetEpochMillis - currentEpochMillis
    val isOverdue = diffMillis < 0
    val duration = Duration.ofMillis(abs(diffMillis))

    val days = duration.toDays()
    val hours = duration.toHours() % 24
    val minutes = duration.toMinutes() % 60

    val timeString = when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
    }

    return if (isOverdue) "overdue $timeString" else timeString
}

/**
 * Tabular countdown text component:
 * - Uses tnum (tabular numbers) to eliminate jitter
 * - Automatically updates every 30-60 seconds
 */
@Composable
fun CountdownText(
    targetInstant: Instant,
    modifier: Modifier = Modifier,
    style: TextStyle = CookedTheme.typography.titleMedium,
    color: Color = CookedTheme.colors.onSurface
) {
    CountdownText(
        targetEpochMillis = targetInstant.toEpochMilli(),
        modifier = modifier,
        style = style,
        color = color
    )
}

@Composable
fun CountdownText(
    targetEpochMillis: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = CookedTheme.typography.titleMedium,
    color: Color = CookedTheme.colors.onSurface
) {
    var formattedText by remember(targetEpochMillis) {
        mutableStateOf(formatCountdown(targetEpochMillis))
    }

    LaunchedEffect(targetEpochMillis) {
        while (true) {
            formattedText = formatCountdown(targetEpochMillis)
            // Recompute every 30 seconds to keep countdown accurate
            delay(30_000L)
        }
    }

    Text(
        text = formattedText,
        modifier = modifier,
        style = style.copy(fontFeatureSettings = FontFeatureSettingsTabular),
        color = color
    )
}

/**
 * Static tabular text wrapper for already-formatted countdown or number strings.
 */
@Composable
fun TabularText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = CookedTheme.typography.titleMedium,
    color: Color = CookedTheme.colors.onSurface
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(fontFeatureSettings = FontFeatureSettingsTabular),
        color = color
    )
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun CountdownTextPreview() {
    val now = System.currentTimeMillis()
    CookedTheme {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            CountdownText(
                targetEpochMillis = now + 180_000_000L, // ~2d 2h
                style = CookedTheme.typography.headlineMedium
            )
            CountdownText(
                targetEpochMillis = now + 2_280_000L, // 38m
                style = CookedTheme.typography.titleLarge
            )
            CountdownText(
                targetEpochMillis = now - 720_000L, // overdue 12m
                style = CookedTheme.typography.titleMedium,
                color = CookedTheme.colors.cooked.textColor
            )
        }
    }
}
