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
        onEditClick = onEditClick,
        onDoneClick = viewModel::onDoneClick,
        onSnoozeClick = viewModel::onSnoozeClick,
        onDeleteClick = viewModel::onDeleteClick,
        modifier = modifier
    )
}
