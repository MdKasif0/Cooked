package com.cooked.app.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.cooked.app.core.designsystem.CookedTheme

/**
 * Custom vector graphic representing a peaceful skillet with tiny resting bubbles/steam.
 */
@Composable
fun CalmSkilletIllustration(
    modifier: Modifier = Modifier,
    accentColor: Color = CookedTheme.colors.primary,
    subtleColor: Color = CookedTheme.colors.outline
) {
    Canvas(modifier = modifier.size(100.dp)) {
        val w = size.width
        val h = size.height

        // Skillet body (pan)
        drawRoundRect(
            color = subtleColor,
            topLeft = Offset(w * 0.15f, h * 0.45f),
            size = Size(w * 0.55f, h * 0.28f),
            cornerRadius = CornerRadius(w * 0.12f, h * 0.12f),
            style = Stroke(width = 3.dp.toPx())
        )

        // Skillet handle
        val handlePath = Path().apply {
            moveTo(w * 0.70f, h * 0.52f)
            lineTo(w * 0.92f, h * 0.46f)
        }
        drawPath(
            path = handlePath,
            color = subtleColor,
            style = Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )

        // Calm flame / sparkle above the skillet
        val flamePath = Path().apply {
            moveTo(w * 0.425f, h * 0.20f)
            cubicTo(
                w * 0.50f, h * 0.28f,
                w * 0.52f, h * 0.35f,
                w * 0.425f, h * 0.40f
            )
            cubicTo(
                w * 0.33f, h * 0.35f,
                w * 0.35f, h * 0.28f,
                w * 0.425f, h * 0.20f
            )
        }
        drawPath(
            path = flamePath,
            color = accentColor
        )

        // Gentle little sparkle dot
        drawCircle(
            color = subtleColor,
            radius = 2.5.dp.toPx(),
            center = Offset(w * 0.26f, h * 0.26f)
        )
        drawCircle(
            color = subtleColor,
            radius = 2.dp.toPx(),
            center = Offset(w * 0.60f, h * 0.24f)
        )
    }
}

/**
 * EmptyState:
 * - Mascot egg representation (calm/chill/done)
 * - Restrained warm tone
 * - Optional action button
 */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    eggLevel: HeatLevel? = HeatLevel.CHILL,
    isDone: Boolean = false,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(CookedTheme.spacing.xl24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EggMascot(
            level = eggLevel,
            isDone = isDone,
            size = 88.dp
        )

        Spacer(modifier = Modifier.height(CookedTheme.spacing.l16))

        Text(
            text = title,
            style = CookedTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = CookedTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(CookedTheme.spacing.s8))

        Text(
            text = message,
            style = CookedTheme.typography.bodyLarge,
            color = CookedTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(CookedTheme.spacing.xl24))
            PrimaryButton(
                text = actionButtonText,
                onClick = onActionClick
            )
        }
    }
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun EmptyStatePreview() {
    CookedTheme {
        EmptyState(
            title = "Suspiciously Calm",
            message = "Nothing due right now. Enjoy it before things start sizzling.",
            actionButtonText = "+ Add Quick Reminder",
            onActionClick = {}
        )
    }
}
