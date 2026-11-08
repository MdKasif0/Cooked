package com.cooked.app.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.cooked.app.core.designsystem.CookedMotion
import com.cooked.app.core.designsystem.CookedRadius
import com.cooked.app.core.designsystem.CookedShapes
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.rememberAreAnimationsDisabled

/**
 * SegmentedControl:
 * - Tonal pill selector
 * - Animated indicator for selected option
 * - Light tick haptic feedback on change
 */
@Composable
fun <T> SegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val haptics = remember(hapticFeedback, view) { CookedHaptics(hapticFeedback, view) }
    val animationsDisabled = rememberAreAnimationsDisabled()

    val selectedIndex = items.indexOf(selectedItem).coerceAtLeast(0)

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(CookedRadius.Button))
            .background(CookedTheme.colors.surfaceContainer)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = item == selectedItem
                val itemInteractionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(CookedRadius.Button - 2.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    if (CookedTheme.colors.isDark) {
                                        CookedTheme.colors.surfaceContainerHigh
                                    } else {
                                        CookedTheme.colors.surface
                                    }
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable(
                            interactionSource = itemInteractionSource,
                            indication = null
                        ) {
                            if (!isSelected) {
                                haptics.lightTick()
                                onItemSelected(item)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelProvider(item),
                        style = CookedTheme.typography.titleMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = if (isSelected) {
                            CookedTheme.colors.onSurface
                        } else {
                            CookedTheme.colors.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun SegmentedControlPreview() {
    CookedTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            SegmentedControl(
                items = listOf("All", "Active", "Done"),
                selectedItem = "Active",
                onItemSelected = {},
                labelProvider = { it }
            )
            SegmentedControl(
                items = listOf("System", "Light", "Dark"),
                selectedItem = "Dark",
                onItemSelected = {},
                labelProvider = { it }
            )
        }
    }
}

