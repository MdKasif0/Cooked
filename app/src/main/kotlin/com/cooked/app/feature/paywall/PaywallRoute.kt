package com.cooked.app.feature.paywall

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PaywallRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PaywallScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onPlanSelected = viewModel::onPlanSelected,
        onPurchase = viewModel::onPurchase,
        onRestore = viewModel::onRestorePurchases,
        onClearMessages = viewModel::clearMessages,
        onSetTermsVisible = viewModel::setTermsDialogVisible,
        onSetPrivacyVisible = viewModel::setPrivacyDialogVisible,
        onClaimRewardedProPass = viewModel::onClaimRewardedProPass,
        modifier = modifier
    )
}
