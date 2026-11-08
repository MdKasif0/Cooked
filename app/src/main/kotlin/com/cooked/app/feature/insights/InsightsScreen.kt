package com.cooked.app.feature.insights

import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.horizontalScroll

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.cooked.app.core.designsystem.components.ShimmerPlaceholder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.designsystem.components.CookedCard
import com.cooked.app.core.designsystem.components.SectionHeader
import com.cooked.app.domain.insights.model.DayCompletion
import com.cooked.app.domain.insights.model.InsightRange
import com.cooked.app.feature.insights.components.CompletionHeatGrid
import com.cooked.app.feature.insights.components.CookedReportDialog
import com.cooked.app.feature.insights.components.CookedScoreHistoryChart
import com.cooked.app.feature.insights.components.DayCompletionsBottomSheet
import com.cooked.app.feature.insights.components.InsightCard
import com.cooked.app.feature.insights.components.SnoozeHabitChart
import com.cooked.app.feature.insights.components.StatTilesRow

@Composable
fun InsightsScreen(
    uiState: InsightsUiState,
    onRangeSelected: (InsightRange) -> Unit,
    onSelectDayCompletion: (DayCompletion?) -> Unit,
    onShowCookedReport: (Boolean) -> Unit,
    onNavigateToPaywall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading || uiState.summary == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CookedTheme.spacing.screenHorizontal, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.5f).height(32.dp))
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                repeat(3) {
                    ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(110.dp))
                }
            }
            return
        }

        val summary = uiState.summary

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CookedTheme.spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 132.dp)
        ) {
            // Header Title
            item(key = "header_title") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 8.dp)) {
                    com.cooked.app.core.designsystem.components.Overline(text = "Rhythm, habits & capacity")
                    Text(
                        text = "How you've been cooking",
                        style = CookedTheme.typography.headlineLarge,
                        color = CookedTheme.colors.onBackground
                    )
                }
            }

            // 1. Range chips (7d / 30d / 90d / All)
            item(key = "range_selector") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InsightRange.entries.forEach { range ->
                        val isSelected = uiState.selectedRange == range
                        com.cooked.app.core.designsystem.components.PillChip(
                            text = range.label,
                            selected = isSelected,
                            onClick = { onRangeSelected(range) },
                            trailing = if (range.isPro && !uiState.isProUser) {
                                {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = "Pro",
                                        tint = if (isSelected) CookedTheme.colors.onPrimary else CookedTheme.colors.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }

            // Soft-lock Container: If user selected Pro range and is not Pro
            if (uiState.isSoftLocked) {
                item(key = "soft_lock_banner") {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = CookedTheme.colors.accentContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = CookedTheme.colors.onAccentContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${uiState.selectedRange.label} history is Pro",
                                        style = CookedTheme.typography.titleSmall,
                                        color = CookedTheme.colors.onAccentContainer
                                    )
                                    Text(
                                        text = "Previewing your last 7 days",
                                        style = CookedTheme.typography.bodySmall,
                                        color = CookedTheme.colors.onAccentContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Button(
                                onClick = onNavigateToPaywall,
                                shape = com.cooked.app.core.designsystem.PillShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CookedTheme.colors.hero,
                                    contentColor = CookedTheme.colors.accent
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Unlock", style = CookedTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                            }
                        }
                    }
                }
            }

            // 2. Hero Row: 3 StatTiles
            item(key = "hero_stat_tiles") {
                StatTilesRow(
                    onTimeRate = summary.onTimeRate,
                    onTimeDelta = summary.onTimeDelta,
                    doneCount = summary.doneCount,
                    doneDelta = summary.doneDelta,
                    nagsPerWin = summary.nagsPerWin,
                    nagsDelta = summary.nagsDelta
                )
            }

            // 3. Cooked Score 12-Week Line Chart
            item(key = "cooked_score_chart") {
                CookedScoreHistoryChart(points = summary.cookedScoreHistory)
            }

            // 4. Completion Heat-Grid (7 rows x 13 weeks)
            item(key = "completion_heat_grid") {
                CompletionHeatGrid(
                    days = summary.doneByDay,
                    onDaySelected = onSelectDayCompletion
                )
            }

            // 5. When You Snooze Chart (24-Hour)
            item(key = "snooze_habit_chart") {
                SnoozeHabitChart(snoozeHabit = summary.snoozeHabit)
            }

            // 6. Insight Cards (Up to 3 Generated Insights)
            if (summary.insights.isNotEmpty()) {
                item(key = "insights_header") {
                    SectionHeader(title = "Patterns & habits", subtitle = "What your data says about you")
                }

                items(
                    items = summary.insights,
                    key = { it.id }
                ) { insight ->
                    InsightCard(insight = insight)
                }
            }

            // 7. "Your Cooked Report" Weekly Recap Entry Point
            item(key = "cooked_report_card") {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = CookedTheme.colors.accent,
                    contentColor = CookedTheme.colors.onAccent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShowCookedReport(true) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(CookedTheme.colors.hero, androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = CookedTheme.colors.accent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Your Cooked Report",
                                    style = CookedTheme.typography.titleLarge,
                                    color = CookedTheme.colors.onAccent
                                )
                                Text(
                                    text = "Your week as a shareable story",
                                    style = CookedTheme.typography.bodySmall,
                                    color = CookedTheme.colors.onAccent.copy(alpha = 0.72f)
                                )
                            }
                        }

                        Text(
                            text = "View →",
                            style = CookedTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = CookedTheme.colors.onAccent
                        )
                    }
                }
            }
        }

        // Day Completions Bottom Sheet
        if (uiState.selectedDayCompletion != null) {
            DayCompletionsBottomSheet(
                dayCompletion = uiState.selectedDayCompletion,
                onDismissRequest = { onSelectDayCompletion(null) }
            )
        }

        // Full Screen Story Cooked Report Dialog
        if (uiState.showCookedReportDialog && summary != null) {
            CookedReportDialog(
                summary = summary,
                onDismissRequest = { onShowCookedReport(false) }
            )
        }
    }
}
