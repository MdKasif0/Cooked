package com.cooked.app.feature.deadlines

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DeadlinesRoute(
    onDeadlineClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeadlinesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DeadlinesScreen(
        uiState = uiState,
        onDeadlineClick = onDeadlineClick,
        onDoneClick = viewModel::onDoneClick,
        modifier = modifier
    )
}
