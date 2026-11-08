package com.cooked.app.feature.insights.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun StatTilesRow(
    onTimeRate: Double,
    onTimeDelta: Double?,
    doneCount: Int,
    doneDelta: Int?,
    nagsPerWin: Double,
    nagsDelta: Double?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Tile 1: On-time %
        val onTimePct = (onTimeRate * 100).roundToInt()
        val onTimeDeltaPct = onTimeDelta?.let { (it * 100).roundToInt() }
        val onTimeAccessibility = if (onTimeDeltaPct != null) {
            val dir = if (onTimeDeltaPct >= 0) "up" else "down"
            "On-time rate $onTimePct percent, $dir ${abs(onTimeDeltaPct)} points vs previous period"
        } else {
            "On-time rate $onTimePct percent"
        }

        StatTile(
            title = "On-time",
            value = "$onTimePct%",
            delta = onTimeDeltaPct?.let { if (it >= 0) "+$it%" else "$it%" },
            isDeltaPositive = (onTimeDeltaPct ?: 0) >= 0,
            accessibilityText = onTimeAccessibility,
            modifier = Modifier.weight(1f)
        )

        // Tile 2: Done Count
        val doneAccessibility = if (doneDelta != null) {
            val dir = if (doneDelta >= 0) "up" else "down"
            "$doneCount items completed, $dir ${abs(doneDelta)} vs previous period"
        } else {
            "$doneCount items completed"
        }

        StatTile(
            title = "Done",
            value = "$doneCount",
            delta = doneDelta?.let { if (it >= 0) "+$it" else "$it" },
            isDeltaPositive = (doneDelta ?: 0) >= 0,
            accessibilityText = doneAccessibility,
            modifier = Modifier.weight(1f)
        )

        // Tile 3: Nags per win (lower nags is BETTER!)
        val nagsFormatted = String.format(Locale.US, "%.1f", nagsPerWin)
        val nagsDeltaFormatted = nagsDelta?.let { String.format(Locale.US, "%.1f", it) }
        val nagsAccessibility = if (nagsDelta != null) {
            val dir = if (nagsDelta <= 0) "down (improved)" else "up"
            "Average $nagsFormatted nags before done, $dir $nagsDeltaFormatted nags vs previous period"
        } else {
            "Average $nagsFormatted nags before done"
        }

        // For nags, delta < 0 is GOOD (positive improvement)
        StatTile(
            title = "Nags/win",
            value = nagsFormatted,
            delta = nagsDelta?.let { if (it > 0) "+$nagsDeltaFormatted" else nagsDeltaFormatted },
            isDeltaPositive = (nagsDelta ?: 0.0) <= 0.0,
            accessibilityText = nagsAccessibility,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatTile(
    title: String,
    value: String,
    delta: String?,
    isDeltaPositive: Boolean,
    accessibilityText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CookedTheme.colors.surfaceContainerLow,
        modifier = modifier.semantics {
            contentDescription = accessibilityText
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = CookedTheme.typography.labelSmall,
                color = CookedTheme.colors.onSurfaceVariant
            )

            Text(
                text = value,
                style = CookedTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace // Tabular numerals
                ),
                color = CookedTheme.colors.onSurface
            )

            if (delta != null) {
                val deltaColor = if (isDeltaPositive) {
                    CookedTheme.colors.heatFor(HeatLevel.CHILL).color
                } else {
                    CookedTheme.colors.heatFor(HeatLevel.COOKED).color
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = if (isDeltaPositive) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                        contentDescription = null,
                        tint = deltaColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = delta,
                        style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = deltaColor
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
