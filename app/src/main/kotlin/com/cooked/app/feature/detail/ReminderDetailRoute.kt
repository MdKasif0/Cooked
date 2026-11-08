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
    onStartCookSession: ((String) -> Unit)? = null,
    onTriggerRescuePlan: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: ReminderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ReminderDetailScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onTitleChanged = viewModel::onTitleChanged,
        onEmojiSelected = viewModel::onEmojiSelected,
        onDueAtChanged = viewModel::onDueAtChanged,
        onRecurrenceChanged = viewModel::onRecurrenceChanged,
        onNagIntervalChanged = viewModel::onNagIntervalChanged,
        onEscalateToggle = viewModel::onEscalateToggle,
        onCriticalToggle = viewModel::onCriticalToggle,
        onNoteChanged = viewModel::onNoteChanged,
        onProgressChanged = viewModel::onProgressChanged,
        onEffortSelected = viewModel::onEffortSelected,
        onToggleChecklistStep = viewModel::onToggleChecklistStep,
        onAddChecklistStep = viewModel::onAddChecklistStep,
        onDeleteChecklistStep = viewModel::onDeleteChecklistStep,
        onPushDeadline = viewModel::onPushDeadline,
        onShowRescheduleSheet = viewModel::setShowRescheduleSheet,
        onShowEmojiPicker = viewModel::setShowEmojiPicker,
        onShowDatePicker = viewModel::setShowDatePicker,
        onMarkDone = viewModel::onMarkDone,
        onDelete = viewModel::onDelete,
        onStartCookSession = onStartCookSession?.let { action -> { uiState.item?.id?.let { action(it) } } },
        onTriggerRescuePlan = onTriggerRescuePlan?.let { action -> { uiState.item?.id?.let { action(it) } } },
        modifier = modifier
    )
}
