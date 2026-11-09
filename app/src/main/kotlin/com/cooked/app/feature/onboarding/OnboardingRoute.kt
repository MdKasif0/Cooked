package com.cooked.app.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OnboardingRoute(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScreen(
        uiState = uiState,
        onNext = viewModel::onNext,
        onSkip = viewModel::onSkip,
        onPersonalitySelected = viewModel::onPersonalitySelected,
        onFirstWinInputChanged = viewModel::onFirstWinInputChanged,
        onSaveFirstWin = viewModel::saveFirstWin,
        onRefreshPermissions = viewModel::refreshPermissions,
        onPlanSelected = viewModel::onPlanSelected,
        onPurchase = viewModel::purchaseSelectedPlan,
        onRestore = viewModel::restorePurchases,
        onClearBillingMessages = viewModel::clearBillingMessages,
        onSetTermsVisible = viewModel::setTermsVisible,
        onSetPrivacyVisible = viewModel::setPrivacyVisible,
        onComplete = onComplete,
        modifier = modifier
    )
}
