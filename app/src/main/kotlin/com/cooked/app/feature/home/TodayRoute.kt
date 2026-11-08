package com.cooked.app.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TodayRoute(
    onReminderClick: (String) -> Unit,
    onCookedScoreClick: () -> Unit,
    onNavigateToReliability: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TodayScreen(
        uiState = uiState,
        onReminderClick = onReminderClick,
        onDoneClick = viewModel::onDone,
        onOpenSnoozeSheet = viewModel::openSnoozeSheet,
        onSnoozeSelected = viewModel::onSnooze,
        onDismissSnoozeSheet = viewModel::dismissSnoozeSheet,
        onItemLongClick = viewModel::toggleSelection,
        onBatchDone = viewModel::onBatchDone,
        onOpenBatchSnooze = viewModel::openBatchSnoozeSheet,
        onBatchSnoozeSelected = viewModel::onBatchSnooze,
        onDismissBatchSnooze = viewModel::dismissBatchSnoozeSheet,
        onBatchDelete = viewModel::onBatchDelete,
        onClearSelection = viewModel::clearSelection,
        onCookedScoreClick = onCookedScoreClick,
        onNavigateToReliability = onNavigateToReliability,
        onDismissCelebration = viewModel::dismissCelebration,
        modifier = modifier
    )
}
