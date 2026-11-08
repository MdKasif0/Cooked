package com.cooked.app.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToReliability: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    SettingsScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToReliability = onNavigateToReliability,
        onNavigateToPaywall = onNavigateToPaywall,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onDynamicColorToggled = viewModel::onDynamicColorToggled,
        onNagIntervalSelected = viewModel::onNagIntervalSelected,
        onPersonalitySelected = viewModel::onPersonalitySelected,
        onHinglishVibesToggled = viewModel::onHinglishVibesToggled,
        onInAppSoundsToggled = viewModel::onInAppSoundsToggled,
        onAppIconSelected = { style -> viewModel.onAppIconSelected(context, style) },
        onPreviewSound = viewModel::previewSound,
        onDailyCapacityChanged = viewModel::onDailyCapacityChanged,
        onWeekStartsOnChanged = viewModel::onWeekStartsOnChanged,
        onExportBackup = viewModel::exportBackup,
        onImportContent = viewModel::onImportContentReceived,
        onConfirmImport = viewModel::confirmImport,
        onDismissImportPreview = viewModel::dismissImportPreview,
        onRequestDeleteAll = viewModel::requestDeleteAllData,
        onConfirmFirstDelete = viewModel::confirmFirstDelete,
        onConfirmFinalDelete = viewModel::confirmFinalDelete,
        onDismissDeleteDialogs = viewModel::dismissDeleteDialogs,
        onTelemetryToggled = viewModel::onTelemetryToggled,
        onSetPrivacyVisible = viewModel::setPrivacyDialogVisible,
        onClearSnackbar = viewModel::clearSnackbar,
        modifier = modifier
    )
}
