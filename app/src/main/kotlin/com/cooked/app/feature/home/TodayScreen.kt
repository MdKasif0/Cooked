package com.cooked.app.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.R
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.FontFeatureSettingsTabular
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.designsystem.components.EggMascot
import com.cooked.app.core.designsystem.components.ShimmerPlaceholder
import com.cooked.app.core.designsystem.components.SteamPuffCelebration
import com.cooked.app.feature.home.components.CalmSkilletIllustration
import com.cooked.app.feature.home.components.ReminderRow
import com.cooked.app.feature.home.components.SnoozeSheet
import java.time.Instant

@Composable
fun TodayScreen(
    uiState: TodayUiState,
    onReminderClick: (String) -> Unit,
    onDoneClick: (String) -> Unit,
    onOpenSnoozeSheet: (String) -> Unit,
    onSnoozeSelected: (String, Instant) -> Unit,
    onDismissSnoozeSheet: () -> Unit,
    onItemLongClick: (String) -> Unit,
    onBatchDone: () -> Unit,
    onOpenBatchSnooze: () -> Unit,
    onBatchSnoozeSelected: (Instant) -> Unit,
    onDismissBatchSnooze: () -> Unit,
    onBatchDelete: () -> Unit,
    onClearSelection: () -> Unit,
    onCookedScoreClick: () -> Unit,
    onClearAllOverdue: () -> Unit = {},
    onNavigateToReliability: () -> Unit = {},
    onDismissCelebration: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CookedTheme.spacing.screenHorizontal, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.6f).height(32.dp))
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.4f).height(18.dp))
                Spacer(modifier = Modifier.height(12.dp))
                repeat(4) {
                    ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(68.dp))
                }
            }
        } else {
            val todayActiveItems = remember(uiState.nowItems, uiState.laterTodayItems) {
                uiState.nowItems + uiState.laterTodayItems
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CookedTheme.spacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
            ) {
                // 1. Editorial Greeting Section
                item(key = "today_header") {
                    val timeIconRes = when (uiState.timeOfDayGreeting) {
                        "Morning" -> R.drawable.ic_time_morning
                        "Afternoon" -> R.drawable.ic_time_afternoon
                        "Evening" -> R.drawable.ic_time_evening
                        else -> R.drawable.ic_time_night
                    }

                    val mascotHeat = when {
                        uiState.overdueItems.isNotEmpty() -> HeatLevel.COOKED
                        uiState.nowItems.isNotEmpty() -> HeatLevel.TOASTY
                        uiState.totalActiveToday == 0 && uiState.completedTodayCount > 0 -> null
                        else -> HeatLevel.CHILL
                    }
                    val mascotIsDone = uiState.totalActiveToday == 0 && uiState.completedTodayCount > 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Subtitle with time context
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(timeIconRes),
                                    contentDescription = uiState.timeOfDayGreeting,
                                    tint = CookedTheme.colors.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "${uiState.timeOfDayGreeting} · ${uiState.dateTitle}",
                                    style = CookedTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    ),
                                    color = CookedTheme.colors.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Big bold editorial headline with custom highlighter brush
                            EditorialHeadline(
                                headline = uiState.greetingHeadline,
                                highlight = uiState.greetingHighlight
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Expressive mascot egg reacting to urgency
                        EggMascot(
                            level = mascotHeat,
                            isDone = mascotIsDone,
                            size = 68.dp
                        )
                    }
                }

                // 2. Today at a Glance Hero Card
                item(key = "today_hero") {
                    TodayAtAGlanceCard(
                        uiState = uiState,
                        onClick = onCookedScoreClick
                    )
                }

                // 3. Reliability notice banner
                if (uiState.hasExactAlarmWarning) {
                    item(key = "reliability_warning_banner") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(CookedTheme.colors.accentContainer)
                                .clickable { onNavigateToReliability() }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CookedTheme.colors.hero),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_emergency_siren),
                                    contentDescription = null,
                                    tint = CookedTheme.colors.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Alarms may run late",
                                    style = CookedTheme.typography.titleSmall,
                                    color = CookedTheme.colors.onAccentContainer
                                )
                                Text(
                                    text = "Allow exact alarms so nags land on time.",
                                    style = CookedTheme.typography.bodySmall,
                                    color = CookedTheme.colors.onAccentContainer.copy(alpha = 0.8f)
                                )
                            }
                            Text(
                                text = "Fix",
                                style = CookedTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = CookedTheme.colors.onAccentContainer
                            )
                        }
                    }
                }

                // 4. Empty State
                if (uiState.isEmpty) {
                    item(key = "empty_state") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp, bottom = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CalmSkilletIllustration(size = 140.dp)
                            Text(
                                text = "Nothing due. Suspiciously calm.",
                                style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CookedTheme.colors.onSurface
                            )
                            Text(
                                text = "Go drink water or do something bold.",
                                style = CookedTheme.typography.bodyMedium,
                                color = CookedTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }

                // 5. Overdue Section
                if (uiState.overdueItems.isNotEmpty()) {
                    item(key = "overdue_header") {
                        DashboardSectionHeader(
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = "Overdue",
                                    tint = Color(0xFFB34855),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            title = "Overdue",
                            count = uiState.overdueItems.size,
                            actionText = "Clear all",
                            onActionClick = onClearAllOverdue
                        )
                    }

                    items(
                        items = uiState.overdueItems,
                        key = { it.id }
                    ) { item ->
                        ReminderRow(
                            item = item,
                            isSelected = uiState.selectedItemIds.contains(item.id),
                            isMultiSelectMode = uiState.isMultiSelectMode,
                            hasInexactWarning = uiState.hasExactAlarmWarning,
                            onItemClick = {
                                if (uiState.isMultiSelectMode) {
                                    onItemLongClick(item.id)
                                } else {
                                    onReminderClick(item.id)
                                }
                            },
                            onLongClick = { onItemLongClick(item.id) },
                            onDone = { onDoneClick(item.id) },
                            onSnooze = { onOpenSnoozeSheet(item.id) }
                        )
                    }
                }

                // 6. Today Section (active today: now + later today)
                if (todayActiveItems.isNotEmpty()) {
                    item(key = "today_section_header") {
                        DashboardSectionHeader(
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarToday,
                                    contentDescription = "Today",
                                    tint = Color(0xFF4D8950),
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            title = "Today",
                            count = todayActiveItems.size
                        )
                    }

                    items(
                        items = todayActiveItems,
                        key = { it.id }
                    ) { item ->
                        ReminderRow(
                            item = item,
                            isSelected = uiState.selectedItemIds.contains(item.id),
                            isMultiSelectMode = uiState.isMultiSelectMode,
                            hasInexactWarning = uiState.hasExactAlarmWarning,
                            onItemClick = {
                                if (uiState.isMultiSelectMode) {
                                    onItemLongClick(item.id)
                                } else {
                                    onReminderClick(item.id)
                                }
                            },
                            onLongClick = { onItemLongClick(item.id) },
                            onDone = { onDoneClick(item.id) },
                            onSnooze = { onOpenSnoozeSheet(item.id) }
                        )
                    }
                }

                // 7. Upcoming Section
                if (uiState.upcomingItems.isNotEmpty()) {
                    item(key = "upcoming_section_header") {
                        DashboardSectionHeader(
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = "Upcoming",
                                    tint = Color(0xFF8A8A82),
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            title = "Upcoming",
                            count = uiState.upcomingItems.size,
                            actionText = "View all →",
                            onActionClick = onCookedScoreClick
                        )
                    }

                    items(
                        items = uiState.upcomingItems.take(5),
                        key = { it.id }
                    ) { item ->
                        ReminderRow(
                            item = item,
                            isSelected = uiState.selectedItemIds.contains(item.id),
                            isMultiSelectMode = uiState.isMultiSelectMode,
                            hasInexactWarning = uiState.hasExactAlarmWarning,
                            onItemClick = {
                                if (uiState.isMultiSelectMode) {
                                    onItemLongClick(item.id)
                                } else {
                                    onReminderClick(item.id)
                                }
                            },
                            onLongClick = { onItemLongClick(item.id) },
                            onDone = { onDoneClick(item.id) },
                            onSnooze = { onOpenSnoozeSheet(item.id) }
                        )
                    }
                }

                // 8. Completed Section (optional peek of items completed today)
                if (uiState.completedTodayItems.isNotEmpty()) {
                    item(key = "completed_section_header") {
                        DashboardSectionHeader(
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircleOutline,
                                    contentDescription = "Completed",
                                    tint = Color(0xFF4D8950),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            title = "Completed today",
                            count = uiState.completedTodayItems.size
                        )
                    }

                    items(
                        items = uiState.completedTodayItems.take(3),
                        key = { "completed_${it.id}" }
                    ) { item ->
                        ReminderRow(
                            item = item,
                            isSelected = false,
                            isMultiSelectMode = false,
                            hasInexactWarning = false,
                            isCompletedItem = true,
                            onItemClick = { onReminderClick(item.id) },
                            onLongClick = {},
                            onDone = {},
                            onSnooze = {}
                        )
                    }
                }
            }
        }

        // Multi-select Action Bar
        AnimatedVisibility(
            visible = uiState.isMultiSelectMode,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 100.dp),
                shape = RoundedCornerShape(32.dp),
                color = CookedTheme.colors.hero,
                contentColor = CookedTheme.colors.onHero,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClearSelection) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Cancel selection",
                                tint = CookedTheme.colors.onHero
                            )
                        }
                        Text(
                            text = "${uiState.selectedItemIds.size} selected",
                            style = CookedTheme.typography.titleSmall,
                            color = CookedTheme.colors.onHero
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onBatchDone,
                            modifier = Modifier
                                .size(40.dp)
                                .background(CookedTheme.colors.accent, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Mark done",
                                tint = CookedTheme.colors.onAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenBatchSnooze,
                            modifier = Modifier
                                .size(40.dp)
                                .background(CookedTheme.colors.heroRaised, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Snooze,
                                contentDescription = "Batch snooze",
                                tint = CookedTheme.colors.onHero,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onBatchDelete,
                            modifier = Modifier
                                .size(40.dp)
                                .background(CookedTheme.colors.heroRaised, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete selected",
                                tint = CookedTheme.colors.onHero,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Single Item Snooze Sheet
        if (uiState.showSnoozeSheetForItemId != null) {
            val targetItemId = uiState.showSnoozeSheetForItemId
            SnoozeSheet(
                onDismissRequest = onDismissSnoozeSheet,
                onSnoozeSelected = { until ->
                    onSnoozeSelected(targetItemId, until)
                }
            )
        }

        // Batch Snooze Sheet
        if (uiState.showBatchSnoozeSheet) {
            SnoozeSheet(
                onDismissRequest = onDismissBatchSnooze,
                onSnoozeSelected = onBatchSnoozeSelected
            )
        }

        // Steam Puff Celebration
        SteamPuffCelebration(
            visible = uiState.showCelebration,
            message = "Not cooked today.",
            onFinished = onDismissCelebration
        )
    }
}

/**
 * Bold editorial headline with an authentic neon lime highlighter stroke
 * painted behind the designated focal phrase (e.g. "overdue.").
 */
@Composable
private fun EditorialHeadline(
    headline: String,
    highlight: String,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var highlightRect by remember(headline, highlight) { mutableStateOf<Rect?>(null) }
    val strokeHeightPx = with(density) { 10.dp.toPx() }
    val strokeRadiusPx = with(density) { 4.dp.toPx() }
    val padXPx = with(density) { 3.dp.toPx() }
    val yOffsetPx = with(density) { 1.dp.toPx() }

    Text(
        text = headline,
        style = CookedTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.5).sp
        ),
        color = CookedTheme.colors.onBackground,
        onTextLayout = { textLayoutResult ->
            if (highlight.isNotEmpty()) {
                val startIdx = headline.indexOf(highlight)
                if (startIdx >= 0) {
                    val endIdx = (startIdx + highlight.length - 1).coerceAtMost(headline.length - 1)
                    val startBox = textLayoutResult.getBoundingBox(startIdx)
                    val endBox = textLayoutResult.getBoundingBox(endIdx)
                    highlightRect = Rect(
                        left = startBox.left - padXPx,
                        top = startBox.bottom - strokeHeightPx + yOffsetPx,
                        right = endBox.right + padXPx,
                        bottom = startBox.bottom + yOffsetPx
                    )
                } else {
                    highlightRect = null
                }
            } else {
                highlightRect = null
            }
        },
        modifier = modifier.drawBehind {
            highlightRect?.let { rect ->
                drawRoundRect(
                    color = Color(0xFFC6FF3D),
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height),
                    cornerRadius = CornerRadius(strokeRadiusPx, strokeRadiusPx)
                )
            }
        }
    )
}

