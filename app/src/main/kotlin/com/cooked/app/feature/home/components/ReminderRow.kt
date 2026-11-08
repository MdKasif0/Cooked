package com.cooked.app.feature.home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import com.cooked.app.core.designsystem.components.CircleButtonStyle
import com.cooked.app.core.designsystem.components.CircleIconButton
import com.cooked.app.core.designsystem.components.softShadow

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
import com.cooked.app.core.designsystem.components.CookedCategoryIcon
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
    val context = LocalContext.current
    val soundManager = remember(context) { SoundManager(context.applicationContext) }
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val haptics = remember(haptic, view) { CookedHaptics(haptic, view) }
    val coroutineScope = rememberCoroutineScope()

    var isDismissed by remember { mutableStateOf(false) }
    var showSteamPuff by remember { mutableStateOf(false) }
    var hasTickedThreshold by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swiped Right -> Mark Done with sound + steam puff + spring collapse
                    coroutineScope.launch {
                        showSteamPuff = true
                        soundManager.playDonePop()
                        haptics.confirmDone()
                        delay(120)
                        isDismissed = true
                        delay(480)
                        onDone()
                    }
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swiped Left -> Snooze
                    haptics.lightTick()
                    onSnooze()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    // Dynamic icon scale based on drag progress
    val progress = dismissState.progress
    val isSwipingToDone = dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd

    LaunchedEffect(progress, isSwipingToDone) {
        if (isSwipingToDone && progress > 0.38f && !hasTickedThreshold) {
            haptics.lightTick()
            hasTickedThreshold = true
        } else if (!isSwipingToDone || progress <= 0.30f) {
            hasTickedThreshold = false
        }
    }

    val iconScale by animateFloatAsState(
        targetValue = if (isSwipingToDone && progress > 0.38f) 1.25f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "DoneIconScale"
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
                        SwipeToDismissBoxValue.StartToEnd -> CookedTheme.colors.accent
                        SwipeToDismissBoxValue.EndToStart -> CookedTheme.colors.hero
                        else -> Color.Transparent
                    },
                    label = "SwipeBgColor"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(bgColor)
                        .padding(horizontal = 20.dp),
                    contentAlignment = if (targetValue == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    if (targetValue == SwipeToDismissBoxValue.StartToEnd) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Done",
                                tint = CookedTheme.colors.onAccent,
                                modifier = Modifier
                                    .size(24.dp)
                                    .scale(iconScale)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Done!",
                                style = CookedTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = CookedTheme.colors.onAccent
                            )
                        }
                    } else if (targetValue == SwipeToDismissBoxValue.EndToStart) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Snooze",
                                style = CookedTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = CookedTheme.colors.onHero
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.Snooze,
                                contentDescription = "Snooze",
                                tint = CookedTheme.colors.onHero,
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
                shape = RoundedCornerShape(24.dp),
                color = when {
                    isSelected -> CookedTheme.colors.accentContainer
                    CookedTheme.colors.isDark -> CookedTheme.colors.surfaceContainer
                    else -> CookedTheme.colors.surface
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .softShadow(RoundedCornerShape(24.dp), 14.dp, 0.06f)
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
                        .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
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

                    // Category icon tile (neon-tinted for critical / overdue items)
                    val tileHot = item.critical || isOverdue
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                when {
                                    tileHot -> CookedTheme.colors.hero
                                    CookedTheme.colors.isDark -> CookedTheme.colors.surfaceContainerHigh
                                    else -> CookedTheme.colors.surfaceContainerLow
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CookedCategoryIcon(
                            keyOrEmoji = item.emoji ?: if (item.kind == ItemKind.DEADLINE) "target" else "bell",
                            size = 22.dp,
                            tint = if (tileHot) CookedTheme.colors.accent else CookedTheme.colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))

                    // Title & Metadata
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = CookedTheme.typography.titleMedium,
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
                                text = if (isOverdue) "Overdue · $formattedTime" else formattedTime,
                                style = CookedTheme.typography.labelMedium,
                                color = if (isOverdue) CookedTheme.colors.heatFor(HeatLevel.COOKED).textColor else CookedTheme.colors.onSurfaceVariant
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
                            HeatChip(level = HeatLevel.COOKED, label = "Critical")
                        }
                        if (!isMultiSelectMode) {
                            // One-tap complete, mirroring swipe-right
                            CircleIconButton(
                                icon = Icons.Rounded.Check,
                                contentDescription = "Mark ${item.title} done",
                                onClick = {
                                    haptics.confirmDone()
                                    soundManager.playDonePop()
                                    onDone()
                                },
                                style = if (CookedTheme.colors.isDark) CircleButtonStyle.Surface else CircleButtonStyle.Ghost,
                                size = 40.dp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .border(1.5.dp, CookedTheme.colors.outline, CircleShape)
                            )
                        }
                    }
                }
            }

            if (showSteamPuff) {
                RowSteamPuff(
                    modifier = Modifier
                        .padding(start = 24.dp)
                        .size(48.dp)
                )
            }
        }
    }
}

/**
 * 4 steam puff particles expanding and dissipating on swipe completion (600ms)
 */
@Composable
private fun RowSteamPuff(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier) {
        val p = progress.value
        val alpha = (1f - p).coerceIn(0f, 1f)
        val particleColor = Color(0xFFB8E62E) // Volt steam

        val offsets = listOf(
            Offset(-12f * p, -16f * p),
            Offset(6f * p, -22f * p),
            Offset(14f * p, -12f * p),
            Offset(-4f * p, -26f * p)
        )

        offsets.forEachIndexed { i, offset ->
            drawCircle(
                color = particleColor.copy(alpha = alpha * 0.7f),
                radius = (4f + i * 1.5f) * (1f + p * 0.5f),
                center = Offset(size.width / 2f + offset.x, size.height / 2f + offset.y)
            )
        }
    }
}

