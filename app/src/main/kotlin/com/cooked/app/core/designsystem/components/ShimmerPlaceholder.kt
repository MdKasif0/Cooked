package com.cooked.app.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.cooked.app.core.designsystem.CookedRadius
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.rememberAreAnimationsDisabled

/**
 * ShimmerPlaceholder:
 * - Warm-neutral subtle shimmer skeleton
 * - Smooth linear sweep
 * - Immediately static if user disabled system animations
 */
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    shape: Shape = RoundedCornerShape(CookedRadius.Chip)
) {
    val animationsDisabled = rememberAreAnimationsDisabled()

    val baseColor = CookedTheme.colors.surfaceContainer
    val highlightColor = CookedTheme.colors.surfaceContainerHigh

    val brush = if (animationsDisabled) {
        Brush.linearGradient(listOf(baseColor, baseColor))
    } else {
        val transition = rememberInfiniteTransition(label = "ShimmerTransition")
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ShimmerTranslation"
        )
        Brush.linearGradient(
            colors = listOf(baseColor, highlightColor, baseColor),
            start = Offset(translateAnim - 400f, translateAnim - 400f),
            end = Offset(translateAnim, translateAnim)
        )
    }

    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(brush)
    )
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun ShimmerPlaceholderPreview() {
    CookedTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
        ) {
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.6f), height = 24.dp)
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(), height = 60.dp, shape = RoundedCornerShape(CookedRadius.Card))
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.8f), height = 16.dp)
        }
    }
}
