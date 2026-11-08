package com.cooked.app.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.cooked.app.core.designsystem.CookedHaptics
import com.cooked.app.core.designsystem.CookedShapes
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.sound.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ReminderRow:
 * - Swipe right (StartToEnd) = Done with green reveal, check icon scale jump at threshold with haptic tick
 * - Release = spring collapse + steam puff (4 particles, 600ms) + confirm haptic + done_pop sound
 * - Swipe left (EndToStart) = Snooze with warm reveal and light tick
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderRow(
    title: String,
    timeText: String,
    heatLevel: HeatLevel,
    onDone: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val soundManager = remember(context) { SoundManager(context.applicationContext) }
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val haptics = remember(hapticFeedback, view) { CookedHaptics(hapticFeedback, view) }
    val coroutineScope = rememberCoroutineScope()

    var isCollapsing by remember { mutableStateOf(false) }
    var showSteamPuff by remember { mutableStateOf(false) }
    var hasTickedThreshold by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    coroutineScope.launch {
                        showSteamPuff = true
                        soundManager.playDonePop()
                        haptics.confirmDone()
                        delay(120)
                        isCollapsing = true
                        delay(480)
                        onDone()
                    }
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    haptics.lightTick()
                    onSnooze()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    // Dynamic icon scale based on swipe progress
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

    val iconScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isSwipingToDone && progress > 0.38f) 1.25f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "DoneIconScale"
    )

    AnimatedVisibility(
        visible = !isCollapsing,
        exit = shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeOut(animationSpec = tween(300))
    ) {
        Box(modifier = modifier) {
            SwipeToDismissBox(
                state = dismissState,
                modifier = Modifier.clip(CookedShapes.medium),
                backgroundContent = {
                    val direction = dismissState.dismissDirection
                    val backgroundColor by animateColorAsState(
                        targetValue = when (direction) {
                            SwipeToDismissBoxValue.StartToEnd -> CookedTheme.colors.done.color
                            SwipeToDismissBoxValue.EndToStart -> CookedTheme.colors.warm.color
                            SwipeToDismissBoxValue.Settled -> Color.Transparent
                        },
                        label = "SwipeBackground"
                    )

                    val alignment = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                        SwipeToDismissBoxValue.Settled -> Alignment.Center
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor)
                            .padding(horizontal = 24.dp),
                        contentAlignment = alignment
                    ) {
                        when (direction) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Done",
                                    tint = Color.White,
                                    modifier = Modifier.scale(iconScale)
                                )
                            }
                            SwipeToDismissBoxValue.EndToStart -> {
                                Icon(
                                    imageVector = Icons.Rounded.Snooze,
                                    contentDescription = "Snooze",
                                    tint = Color.White
                                )
                            }
                            SwipeToDismissBoxValue.Settled -> {}
                        }
                    }
                }
            ) {
                CookedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClick
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = title,
                                style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = CookedTheme.colors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = timeText,
                                style = CookedTheme.typography.bodyMedium,
                                color = CookedTheme.colors.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        HeatChip(level = heatLevel)
                    }
                }
            }

            // Steam puff particle burst on Done release (4 particles, 600ms)
            if (showSteamPuff) {
                RowSteamPuff(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
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
        val particleColor = Color(0xFF10B981) // Done emerald tint

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

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun ReminderRowPreview() {
    CookedTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ReminderRow(
                title = "Study Thermodynamics Ch 4",
                timeText = "Due in 38m",
                heatLevel = HeatLevel.COOKED,
                onDone = {},
                onSnooze = {}
            )
        }
    }
}
