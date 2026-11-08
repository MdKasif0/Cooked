package com.cooked.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

/**
 * SectionHeader:
 * - Headline/title with Bricolage Grotesque
 * - Optional numeric count pill
 * - Optional trailing action (e.g. "See all")
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    count: Int? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = CookedTheme.spacing.s8)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = title,
                    style = CookedTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = CookedTheme.colors.onBackground
                )

                if (count != null) {
                    Spacer(modifier = Modifier.width(CookedTheme.spacing.s8))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CookedTheme.colors.surfaceContainerHigh)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = count.toString(),
                            style = CookedTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = FontFeatureSettingsTabular
                            ),
                            color = CookedTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }

            if (actionText != null && onActionClick != null) {
                Text(
                    text = actionText,
                    style = CookedTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = CookedTheme.colors.primary,
                    modifier = Modifier
                        .clip(CookedTheme.shapes.extraSmall)
                        .clickable(onClick = onActionClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = CookedTheme.typography.bodySmall,
                color = CookedTheme.colors.onSurfaceVariant
            )
        }
    }
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun SectionHeaderPreview() {
    CookedTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(
                title = "Critical Heat",
                count = 3,
                actionText = "See all",
                onActionClick = {}
            )
            SectionHeader(
                title = "Later This Week",
                count = 7
            )
        }
    }
}

