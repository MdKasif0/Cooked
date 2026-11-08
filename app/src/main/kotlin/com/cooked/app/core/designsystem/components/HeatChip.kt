package com.cooked.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.cooked.app.core.designsystem.CookedRadius
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel

/**
 * HeatChip:
 * - 10dp radius pill container
 * - 8dp circular dot with the heat accent color
 * - Semantic urgency text with AA contrast
 */
@Composable
fun HeatChip(
    level: HeatLevel,
    modifier: Modifier = Modifier,
    label: String = level.displayName,
    onClick: (() -> Unit)? = null
) {
    val heatColors = CookedTheme.colors.heatFor(level)

    val chipModifier = modifier
        .clip(RoundedCornerShape(CookedRadius.Chip))
        .background(heatColors.container)
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        )
        .padding(horizontal = 10.dp, vertical = 6.dp)

    Row(
        modifier = chipModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(heatColors.color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = heatColors.textColor
        )
    }
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun HeatChipPreview() {
    CookedTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            HeatChip(level = HeatLevel.CHILL)
            HeatChip(level = HeatLevel.WARM)
            HeatChip(level = HeatLevel.TOASTY)
            HeatChip(level = HeatLevel.COOKED)
            HeatChip(level = HeatLevel.DONE)
        }
    }
}
