package com.cooked.app.feature.deadlines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import com.cooked.app.core.config.FeatureFlags
import com.cooked.app.core.designsystem.components.ShimmerPlaceholder
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.components.CookedCard
import com.cooked.app.core.designsystem.components.EmptyState
import com.cooked.app.core.designsystem.components.SectionHeader
import com.cooked.app.feature.deadlines.components.CookedScoreGauge
import com.cooked.app.feature.deadlines.components.DeadlineCard
import com.cooked.app.feature.deadlines.components.WorkloadWarningBanner

@Composable
fun DeadlinesScreen(
    uiState: DeadlinesUiState,
    onDeadlineClick: (String) -> Unit,
    onImOnItClick: (String) -> Unit,
    onDoneClick: (String) -> Unit,
    onQuickAddClick: () -> Unit = {},
    onScanDatesheetClick: (() -> Unit)? = null,
    onTriggerRescuePlan: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = CookedTheme.spacing.screenHorizontal, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(160.dp))
            Spacer(modifier = Modifier.height(8.dp))
            repeat(3) {
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(90.dp))
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = CookedTheme.spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Hero Card: Cooked Score Semicircle Gauge
        if (uiState.scoreResult != null) {
            item(key = "hero_gauge") {
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    CookedScoreGauge(
                        scoreResult = uiState.scoreResult,
                        onSuggestedActionClick = {
                            val topItem = uiState.scoreResult.topContributors.firstOrNull()?.item
                            if (topItem != null) {
                                onDeadlineClick(topItem.id)
                            }
                        }
                    )
                }
            }

            // Workload Warning Banner when load > 1.0
            if (!uiState.scoreResult.workloadWarning.isNullOrBlank()) {
                item(key = "workload_warning") {
                    WorkloadWarningBanner(
                        warningMessage = uiState.scoreResult.workloadWarning,
                        onStartOneClick = {
                            val topItem = uiState.scoreResult.topContributors.firstOrNull()?.item
                            if (topItem != null) {
                                onImOnItClick(topItem.id)
                            }
                        },
                        onPushDeadlineClick = {
                            val topItem = uiState.scoreResult.topContributors.firstOrNull()?.item
                            if (topItem != null) {
                                onDeadlineClick(topItem.id)
                            }
                        },
                        onLowerEffortClick = {
                            val topItem = uiState.scoreResult.topContributors.firstOrNull()?.item
                            if (topItem != null) {
                                onDeadlineClick(topItem.id)
                            }
                        },
                        onSaveMeClick = if (FeatureFlags.isPanicButtonEnabled && onTriggerRescuePlan != null) {
                            {
                                val topItem = uiState.scoreResult.topContributors.firstOrNull()?.item
                                if (topItem != null) {
                                    onTriggerRescuePlan(topItem.id)
                                }
                            }
                        } else null
                    )
                }
            }
        }

        // Deadlines List Header
        item(key = "deadlines_header") {
            SectionHeader(
                title = if (uiState.deadlines.isNotEmpty())
                    "Deadlines by Urgency (${uiState.deadlines.size})"
                else
                    "Deadlines"
            )
        }

        // Empty State or Deadline Cards
        if (uiState.deadlines.isEmpty()) {
            item(key = "empty_state") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EmptyState(
                        title = "All clear!",
                        message = uiState.emptyMessage
                    )
                    if (FeatureFlags.isDatesheetScanEnabled && onScanDatesheetClick != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = onScanDatesheetClick,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan datesheet or timetable 📷", style = CookedTheme.typography.labelLarge)
                        }
                    }
                }
            }
        } else {
            items(
                items = uiState.deadlines,
                key = { it.item.id }
            ) { cardModel ->
                DeadlineCard(
                    item = cardModel.item,
                    heatResult = cardModel.heatResult,
                    startByInstant = cardModel.suggestedStartBy,
                    onCardClick = { onDeadlineClick(cardModel.item.id) },
                    onImOnItClick = { onImOnItClick(cardModel.item.id) },
                    onDoneClick = { onDoneClick(cardModel.item.id) }
                )
            }
        }
    }
}
