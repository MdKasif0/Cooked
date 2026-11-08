package com.cooked.app.feature.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cooked.app.core.designsystem.CookedTheme

/**
 * CalmSkilletIllustration:
 * A bespoke vector illustration of a calm, empty skillet with gentle cooling steam curls.
 * Fits the Cooked warm-neutral aesthetic for empty states.
 */
@Composable
fun CalmSkilletIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val panColor = CookedTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
    val steamColor = CookedTheme.colors.outline.copy(alpha = 0.8f)
    val accentColor = CookedTheme.colors.primary.copy(alpha = 0.7f)

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // 1. Skillet Body (Rim and Pan Bowl)
        val panLeft = w * 0.15f
        val panTop = h * 0.45f
        val panWidth = w * 0.55f
        val panHeight = h * 0.32f

        // Draw pan bottom curve
        val panPath = Path().apply {
            moveTo(panLeft, panTop + panHeight * 0.3f)
            cubicTo(
                panLeft, panTop + panHeight,
                panLeft + panWidth, panTop + panHeight,
                panLeft + panWidth, panTop + panHeight * 0.3f
            )
            lineTo(panLeft + panWidth, panTop)
            lineTo(panLeft, panTop)
            close()
        }
        drawPath(
            path = panPath,
            color = CookedTheme.colors.surfaceContainerHigh
        )
        drawPath(
            path = panPath,
            color = panColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw pan rim ellipse
        drawOval(
            color = panColor,
            topLeft = Offset(panLeft, panTop - 3.dp.toPx()),
            size = Size(panWidth, panHeight * 0.35f),
            style = Stroke(width = 3.dp.toPx())
        )

        // 2. Skillet Handle
        val handleStart = Offset(panLeft + panWidth - 2.dp.toPx(), panTop + panHeight * 0.15f)
        val handleEnd = Offset(w * 0.92f, panTop - h * 0.08f)
        drawLine(
            color = panColor,
            start = handleStart,
            end = handleEnd,
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Handle loop opening
        drawCircle(
            color = CookedTheme.colors.surface,
            radius = 3.5.dp.toPx(),
            center = Offset(w * 0.88f, panTop - h * 0.05f)
        )

        // 3. Calm Steam Curls (Gentle S-curves rising upwards)
        val steam1 = Path().apply {
            moveTo(w * 0.35f, panTop - h * 0.05f)
            cubicTo(
                w * 0.32f, panTop - h * 0.18f,
                w * 0.40f, panTop - h * 0.28f,
                w * 0.36f, panTop - h * 0.38f
            )
        }
        drawPath(
            path = steam1,
            color = steamColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        val steam2 = Path().apply {
            moveTo(w * 0.48f, panTop - h * 0.02f)
            cubicTo(
                w * 0.52f, panTop - h * 0.15f,
                w * 0.44f, panTop - h * 0.25f,
                w * 0.50f, panTop - h * 0.36f
            )
        }
        drawPath(
            path = steam2,
            color = accentColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
