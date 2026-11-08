package com.cooked.app.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.cooked.app.core.designsystem.CookedMotion
import com.cooked.app.core.designsystem.CookedRadius
import com.cooked.app.core.designsystem.CookedShapes
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.rememberAreAnimationsDisabled

/**
 * Primary action button:
 * - 56dp height
 * - 14dp radius
 * - Ember primary fill with onPrimary text
 * - 0.97 pressed spring scale
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animationsDisabled = rememberAreAnimationsDisabled()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isLoading) 0.97f else 1.0f,
        animationSpec = if (animationsDisabled) {
            CookedMotion.fadeSpec(0)
        } else {
            CookedMotion.layoutSpring()
        },
        label = "PrimaryButtonScale"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp)
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = enabled && !isLoading,
        shape = CookedShapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = CookedTheme.colors.primary,
            contentColor = CookedTheme.colors.onPrimary,
            disabledContainerColor = CookedTheme.colors.surfaceContainerHigh,
            disabledContentColor = CookedTheme.colors.onSurfaceVariant
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = CookedTheme.colors.onPrimary,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(CookedTheme.spacing.s8))
                }
                Text(
                    text = text,
                    style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

/**
 * Secondary action button:
 * - 56dp height, 14dp radius
 * - Tonal / outlined styling with outlineVariant border
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animationsDisabled = rememberAreAnimationsDisabled()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isLoading) 0.97f else 1.0f,
        animationSpec = if (animationsDisabled) {
            CookedMotion.fadeSpec(0)
        } else {
            CookedMotion.layoutSpring()
        },
        label = "SecondaryButtonScale"
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp)
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = enabled && !isLoading,
        shape = CookedShapes.small,
        border = BorderStroke(1.dp, CookedTheme.colors.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = CookedTheme.colors.onSurface,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = CookedTheme.colors.onSurfaceVariant
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = CookedTheme.colors.primary,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(CookedTheme.spacing.s8))
                }
                Text(
                    text = text,
                    style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

/**
 * Ghost / text action button:
 * - 56dp height, 14dp radius
 * - Transparent background, onSurfaceVariant / primary content
 */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animationsDisabled = rememberAreAnimationsDisabled()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1.0f,
        animationSpec = if (animationsDisabled) {
            CookedMotion.fadeSpec(0)
        } else {
            CookedMotion.layoutSpring()
        },
        label = "GhostButtonScale"
    )

    TextButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp)
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = enabled,
        shape = CookedShapes.small,
        colors = ButtonDefaults.textButtonColors(
            contentColor = CookedTheme.colors.onSurfaceVariant,
            disabledContentColor = CookedTheme.colors.outline
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(CookedTheme.spacing.s8))
            }
            Text(
                text = text,
                style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun CookedButtonsPreview() {
    CookedTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            PrimaryButton(text = "Save Reminder", onClick = {}, modifier = Modifier.fillMaxWidth())
            SecondaryButton(text = "Dismiss for 10m", onClick = {}, modifier = Modifier.fillMaxWidth())
            GhostButton(text = "Cancel", onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}
