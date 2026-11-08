package com.cooked.app.feature.insights.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.domain.insights.model.WeeklyScorePoint
import kotlin.math.roundToInt

@Composable
fun CookedScoreHistoryChart(
    points: List<WeeklyScorePoint>,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()

    // Animation progress 0f..1f (400ms entrance)
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
    }

    var selectedIndex by remember { mutableIntStateOf(-1) }
    var scrubX by remember { mutableStateOf(0f) }

    val hasEnoughData = points.isNotEmpty()
    val currentWeekPoint = points.lastOrNull { it.isCurrentWeek } ?: points.lastOrNull()
    val talkBackDescription = if (currentWeekPoint != null) {
        "12-week Cooked Score trend line. Current week score is ${currentWeekPoint.score} out of 100."
    } else {
        "12-week Cooked Score chart with no active data."
    }

    // Rendered as an ink hero panel with a volt trend line (reference: "AI Price Forecast").
    val primaryColor = CookedTheme.colors.accent
    val outlineVariant = CookedTheme.colors.heroRaised
    val surfaceColor = CookedTheme.colors.hero
    val onSurfaceVariant = CookedTheme.colors.onHeroMuted
    val onSurface = CookedTheme.colors.onHero

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = CookedTheme.colors.hero,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = talkBackDescription }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cooked Score Trend",
                        style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = onSurface
                    )
                    Text(
                        text = "12-week workload pressure index",
                        style = CookedTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                }

                if (currentWeekPoint != null) {
                    Surface(
                        shape = com.cooked.app.core.designsystem.PillShape,
                        color = CookedTheme.colors.accent
                    ) {
                        Text(
                            text = "Now ${currentWeekPoint.score}",
                            style = CookedTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = CookedTheme.colors.onAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (!hasEnoughData) {
                    // Empty ghosted state
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val ghostPath = Path().apply {
                            moveTo(0f, h * 0.7f)
                            cubicTo(w * 0.3f, h * 0.4f, w * 0.7f, h * 0.8f, w, h * 0.5f)
                        }
                        drawPath(
                            path = ghostPath,
                            color = outlineVariant.copy(alpha = 0.5f),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                            )
                        )
                    }
                    Text(
                        text = "Need a few more days of data.",
                        style = CookedTheme.typography.bodySmall,
                        color = onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(points) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        scrubX = offset.x
                                        val idx = calculateIndex(offset.x, size.width.toFloat(), points.size)
                                        if (idx != selectedIndex) {
                                            selectedIndex = idx
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    },
                                    onDragEnd = { selectedIndex = -1 },
                                    onDragCancel = { selectedIndex = -1 },
                                    onDrag = { change, _ ->
                                        scrubX = change.position.x
                                        val idx = calculateIndex(change.position.x, size.width.toFloat(), points.size)
                                        if (idx != selectedIndex && idx in points.indices) {
                                            selectedIndex = idx
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    }
                                )
                            }
                    ) {
                        val w = size.width
                        val h = size.height
                        val paddingBottom = 24.dp.toPx()
                        val chartHeight = h - paddingBottom
                        val progress = animProgress.value

                        // 1. Gridlines (Light mode: 3 gridlines at 40% opacity; Dark mode: no gridlines)
                        if (!isDark) {
                            val gridLineCount = 3
                            for (i in 1..gridLineCount) {
                                val y = chartHeight * (i.toFloat() / (gridLineCount + 1))
                                drawLine(
                                    color = outlineVariant.copy(alpha = 0.4f),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                                )
                            }
                        }

                        // 2. Build Area & Stroke Paths
                        val stepX = w / (points.size - 1).coerceAtLeast(1)
                        val linePath = Path()
                        val areaPath = Path()

                        val coords = points.mapIndexed { i, p ->
                            val x = i * stepX
                            val normalized = (p.score.toFloat() / 100f).coerceIn(0f, 1f)
                            // Animated from bottom baseline
                            val y = chartHeight - (normalized * chartHeight * progress)
                            Offset(x, y)
                        }

                        if (coords.isNotEmpty()) {
                            linePath.moveTo(coords[0].x, coords[0].y)
                            areaPath.moveTo(coords[0].x, chartHeight)
                            areaPath.lineTo(coords[0].x, coords[0].y)

                            for (i in 1 until coords.size) {
                                val prev = coords[i - 1]
                                val curr = coords[i]
                                val midX = (prev.x + curr.x) / 2f
                                linePath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                                areaPath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }

                            areaPath.lineTo(coords.last().x, chartHeight)
                            areaPath.close()

                            // Vertical Heat Gradient Fill (Ember fading to transparent)
                            drawPath(
                                path = areaPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.35f * progress),
                                        primaryColor.copy(alpha = 0.05f * progress),
                                        Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = chartHeight
                                )
                            )

                            // Main Trend Line
                            drawPath(
                                path = linePath,
                                color = primaryColor,
                                style = Stroke(
                                    width = 2.5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )

                            // 3. Highlight Current Week Dot
                            val currentIdx = points.indexOfFirst { it.isCurrentWeek }
                            if (currentIdx != -1 && currentIdx in coords.indices) {
                                val currentCoord = coords[currentIdx]
                                // Glow ring
                                drawCircle(
                                    color = primaryColor.copy(alpha = 0.25f),
                                    radius = 8.dp.toPx(),
                                    center = currentCoord
                                )
                                // Solid center dot
                                drawCircle(
                                    color = primaryColor,
                                    radius = 4.5.dp.toPx(),
                                    center = currentCoord
                                )
                            }

                            // 4. Scrubber Vertical Indicator Line
                            if (selectedIndex in coords.indices) {
                                val selectedCoord = coords[selectedIndex]
                                drawLine(
                                    color = onSurfaceVariant,
                                    start = Offset(selectedCoord.x, 0f),
                                    end = Offset(selectedCoord.x, chartHeight),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                                )
                                drawCircle(
                                    color = surfaceColor,
                                    radius = 6.dp.toPx(),
                                    center = selectedCoord
                                )
                                drawCircle(
                                    color = primaryColor,
                                    radius = 4.dp.toPx(),
                                    center = selectedCoord
                                )
                            }
                        }
                    }

                    // Scrubber Value Tooltip
                    if (selectedIndex in points.indices) {
                        val point = points[selectedIndex]
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = CookedTheme.colors.heroRaised,
                            tonalElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset { IntOffset(0, 8.dp.roundToPx()) }
                        ) {
                            Text(
                                text = "${point.weekLabel}: ${point.score}/100",
                                style = CookedTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Axis Labels (First week, Mid week, Now)
            if (points.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = points.first().weekLabel,
                        style = CookedTheme.typography.labelSmall,
                        color = onSurfaceVariant
                    )
                    Text(
                        text = points[points.size / 2].weekLabel,
                        style = CookedTheme.typography.labelSmall,
                        color = onSurfaceVariant
                    )
                    Text(
                        text = points.last().weekLabel,
                        style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = primaryColor
                    )
                }
            }
        }
    }
}

private fun calculateIndex(touchX: Float, totalWidth: Float, count: Int): Int {
    if (count <= 1 || totalWidth <= 0f) return 0
    val step = totalWidth / (count - 1)
    val idx = (touchX / step).roundToInt()
    return idx.coerceIn(0, count - 1)
}
