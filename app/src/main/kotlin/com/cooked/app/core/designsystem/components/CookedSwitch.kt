package com.cooked.app.core.designsystem.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.cooked.app.core.designsystem.CookedHaptics
import com.cooked.app.core.designsystem.CookedTheme

/**
 * CookedSwitch:
 * - Branded Ember switch
 * - Light tick haptic on state toggle
 */
@Composable
fun CookedSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val haptics = remember(hapticFeedback, view) { CookedHaptics(hapticFeedback, view) }

    Switch(
        checked = checked,
        onCheckedChange = { newValue ->
            haptics.lightTick()
            onCheckedChange?.invoke(newValue)
        },
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = CookedTheme.colors.onPrimary,
            checkedTrackColor = CookedTheme.colors.primary,
            uncheckedThumbColor = CookedTheme.colors.onSurfaceVariant,
            uncheckedTrackColor = CookedTheme.colors.surfaceContainerHigh,
            uncheckedBorderColor = CookedTheme.colors.outline
        )
    )
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun CookedSwitchPreview() {
    CookedTheme {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            CookedSwitch(checked = true, onCheckedChange = {})
            CookedSwitch(checked = false, onCheckedChange = {})
        }
    }
}
