package com.cooked.app.feature.home

import com.cooked.app.core.designsystem.components.AccentBadge
import com.cooked.app.core.designsystem.components.HeroCard
import com.cooked.app.core.designsystem.components.Overline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Snooze
import com.cooked.app.core.designsystem.components.CookedAppLogo
import com.cooked.app.core.designsystem.components.EggMascot
import com.cooked.app.core.designsystem.components.ShimmerPlaceholder
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.R
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.designsystem.components.CountdownText
import com.cooked.app.core.designsystem.components.HeatChip
import com.cooked.app.core.designsystem.components.SectionHeader
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
    onNavigateToReliability: () -> Unit = {},
    onDismissCelebration: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isTomorrowExpanded by remember { mutableStateOf(false) }

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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CookedTheme.spacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 132.dp)
            ) {
                // Editorial greeting
                item(key = "today_header") {
                    val hour = remember { java.time.LocalTime.now().hour }
                    val greeting = when (hour) {
                        in 5..11 -> "Good morning"
                        in 12..16 -> "Good afternoon"
                        in 17..21 -> "Good evening"
                        else -> "Late night"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Overline(text = "$greeting · ${uiState.dateTitle}")
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = uiState.voiceStatus,
                                style = CookedTheme.typography.headlineLarge,
                                color = CookedTheme.colors.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        EggMascot(
                            level = uiState.hottestDeadlines.firstOrNull()?.second ?: HeatLevel.CHILL,
                            size = 56.dp
                        )
                    }
                }

                // Day-at-a-glance hero (ink + neon)
                item(key = "today_hero") {
                    TodayHeroCard(
                        uiState = uiState,
                        onClick = onCookedScoreClick
                    )
                }

                // Gentle reliability notice
                if (uiState.hasExactAlarmWarning) {
                    item(key = "reliability_warning_banner") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(28.dp))
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

                // Empty State when no items due
                if (uiState.isEmpty) {
                    item(key = "empty_state") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp, bottom = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
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

                // 1. Overdue Section (Heat: Cooked)
                if (uiState.overdueItems.isNotEmpty()) {
                    item(key = "overdue_header") {
                        SectionHeader(
                            title = "Overdue",
                            count = uiState.overdueItems.size,
                            modifier = Modifier.padding(top = 10.dp)
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

                // 2. Now Section (due within 1h)
                if (uiState.nowItems.isNotEmpty()) {
                    item(key = "now_header") {
                        SectionHeader(
                            title = "Due now",
                            count = uiState.nowItems.size,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    items(
                        items = uiState.nowItems,
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

                // 3. Later Today Section
                if (uiState.laterTodayItems.isNotEmpty()) {
                    item(key = "later_today_header") {
                        SectionHeader(
                            title = "Later today",
                            count = uiState.laterTodayItems.size,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    items(
                        items = uiState.laterTodayItems,
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

                // 4. Tomorrow Peek Section (Collapsible, Max 3 items)
                if (uiState.tomorrowPeekItems.isNotEmpty()) {
                    item(key = "tomorrow_peek_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isTomorrowExpanded = !isTomorrowExpanded }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tomorrow · ${uiState.tomorrowPeekItems.size}",
                                style = CookedTheme.typography.titleMedium,
                                color = CookedTheme.colors.onSurfaceVariant
                            )
                            Icon(
                                imageVector = if (isTomorrowExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = if (isTomorrowExpanded) "Collapse" else "Expand",
                                tint = CookedTheme.colors.onSurfaceVariant
                            )
                        }
                    }

                    if (isTomorrowExpanded) {
                        items(
                            items = uiState.tomorrowPeekItems,
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
                }
            }
        }

        // Multi-select Action Bar (Floats at bottom when in multi-select mode)
        AnimatedVisibility(
            visible = uiState.isMultiSelectMode,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 110.dp),
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
                        // Batch Done
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

                        // Batch Snooze
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

                        // Batch Delete
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

        // Confetti-free steam puff celebration on completing a hot/toasty/cooked item (<800ms)
        com.cooked.app.core.designsystem.components.SteamPuffCelebration(
            visible = uiState.showCelebration,
            message = "Not cooked today.",
            onFinished = onDismissCelebration
        )
    }
}


/**
 * Ink hero summarizing the day: big count, a time-of-day distribution in neon bars,
 * and the hottest deadline with a live countdown.
 */
@Composable
private fun TodayHeroCard(
    uiState: TodayUiState,
    onClick: () -> Unit
) {
    val colors = CookedTheme.colors
    val todayItems = uiState.overdueItems + uiState.nowItems + uiState.laterTodayItems
    val total = todayItems.size
    val zone = java.time.ZoneId.systemDefault()
    // Six 4-hour buckets across the day
    val buckets = remember(todayItems) {
        val counts = FloatArray(6)
        todayItems.forEach { item ->
            val h = item.dueAt.atZone(zone).hour
            counts[(h / 4).coerceIn(0, 5)] += 1f
        }
        counts.map { it + 0.35f }
    }
    val currentBucket = (java.time.LocalTime.now().hour / 4).coerceIn(0, 5)

    HeroCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Overline(text = "Today at a glance", color = colors.onHeroMuted, modifier = Modifier.weight(1f))
            AccentBadge(
                text = when {
                    uiState.overdueItems.isNotEmpty() -> "${uiState.overdueItems.size} overdue"
                    total == 0 -> "All clear"
                    else -> "On track"
                }
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = total.toString(),
                style = CookedTheme.typography.displayLarge,
                color = colors.onHero
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f).padding(bottom = 8.dp)) {
                Text(
                    text = if (total == 1) "thing due" else "things due",
                    style = CookedTheme.typography.titleMedium,
                    color = colors.onHero
                )
                Text(
                    text = "${uiState.nowItems.size} in the next hour",
                    style = CookedTheme.typography.bodySmall,
                    color = colors.onHeroMuted
                )
            }
            Row(
                modifier = Modifier.height(56.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val max = (buckets.maxOrNull() ?: 1f).coerceAtLeast(1f)
                buckets.forEachIndexed { index, v ->
                    Box(
                        modifier = Modifier
                            .width(9.dp)
                            .height(56.dp * (v / max).coerceIn(0.14f, 1f))
                            .clip(RoundedCornerShape(50))
                            .background(if (index == currentBucket) colors.accent else colors.heroRaised)
                    )
                }
            }
        }

        val hottest = uiState.hottestDeadlines.firstOrNull()
        if (hottest != null) {
            val (deadline, heat) = hottest
            Spacer(Modifier.height(18.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.heroRaised)
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.heroRaised),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = colors.heatFor(heat).color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deadline.title,
                        style = CookedTheme.typography.titleSmall,
                        color = colors.onHero,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Hottest deadline · ${heat.displayName}",
                        style = CookedTheme.typography.bodySmall,
                        color = colors.onHeroMuted
                    )
                }
                Spacer(Modifier.width(8.dp))
                CountdownText(
                    targetInstant = deadline.dueAt,
                    style = CookedTheme.typography.titleLarge,
                    color = colors.accent
                )
            }
        }
    }
}