/**
 * Near-black card ("Today at a glance") featuring 3 metric columns
 * and a circular progress ring indicating percentage of today's tasks completed.
 */
@Composable
private fun TodayAtAGlanceCard(
    uiState: TodayUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalActiveToday = uiState.totalActiveToday
    val overdueCount = uiState.overdueItems.size
    val inNextHourCount = uiState.nowItems.size
    val completedCount = uiState.completedTodayCount

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1D2214), Color(0xFF11120F)),
                    center = Offset(800f, 100f),
                    radius = 900f
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column {
            // Header Row: "Today at a glance" + Pill status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Today at a glance",
                    style = CookedTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = Color.White
                )

                val (badgeBg, badgeTextColor, badgeText) = when {
                    overdueCount > 0 -> Triple(Color(0xFFC6FF3D), Color(0xFF11120F), "$overdueCount overdue")
                    totalActiveToday == 0 && completedCount > 0 -> Triple(Color(0xFFC6FF3D), Color(0xFF11120F), "All done")
                    totalActiveToday == 0 -> Triple(Color(0xFF22241E), Color(0xFFC6FF3D), "All clear")
                    else -> Triple(Color(0xFF22241E), Color(0xFFC6FF3D), "On track")
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeText,
                        style = CookedTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = badgeTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Metrics Columns + Circular Progress Ring Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left 3 columns
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricColumn(
                        value = totalActiveToday.toString(),
                        label = "Due today"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(34.dp)
                            .background(Color(0xFF22241E))
                    )
                    MetricColumn(
                        value = inNextHourCount.toString(),
                        label = "In next hour"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(34.dp)
                            .background(Color(0xFF22241E))
                    )
                    MetricColumn(
                        value = completedCount.toString(),
                        label = "Completed"
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Circular Progress Ring
                ProgressRing(percentage = uiState.donePercentage)
            }
        }
    }
}

