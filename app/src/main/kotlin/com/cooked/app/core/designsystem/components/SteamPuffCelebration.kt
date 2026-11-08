package com.cooked.app.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedTheme
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * A confetti-free steam puff celebration that rises and dissipates gracefully in under 800ms.
 * Shown when completing a Toasty or Cooked item.
 */
@Composable
fun SteamPuffCelebration(
    visible: Boolean,
    message: String = "Not cooked today.",
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(750) // Total duration strictly under 800ms
            onFinished()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.85f),
        exit = fadeOut() + scaleOut(targetScale = 1.05f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 96.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = CookedTheme.colors.surfaceContainerHigh,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, CookedTheme.colors.primary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SteamWispIcon(
                        color = CookedTheme.colors.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = message,
                        style = CookedTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = CookedTheme.colors.onSurface
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SteamWispIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 3 subtle rising wavy lines
        val strokeWidth = 2.dp.toPx()
        val offsets = listOf(0.25f, 0.5f, 0.75f)

        offsets.forEachIndexed { i, factor ->
            val path = Path()
            val startX = w * factor
            path.moveTo(startX, h * 0.9f)
            path.cubicTo(
                startX + (if (i % 2 == 0) 3f else -3f), h * 0.6f,
                startX + (if (i % 2 == 0) -3f else 3f), h * 0.3f,
                startX, h * 0.1f
            )
            drawPath(
                path = path,
                color = color.copy(alpha = 0.85f - (i * 0.15f)),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}
