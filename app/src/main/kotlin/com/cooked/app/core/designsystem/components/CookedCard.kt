package com.cooked.app.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.cooked.app.core.designsystem.CookedRadius
import com.cooked.app.core.designsystem.CookedShapes
import com.cooked.app.core.designsystem.CookedTheme

/**
 * Standard Cooked Card:
 * - 20dp rounded corners
 * - Light: Hairline 1dp outlineVariant border (#EEE7DD) on pure/warm surface (#FFFFFF)
 * - Dark: Borderless tonal surface (#1F1C19 / surfaceContainer), zero harsh drop-shadows
 */
@Composable
fun CookedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = CookedTheme.colors.isDark
    val containerColor = if (isDark) {
        CookedTheme.colors.surfaceContainer
    } else {
        CookedTheme.colors.surface
    }

    val border = if (isDark) {
        null
    } else {
        BorderStroke(1.dp, CookedTheme.colors.outlineVariant)
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = CookedShapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = CookedTheme.colors.onSurface
            ),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = CookedShapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = CookedTheme.colors.onSurface
            ),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = content
        )
    }
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun CookedCardPreview() {
    CookedTheme {
        Surface(color = CookedTheme.colors.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Physics Final",
                            style = CookedTheme.typography.titleMedium,
                            color = CookedTheme.colors.onSurface
                        )
                        Text(
                            text = "Due tomorrow at 9:00 AM",
                            style = CookedTheme.typography.bodyMedium,
                            color = CookedTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

