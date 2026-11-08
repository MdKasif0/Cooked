package com.cooked.app.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.cooked.app.core.designsystem.CookedMotion
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.designsystem.rememberAreAnimationsDisabled

/**
 * HeatBar:
 * - Thin progress bar colored by urgency heat level
 * - Smooth animated progress fill
 * - Rounded end caps
 */
@Composable
fun HeatBar(
    progress: Float,
    level: HeatLevel,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    trackColor: Color = CookedTheme.colors.surfaceContainerHigh
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val animationsDisabled = rememberAreAnimationsDisabled()

    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = if (animationsDisabled) {
            CookedMotion.fadeSpec(0)
        } else {
            CookedMotion.layoutSpring()
        },
        label = "HeatBarProgress"
    )

    val fillColor = CookedTheme.colors.heatFor(level).color

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(height / 2))
                .background(fillColor)
        )
    }
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun HeatBarPreview() {
    CookedTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
        ) {
            HeatBar(progress = 0.25f, level = HeatLevel.CHILL)
            HeatBar(progress = 0.50f, level = HeatLevel.WARM)
            HeatBar(progress = 0.75f, level = HeatLevel.TOASTY)
            HeatBar(progress = 0.95f, level = HeatLevel.COOKED)
            HeatBar(progress = 1.0f, level = HeatLevel.DONE)
        }
    }
}

