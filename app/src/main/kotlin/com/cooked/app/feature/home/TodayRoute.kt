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
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TodayScreen(
        uiState = uiState,
        onReminderClick = onReminderClick,
        onDoneClick = viewModel::onDoneClick,
        onSnoozeClick = viewModel::onSnoozeClick,
        onCookedScoreClick = onCookedScoreClick,
        modifier = modifier
    )
}
