package com.cooked.app.feature.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.R
import com.cooked.app.core.designsystem.CookedHaptics
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.designsystem.components.CategoryIcons
import com.cooked.app.core.designsystem.components.HeatChip
import com.cooked.app.core.designsystem.components.softShadow
import com.cooked.app.core.sound.SoundManager
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    modifier: Modifier = Modifier,
    isCompletedItem: Boolean = false
) {
    val context = LocalContext.current
    val soundManager = remember(context) { SoundManager(context.applicationContext) }
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val haptics = remember(haptic, view) { CookedHaptics(haptic, view) }
    val coroutineScope = rememberCoroutineScope()

    var isDismissed by remember { mutableStateOf(false) }
    var isDoneAnimated by remember { mutableStateOf(isCompletedItem) }
    var showSteamPuff by remember { mutableStateOf(false) }
    var hasTickedThreshold by remember { mutableStateOf(false) }

    val isDark = CookedTheme.colors.isDark
    val now = remember { Instant.now() }
    val isOverdue = remember(item.dueAt, now) { item.dueAt.isBefore(now) && !isCompletedItem }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swiped Right -> Mark Done
                    coroutineScope.launch {
                        showSteamPuff = true
                        isDoneAnimated = true
                        soundManager.playDonePop()
                        haptics.confirmDone()
                        delay(120)
                        isDismissed = true
                        delay(400)
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
            enableDismissFromStartToEnd = !isMultiSelectMode && !isCompletedItem,
            enableDismissFromEndToStart = !isMultiSelectMode && !isCompletedItem,
            backgroundContent = {
                val targetValue = dismissState.targetValue
                val bgColor by animateColorAsState(
                    targetValue = when (targetValue) {
                        SwipeToDismissBoxValue.StartToEnd -> Color(0xFFC6FF3D) // Volt
                        SwipeToDismissBoxValue.EndToStart -> Color(0xFF11120F) // Ink
                        else -> Color.Transparent
                    },
                    label = "SwipeBgColor"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgColor)
                        .padding(horizontal = 20.dp),
                    contentAlignment = if (targetValue == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    if (targetValue == SwipeToDismissBoxValue.StartToEnd) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Done",
                                tint = Color(0xFF11120F),
                                modifier = Modifier
                                    .size(24.dp)
                                    .scale(iconScale)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Done!",
                                style = CookedTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF11120F)
                            )
                        }
                    } else if (targetValue == SwipeToDismissBoxValue.EndToStart) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Snooze",
                                style = CookedTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFF5F5F0)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.Snooze,
                                contentDescription = "Snooze",
                                tint = Color(0xFFF5F5F0),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            },
            modifier = modifier
        ) {
            // Subtitle formatting
            val subtitleText = remember(item, isOverdue, now) {
                formatTaskSubtitle(item, isOverdue, now)
            }

            // Card Container
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when {
                    isSelected -> if (isDark) Color(0xFF29330A) else Color(0xFFEDF9C4)
                    isCompletedItem -> if (isDark) Color(0xFF111210).copy(alpha = 0.6f) else Color(0xFFF8F7F2).copy(alpha = 0.7f)
                    isDark -> Color(0xFF141512)
                    else -> Color(0xFFFFFFFF) // Pure white task card in Light mode
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .softShadow(RoundedCornerShape(16.dp), 10.dp, if (isDark) 0.15f else 0.04f)
                    .border(
                        1.dp,
                        if (isSelected) Color(0xFFC6FF3D)
                        else if (isDark) Color(0xFF292B25)
                        else Color(0xFFE1DFD5),
                        RoundedCornerShape(16.dp)
                    )
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
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isMultiSelectMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onItemClick() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFC6FF3D),
                                checkmarkColor = Color(0xFF11120F)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // 1. Category Icon Box (Dark Container with Volt/Cream Icon)
                    val iconRes = resolveCategoryIcon(item)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF1B1D17) else Color(0xFF11120F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            tint = Color(0xFFC6FF3D), // Volt Lime Icon
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // 2. Title & Subtitle Column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = CookedTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textDecoration = if (isCompletedItem) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = when {
                                isCompletedItem -> if (isDark) Color(0xFF777A70) else Color(0xFFA09F96)
                                isDark -> Color(0xFFF5F5F0)
                                else -> Color(0xFF151612)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = subtitleText,
                                style = CookedTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = if (isOverdue) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = when {
                                    isCompletedItem -> Color(0xFF77776F)
                                    isOverdue -> Color(0xFFB34855) // Overdue Muted Red
                                    isDark -> Color(0xFFA2A49B)
                                    else -> Color(0xFF77776F)
                                }
                            )

                            // Repeating recurrence badge
                            if (item.recurrence != null) {
                                Icon(
                                    imageVector = Icons.Rounded.Repeat,
                                    contentDescription = "Repeating",
                                    tint = if (isDark) Color(0xFFA2A49B) else Color(0xFF77776F),
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            // Inexact alarm warning icon
                            if (hasInexactWarning && !isCompletedItem) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = "May fire late",
                                    tint = Color(0xFFC28B36),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // 3. Completion Control Circle
                    if (!isMultiSelectMode) {
                        val interactionSource = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isDoneAnimated) Color(0xFFC6FF3D) else Color.Transparent)
                                .border(
                                    width = if (isDoneAnimated) 0.dp else 1.8.dp,
                                    color = if (isDoneAnimated) Color.Transparent else if (isDark) Color(0xFF34362F) else Color(0xFFD6D1C6),
                                    shape = CircleShape
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    if (!isCompletedItem) {
                                        haptics.confirmDone()
                                        soundManager.playDonePop()
                                        isDoneAnimated = true
                                        coroutineScope.launch {
                                            delay(200)
                                            isDismissed = true
                                            delay(350)
                                            onDone()
                                        }
                                    } else {
                                        onDone()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDoneAnimated) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Completed",
                                    tint = Color(0xFF11120F),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
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

// -----------------------------------------------------------------------------
// INTELLIGENT CATEGORY ICON RESOLVER
// -----------------------------------------------------------------------------
private fun resolveCategoryIcon(item: Item): Int {
    val titleLower = item.title.lowercase(Locale.getDefault())
    return when {
        item.emoji != null -> CategoryIcons.resolve(item.emoji)
        titleLower.contains("physics") || titleLower.contains("practical") || titleLower.contains("lab") -> R.drawable.ic_cat_bell
        titleLower.contains("assignment") || titleLower.contains("homework") || titleLower.contains("essay") || titleLower.contains("study") -> R.drawable.ic_cat_assignment
        titleLower.contains("rent") || titleLower.contains("pay") || titleLower.contains("bill") || titleLower.contains("bank") || titleLower.contains("emi") -> R.drawable.ic_cat_bill
        titleLower.contains("water") || titleLower.contains("drink") || titleLower.contains("hydrate") -> R.drawable.ic_cat_droplet
        titleLower.contains("code") || titleLower.contains("dev") || titleLower.contains("bug") || titleLower.contains("pr") || titleLower.contains("github") -> R.drawable.ic_cat_code
        titleLower.contains("exam") || titleLower.contains("test") || titleLower.contains("quiz") -> R.drawable.ic_cat_exam
        titleLower.contains("call") || titleLower.contains("phone") -> R.drawable.ic_cat_call
        titleLower.contains("doctor") || titleLower.contains("dentist") || titleLower.contains("pill") || titleLower.contains("medicine") -> R.drawable.ic_cat_health
        item.kind == ItemKind.DEADLINE -> R.drawable.ic_cat_target
        else -> R.drawable.ic_cat_bell
    }
}

// -----------------------------------------------------------------------------
// SUBTITLE FORMATTER
// -----------------------------------------------------------------------------
private fun formatTaskSubtitle(item: Item, isOverdue: Boolean, now: Instant): String {
    val zone = ZoneId.systemDefault()
    val dueZdt = item.dueAt.atZone(zone)
    val nowZdt = now.atZone(zone)
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    val formattedTime = timeFormatter.format(dueZdt).lowercase(Locale.getDefault())

    val diffMinutes = Duration.between(now, item.dueAt).toMinutes()

    return when {
        isOverdue -> "Overdue · $formattedTime"
        diffMinutes in 0..60 -> "In $diffMinutes minutes"
        dueZdt.toLocalDate() == nowZdt.toLocalDate() -> "Today · $formattedTime"
        dueZdt.toLocalDate() == nowZdt.toLocalDate().plusDays(1) -> "Tomorrow · $formattedTime"
        item.recurrence != null -> {
            val recurrenceLabel = when (item.recurrence) {
                is com.cooked.app.domain.model.Recurrence.Daily -> "Daily"
                is com.cooked.app.domain.model.Recurrence.Weekdays -> "Weekdays"
                is com.cooked.app.domain.model.Recurrence.Weekly -> "Weekly"
                is com.cooked.app.domain.model.Recurrence.Monthly -> "Monthly"
                is com.cooked.app.domain.model.Recurrence.Yearly -> "Yearly"
                else -> "Recurring"
            }
            "1st of next month · $recurrenceLabel"
        }
        else -> {
            val dateFormatter = DateTimeFormatter.ofPattern("d MMM · h:mm a", Locale.getDefault())
            dateFormatter.format(dueZdt).lowercase(Locale.getDefault())
        }
    }
}

// -----------------------------------------------------------------------------
// STEAM PUFF PARTICLES
// -----------------------------------------------------------------------------
@Composable
private fun RowSteamPuff(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier) {
        val p = progress.value
        val alpha = (1f - p).coerceIn(0f, 1f)
        val particleColor = Color(0xFFC6FF3D) // Volt steam

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