@Composable
private fun MetricColumn(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = value,
            style = CookedTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                fontFeatureSettings = FontFeatureSettingsTabular
            ),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = CookedTheme.typography.bodySmall.copy(
                fontSize = 11.sp
            ),
            color = Color(0xFF9E9E94)
        )
    }
}

@Composable
private fun ProgressRing(
    percentage: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "progressRing"
    )

    Box(
        modifier = modifier.size(66.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 7.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Background track
            drawCircle(
                color = Color(0xFF23251E),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Progress arc
            if (animatedProgress > 0.005f) {
                drawArc(
                    color = Color(0xFFC6FF3D),
                    startAngle = -90f,
                    sweepAngle = (animatedProgress * 360f).coerceIn(0f, 360f),
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$percentage%",
                style = CookedTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFeatureSettings = FontFeatureSettingsTabular
                ),
                color = Color.White
            )
            Text(
                text = "done",
                style = CookedTheme.typography.labelSmall.copy(
                    fontSize = 10.sp
                ),
                color = Color(0xFF9E9E94)
            )
        }
    }
}

@Composable
private fun DashboardSectionHeader(
    icon: @Composable () -> Unit,
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon()
            Text(
                text = title,
                style = CookedTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = CookedTheme.colors.onBackground
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (CookedTheme.colors.isDark) Color(0xFF22241E) else Color(0xFFE5E3D8))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    style = CookedTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        fontFeatureSettings = FontFeatureSettingsTabular
                    ),
                    color = if (CookedTheme.colors.isDark) Color(0xFFC6FF3D) else Color(0xFF55574F)
                )
            }
        }

        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = CookedTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = CookedTheme.colors.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}
