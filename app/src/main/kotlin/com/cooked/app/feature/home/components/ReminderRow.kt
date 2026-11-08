package com.cooked.app.feature.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.cooked.app.core.designsystem.CookedHaptics
import com.cooked.app.core.sound.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.designsystem.components.HeatChip
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReminderRow(
    item: Item,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    hasInexactWarning: Boolean,
    onItemClick: () -> Unit,
    onLongClick: () -> Unit,
    onDone: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isDismissed by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swiped Right -> Mark Done
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isDismissed = true
                    onDone()
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swiped Left -> Snooze
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSnooze()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    AnimatedVisibility(
        visible = !isDismissed,
        exit = shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeOut()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = !isMultiSelectMode,
            enableDismissFromEndToStart = !isMultiSelectMode,
            backgroundContent = {
                val targetValue = dismissState.targetValue
                val bgColor by animateColorAsState(
                    targetValue = when (targetValue) {
                        SwipeToDismissBoxValue.StartToEnd -> CookedTheme.colors.heatFor(HeatLevel.CHILL).color
                        SwipeToDismissBoxValue.EndToStart -> CookedTheme.colors.heatFor(HeatLevel.WARM).color
                        else -> Color.Transparent
                    },
                    label = "SwipeBgColor"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(bgColor)
                        .padding(horizontal = 20.dp),
                    contentAlignment = if (targetValue == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    if (targetValue == SwipeToDismissBoxValue.StartToEnd) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Done",
                                tint = CookedTheme.colors.heatFor(HeatLevel.CHILL).onColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Done!",
                                style = CookedTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = CookedTheme.colors.heatFor(HeatLevel.CHILL).onColor
                            )
                        }
                    } else if (targetValue == SwipeToDismissBoxValue.EndToStart) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Snooze",
                                style = CookedTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = CookedTheme.colors.heatFor(HeatLevel.WARM).onColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.Snooze,
                                contentDescription = "Snooze",
                                tint = CookedTheme.colors.heatFor(HeatLevel.WARM).onColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            },
            modifier = modifier
        ) {
            val isOverdue = item.dueAt.isBefore(Instant.now())
            val formattedTime = remember(item.dueAt) {
                val zdt = item.dueAt.atZone(ZoneId.systemDefault())
                DateTimeFormatter.ofPattern("h:mm a").format(zdt)
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) CookedTheme.colors.primaryContainer.copy(alpha = 0.5f) else CookedTheme.colors.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = onItemClick,
                        onLongClick = onLongClick
                    )
                    .semantics {
                        customActions = listOf(
                            CustomAccessibilityAction("Mark as done") {
                                onDone()
                                true
                            },
                            CustomAccessibilityAction("Snooze reminder") {
                                onSnooze()
                                true
                            }
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isMultiSelectMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onItemClick() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = CookedTheme.colors.primary,
                                checkmarkColor = CookedTheme.colors.onPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Emoji or subtle indicator
                    if (item.emoji != null) {
                        Text(
                            text = item.emoji!!,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                    }

                    // Title & Metadata
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = CookedTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = CookedTheme.colors.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Time
                            Text(
                                text = formattedTime,
                                style = CookedTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = if (isOverdue) CookedTheme.colors.heatFor(HeatLevel.COOKED).color else CookedTheme.colors.onSurfaceVariant
                            )

                            // Repeating Icon
                            if (item.recurrence != null) {
                                Icon(
                                    imageVector = Icons.Rounded.Repeat,
                                    contentDescription = "Repeating",
                                    tint = CookedTheme.colors.heatFor(HeatLevel.CHILL).color,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            // Inexact warning
                            if (hasInexactWarning) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = "May fire slightly late",
                                    tint = CookedTheme.colors.heatFor(HeatLevel.WARM).color,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Trailing Badges: Urgent / Deadline
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.critical) {
                            HeatChip(level = HeatLevel.COOKED)
                        } else if (item.kind == ItemKind.DEADLINE) {
                            Icon(
                                imageVector = Icons.Rounded.Whatshot,
                                contentDescription = "Deadline",
                                tint = CookedTheme.colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

