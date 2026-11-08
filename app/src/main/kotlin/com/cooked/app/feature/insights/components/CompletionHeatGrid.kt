package com.cooked.app.feature.insights.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.domain.insights.model.DayCompletion
import java.time.LocalDate

@Composable
fun CompletionHeatGrid(
    days: List<DayCompletion>,
    onDaySelected: (DayCompletion) -> Unit,
    modifier: Modifier = Modifier
) {
    // 7 rows (Mon..Sun) x 13 columns (weeks) = 91 days
    val totalCols = 13
    val totalRows = 7
    val totalCompletions = days.sumOf { it.count }
    val talkBackDesc = "Activity heat grid showing $totalCompletions completions over the past 13 weeks. Tap any square to view completions."

    val primaryColor = CookedTheme.colors.primary
    val emptyCellColor = CookedTheme.colors.surfaceContainerHigh
    val onSurface = CookedTheme.colors.onSurface
    val onSurfaceVariant = CookedTheme.colors.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CookedTheme.colors.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = talkBackDesc }
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
                        text = "Completion Tapestry",
                        style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = onSurface
                    )
                    Text(
                        text = "13 weeks of quiet wins (no streaks, no pressure)",
                        style = CookedTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                }

                Text(
                    text = "$totalCompletions done",
                    style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = primaryColor
                )
            }

            // Canvas Grid
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .pointerInput(days) {
                        detectTapGestures { offset ->
                            val totalWidth = size.width
                            val totalHeight = size.height
                            val cellWidth = totalWidth / totalCols
                            val cellHeight = totalHeight / totalRows

                            val col = (offset.x / cellWidth).toInt().coerceIn(0, totalCols - 1)
                            val row = (offset.y / cellHeight).toInt().coerceIn(0, totalRows - 1)
                            val dayIndex = col * totalRows + row

                            if (dayIndex in days.indices) {
                                onDaySelected(days[dayIndex])
                            }
                        }
                    }
            ) {
                val totalWidth = size.width
                val totalHeight = size.height

                val horizontalGap = 3.dp.toPx()
                val verticalGap = 3.dp.toPx()

                val cellWidth = (totalWidth - (totalCols - 1) * horizontalGap) / totalCols
                val cellHeight = (totalHeight - (totalRows - 1) * verticalGap) / totalRows

                val cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx())

                for (col in 0 until totalCols) {
                    for (row in 0 until totalRows) {
                        val index = col * totalRows + row
                        val dayItem = if (index in days.indices) days[index] else null
                        val count = dayItem?.count ?: 0

                        val cellColor = when {
                            count == 0 -> emptyCellColor
                            count in 1..2 -> primaryColor.copy(alpha = 0.35f)
                            count in 3..4 -> primaryColor.copy(alpha = 0.70f)
                            else -> primaryColor // 5+ completions
                        }

                        val x = col * (cellWidth + horizontalGap)
                        val y = row * (cellHeight + verticalGap)

                        drawRoundRect(
                            color = cellColor,
                            topLeft = Offset(x, y),
                            size = Size(cellWidth, cellHeight),
                            cornerRadius = cornerRadius
                        )
                    }
                }
            }

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mon - Sun",
                    style = CookedTheme.typography.labelSmall,
                    color = onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Less",
                        style = CookedTheme.typography.labelSmall,
                        color = onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(2.dp),
                        color = emptyCellColor,
                        modifier = Modifier.size(10.dp)
                    ) {}
                    Surface(
                        shape = RoundedCornerShape(2.dp),
                        color = primaryColor.copy(alpha = 0.35f),
                        modifier = Modifier.size(10.dp)
                    ) {}
                    Surface(
                        shape = RoundedCornerShape(2.dp),
                        color = primaryColor.copy(alpha = 0.70f),
                        modifier = Modifier.size(10.dp)
                    ) {}
                    Surface(
                        shape = RoundedCornerShape(2.dp),
                        color = primaryColor,
                        modifier = Modifier.size(10.dp)
                    ) {}
                    Text(
                        text = "More",
                        style = CookedTheme.typography.labelSmall,
                        color = onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
