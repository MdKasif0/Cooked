package com.cooked.app.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToOemGuide: () -> Unit,
    onNavigateToDesignGallery: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToOemGuide = onNavigateToOemGuide,
        onNavigateToDesignGallery = onNavigateToDesignGallery,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onNagIntervalSelected = viewModel::onNagIntervalSelected,
        onPersonalitySelected = viewModel::onPersonalitySelected,
        onFireAlarmNow = viewModel::fireAlarmNow,
        onRefreshAlarms = viewModel::refreshAlarms,
        modifier = modifier
    )
}
