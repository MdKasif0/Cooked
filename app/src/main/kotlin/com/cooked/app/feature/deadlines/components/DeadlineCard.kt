package com.cooked.app.feature.deadlines.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.designsystem.components.CookedCard
import com.cooked.app.core.designsystem.components.CookedCategoryIcon
import com.cooked.app.core.designsystem.components.CountdownText
import com.cooked.app.core.designsystem.components.HeatBar
import com.cooked.app.core.designsystem.components.HeatChip
import com.cooked.app.domain.deadline.HeatResult
import com.cooked.app.domain.model.Item
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DeadlineCard(
    item: Item,
    heatResult: HeatResult,
    startByInstant: Instant,
    onCardClick: () -> Unit,
    onImOnItClick: () -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val zone = try {
        ZoneId.of(item.timeZoneId)
    } catch (_: Exception) {
        ZoneId.systemDefault()
    }

    val startByFormatter = DateTimeFormatter.ofPattern("EEE h:mm a").withZone(zone)
    val startByText = "Start by " + startByFormatter.format(startByInstant)

    CookedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Emoji, Title, HeatChip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                if (heatResult.heatLevel == com.cooked.app.core.designsystem.HeatLevel.COOKED) CookedTheme.colors.hero
                                else if (CookedTheme.colors.isDark) CookedTheme.colors.surfaceContainerHigh
                                else CookedTheme.colors.surfaceContainerLow
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CookedCategoryIcon(
                            keyOrEmoji = item.emoji ?: "target",
                            size = 22.dp,
                            tint = if (heatResult.heatLevel == com.cooked.app.core.designsystem.HeatLevel.COOKED) CookedTheme.colors.accent else CookedTheme.colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.title,
                        style = CookedTheme.typography.titleMedium,
                        color = CookedTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                HeatChip(level = heatResult.heatLevel)
            }

            // Countdown & Reason
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountdownText(
                    targetInstant = item.dueAt,
                    style = CookedTheme.typography.displaySmall.copy(
                        color = CookedTheme.colors.onSurface
                    )
                )

                Text(
                    text = startByText,
                    style = CookedTheme.typography.labelMedium,
                    color = CookedTheme.colors.onSurfaceVariant,
                    modifier = Modifier
                        .clip(com.cooked.app.core.designsystem.PillShape)
                        .background(if (CookedTheme.colors.isDark) CookedTheme.colors.surfaceContainerHigh else CookedTheme.colors.surfaceContainerLow)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // HeatBar (Urgency visual bar)
            HeatBar(
                progress = heatResult.heatProgress,
                level = heatResult.heatLevel,
                modifier = Modifier.fillMaxWidth()
            )

            // One-line reason text
            Text(
                text = heatResult.reason,
                style = CookedTheme.typography.bodyMedium,
                color = CookedTheme.colors.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Bottom action row: Progress ring and "I'm on it" action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
                        CircularProgressIndicator(
                            progress = { item.progress / 100f },
                            modifier = Modifier.size(28.dp),
                            color = CookedTheme.colors.heatFor(heatResult.heatLevel).color,
                            trackColor = CookedTheme.colors.surfaceContainerHigh,
                            strokeWidth = 3.dp
                        )
                    }
                    Text(
                        text = "${item.progress}%",
                        style = CookedTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = CookedTheme.colors.onSurfaceVariant
                    )
                }

                // Quick buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.Button(
                        onClick = onImOnItClick,
                        shape = com.cooked.app.core.designsystem.PillShape,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (CookedTheme.colors.isDark) CookedTheme.colors.surfaceContainerHigh else CookedTheme.colors.surfaceContainerLow,
                            contentColor = CookedTheme.colors.onSurface
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "I'm on it", style = CookedTheme.typography.labelLarge)
                    }

                    androidx.compose.material3.Button(
                        onClick = onDoneClick,
                        shape = com.cooked.app.core.designsystem.PillShape,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = CookedTheme.colors.primary,
                            contentColor = CookedTheme.colors.onPrimary
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Done", style = CookedTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

