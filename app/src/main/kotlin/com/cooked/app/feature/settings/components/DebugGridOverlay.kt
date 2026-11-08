package com.cooked.app.feature.settings.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp

/**
 * Debug grid overlay that paints 4dp / 8dp grid lines and 20dp boundary guides
 * at 50% opacity to visually audit spatial rhythm and alignment.
 */
@Composable
fun DebugGridOverlay(
    modifier: Modifier = Modifier,
    showGrid: Boolean = true,
    showGutterGuides: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        content()

        if (showGrid || showGutterGuides) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val density = this
                val fourDpPx = with(density) { 4.dp.toPx() }
                val eightDpPx = with(density) { 8.dp.toPx() }
                val twentyDpPx = with(density) { 20.dp.toPx() }

                val width = size.width
                val height = size.height

                if (showGrid) {
                    // Vertical 4dp & 8dp grid lines
                    var x = 0f
                    var colIndex = 0
                    while (x <= width) {
                        val is8dp = colIndex % 2 == 0
                        val color = if (is8dp) {
                            Color(0x3300AAFF) // 8dp accent blue at 20%
                        } else {
                            Color(0x1A00AAFF) // 4dp subtle blue at 10%
                        }
                        drawLine(
                            color = color,
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = if (is8dp) 1.5f else 1f
                        )
                        x += fourDpPx
                        colIndex++
                    }

                    // Horizontal 4dp & 8dp grid lines
                    var y = 0f
                    var rowIndex = 0
                    while (y <= height) {
                        val is8dp = rowIndex % 2 == 0
                        val color = if (is8dp) {
                            Color(0x3300AAFF)
                        } else {
                            Color(0x1A00AAFF)
                        }
                        drawLine(
                            color = color,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = if (is8dp) 1.5f else 1f
                        )
                        y += fourDpPx
                        rowIndex++
                    }
                }

                if (showGutterGuides) {
                    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    val emberGuideColor = Color(0x99C2410C) // 60% opacity Ember

                    // Left 20dp boundary guide
                    drawLine(
                        color = emberGuideColor,
                        start = Offset(twentyDpPx, 0f),
                        end = Offset(twentyDpPx, height),
                        strokeWidth = 2.5f,
                        pathEffect = dashedEffect
                    )

                    // Right 20dp boundary guide
                    val rightTwentyDpPx = width - twentyDpPx
                    drawLine(
                        color = emberGuideColor,
                        start = Offset(rightTwentyDpPx, 0f),
                        end = Offset(rightTwentyDpPx, height),
                        strokeWidth = 2.5f,
                        pathEffect = dashedEffect
                    )
                }
            }
        }
    }
}

