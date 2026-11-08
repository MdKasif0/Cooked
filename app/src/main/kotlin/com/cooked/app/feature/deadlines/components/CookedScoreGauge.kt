package com.cooked.app.feature.deadlines.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.components.EggMascot
import com.cooked.app.core.designsystem.components.RollingNumberText
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.FontFeatureSettingsTabular
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.domain.deadline.CookedScoreResult

@Composable
fun CookedScoreGauge(
    scoreResult: CookedScoreResult,
    onSuggestedActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (scoreResult.score / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900),
        label = "CookedScoreGaugeAnimation"
    )

    val chillColor = CookedTheme.colors.heatFor(HeatLevel.CHILL).color
    val warmColor = CookedTheme.colors.heatFor(HeatLevel.WARM).color
    val toastyColor = CookedTheme.colors.heatFor(HeatLevel.TOASTY).color
    val cookedColor = CookedTheme.colors.heatFor(HeatLevel.COOKED).color
    val trackColor = CookedTheme.colors.surfaceContainerHigh

    val activeColor = CookedTheme.colors.heatFor(scoreResult.heatLevel).color

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Gauge & Egg Mascot Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 200.dp, height = 120.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    val strokeWidth = 16.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = Size(diameter, diameter)

                    // 1. Draw background track (180 degrees: from 180° to 360°)
                    drawArc(
                        color = trackColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // 2. Draw active arc with heat gradient
                    if (animatedProgress > 0.01f) {
                        val sweep = 180f * animatedProgress
                        val gradient = Brush.horizontalGradient(
                            colors = listOf(chillColor, warmColor, toastyColor, cookedColor)
                        )
                        drawArc(
                            brush = gradient,
                            startAngle = 180f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Center Rolling Text inside the semicircle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    RollingNumberText(
                        number = scoreResult.score,
                        style = CookedTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            lineHeight = 44.sp
                        ),
                        color = CookedTheme.colors.onSurface
                    )
                    Text(
                        text = scoreResult.label.uppercase(),
                        style = CookedTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = activeColor,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            EggMascot(
                level = scoreResult.heatLevel,
                size = 72.dp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Suggested Action Button / Pill
        if (!scoreResult.suggestedAction.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = CookedTheme.colors.surfaceContainerLow,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(onClick = onSuggestedActionClick)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = CookedTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = scoreResult.suggestedAction,
                        style = CookedTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CookedTheme.colors.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

