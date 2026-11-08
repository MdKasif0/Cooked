package com.cooked.app.feature.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ReminderDetailRoute(
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReminderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ReminderDetailScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onProgressChanged = viewModel::onProgressChanged,
        onEffortSelected = viewModel::onEffortSelected,
        onToggleChecklistStep = viewModel::onToggleChecklistStep,
        onAddChecklistStep = viewModel::onAddChecklistStep,
        onDeleteChecklistStep = viewModel::onDeleteChecklistStep,
        onPushDeadline = viewModel::onPushDeadline,
        onShowRescheduleSheet = viewModel::setShowRescheduleSheet,
        onMarkDone = viewModel::onMarkDone,
        onDelete = viewModel::onDelete,
        modifier = modifier
    )
}
