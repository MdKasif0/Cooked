package com.cooked.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.FontFeatureSettingsTabular
import com.cooked.app.core.designsystem.HeatLevel

/**
 * StatTile:
 * - Prominent tabular number (Bricolage Grotesque)
 * - Descriptive label
 * - Optional delta badge or status indicator (e.g., Cooked score, completed stats)
 */
@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    delta: String? = null,
    deltaHeatLevel: HeatLevel? = null,
    onClick: (() -> Unit)? = null
) {
    CookedCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CookedTheme.spacing.l16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = CookedTheme.typography.labelMedium,
                    color = CookedTheme.colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                if (delta != null && deltaHeatLevel != null) {
                    val heatColors = CookedTheme.colors.heatFor(deltaHeatLevel)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CookedTheme.shapes.extraSmall)
                            .background(heatColors.container)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(heatColors.color)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = delta,
                            style = CookedTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFeatureSettings = FontFeatureSettingsTabular
                            ),
                            color = heatColors.textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(CookedTheme.spacing.s8))

            Text(
                text = value,
                style = CookedTheme.typography.displaySmall.copy(
                    fontFeatureSettings = FontFeatureSettingsTabular
                ),
                color = CookedTheme.colors.onSurface
            )
        }
    }
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun StatTilePreview() {
    CookedTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            StatTile(
                value = "82",
                label = "COOKED SCORE",
                delta = "+14%",
                deltaHeatLevel = HeatLevel.TOASTY,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                value = "14",
                label = "DONE TODAY",
                delta = "Chill",
                deltaHeatLevel = HeatLevel.CHILL,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

