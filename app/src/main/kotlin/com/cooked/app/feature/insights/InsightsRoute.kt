package com.cooked.app.feature.insights

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun InsightsRoute(
    onCookedScoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    InsightsScreen(
        uiState = uiState,
        onCookedScoreClick = onCookedScoreClick,
        modifier = modifier
    )
}
