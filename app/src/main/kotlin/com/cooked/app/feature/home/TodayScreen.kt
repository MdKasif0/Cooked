package com.cooked.app.feature.home

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
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
            ) {
                // Large Collapsing Title Header with Date, Voice Status & Egg Mascot
                item(key = "today_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = uiState.dateTitle,
                                style = CookedTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = CookedTheme.colors.onBackground
                            )
                            Text(
                                text = uiState.voiceStatus,
                                style = CookedTheme.typography.bodyLarge,
                                color = CookedTheme.colors.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        EggMascot(
                            level = uiState.hottestDeadlines.firstOrNull()?.second ?: HeatLevel.CHILL,
                            size = 52.dp
                        )
                    }
                }

                // Gentle Reliability Warning Banner
                if (uiState.hasExactAlarmWarning) {
                    item(key = "reliability_warning_banner") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToReliability() },
                            shape = RoundedCornerShape(12.dp),
                            color = CookedTheme.colors.primaryContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CookedTheme.colors.primary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_emergency_siren),
                                    contentDescription = null,
                                    tint = CookedTheme.colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Cooked might stay silent. Tap to fix exact alarms & battery settings.",
                                    style = CookedTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = CookedTheme.colors.onPrimaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Fix →",
                                    style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CookedTheme.colors.primary
                                )
                            }
                        }
                    }
                }

                // Compact Strip of Top 2 Hottest Deadlines
                if (uiState.hottestDeadlines.isNotEmpty()) {
                    item(key = "hottest_deadlines_strip") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CookedTheme.colors.surfaceContainer)
                                .clickable { onCookedScoreClick() }
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = CookedTheme.colors.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Top Heat Deadlines",
                                        style = CookedTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CookedTheme.colors.onSurface
                                    )
                                }
                                Text(
                                    text = "View All →",
                                    style = CookedTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = CookedTheme.colors.primary
                                )
                            }

                            uiState.hottestDeadlines.forEach { (deadline, heat) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CookedTheme.colors.surfaceContainerLow)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = deadline.title,
                                        style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = CookedTheme.colors.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    CountdownText(
                                        targetInstant = deadline.dueAt,
                                        style = CookedTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = CookedTheme.colors.heatFor(heat).color
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    HeatChip(level = heat)
                                }
                            }
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
                            title = "Overdue (${uiState.overdueItems.size})",
                            modifier = Modifier.padding(top = 8.dp)
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
                            title = "Due Now (${uiState.nowItems.size})",
                            modifier = Modifier.padding(top = 8.dp)
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
                            title = "Later Today (${uiState.laterTodayItems.size})",
                            modifier = Modifier.padding(top = 8.dp)
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
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isTomorrowExpanded = !isTomorrowExpanded }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tomorrow Peek (${uiState.tomorrowPeekItems.size})",
                                style = CookedTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
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
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = CookedTheme.colors.surfaceContainerHigh,
                tonalElevation = 6.dp
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
                                tint = CookedTheme.colors.onSurface
                            )
                        }
                        Text(
                            text = "${uiState.selectedItemIds.size} selected",
                            style = CookedTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = CookedTheme.colors.onSurface
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Batch Done
                        IconButton(
                            onClick = onBatchDone,
                            modifier = Modifier
                                .size(40.dp)
                                .background(CookedTheme.colors.heatFor(HeatLevel.CHILL).color, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Mark done",
                                tint = CookedTheme.colors.heatFor(HeatLevel.CHILL).onColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Batch Snooze
                        IconButton(
                            onClick = onOpenBatchSnooze,
                            modifier = Modifier
                                .size(40.dp)
                                .background(CookedTheme.colors.heatFor(HeatLevel.WARM).color, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Snooze,
                                contentDescription = "Batch snooze",
                                tint = CookedTheme.colors.heatFor(HeatLevel.WARM).onColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Batch Delete
                        IconButton(
                            onClick = onBatchDelete,
                            modifier = Modifier
                                .size(40.dp)
                                .background(CookedTheme.colors.surfaceContainer, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete selected",
                                tint = CookedTheme.colors.onSurfaceVariant,
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
