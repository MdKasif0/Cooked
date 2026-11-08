package com.cooked.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.cooked.app.core.designsystem.CookedHaptics
import com.cooked.app.core.designsystem.CookedShapes
import com.cooked.app.core.designsystem.CookedTheme

/**
 * CookedSnackbar:
 * - Floating pill / card with 14dp radius
 * - High contrast surface
 * - High-urgency Undo action
 */
@Composable
fun CookedSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val haptics = remember(hapticFeedback, view) { CookedHaptics(hapticFeedback, view) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CookedShapes.small)
            .background(
                if (CookedTheme.colors.isDark) {
                    CookedTheme.colors.surfaceContainerHigh
                } else {
                    CookedTheme.colors.onBackground
                }
            )
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = message,
                style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (CookedTheme.colors.isDark) {
                    CookedTheme.colors.onSurface
                } else {
                    CookedTheme.colors.background
                },
                modifier = Modifier.weight(1f).padding(end = 12.dp)
            )

            if (actionLabel != null && onActionClick != null) {
                Text(
                    text = actionLabel,
                    style = CookedTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (CookedTheme.colors.isDark) {
                        CookedTheme.colors.primary
                    } else {
                        CookedTheme.colors.primaryContainer
                    },
                    modifier = Modifier
                        .clickable {
                            haptics.lightTick()
                            onActionClick()
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun CookedSnackbarPreview() {
    CookedTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CookedSnackbar(
                message = "Marked 'Physics Homework' as done",
                actionLabel = "UNDO",
                onActionClick = {}
            )
        }
    }
}

