package com.cooked.app.feature.onboarding

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.domain.model.Personality
import com.cooked.app.feature.onboarding.components.CookedProOnboardingContent
import com.cooked.app.feature.onboarding.components.MakeItReliableContent
import com.cooked.app.feature.onboarding.components.PickYourVibeContent
import com.cooked.app.feature.onboarding.components.QuickCaptureContent

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onPersonalitySelected: (Personality) -> Unit,
    onFirstWinInputChanged: (String) -> Unit,
    onSaveFirstWin: (String) -> Unit,
    onRefreshPermissions: () -> Unit,
    onPlanSelected: (String) -> Unit = {},
    onPurchase: (Activity) -> Unit = {},
    onRestore: () -> Unit = {},
    onClearBillingMessages: () -> Unit = {},
    onSetTermsVisible: (Boolean) -> Unit = {},
    onSetPrivacyVisible: (Boolean) -> Unit = {},
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onComplete()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CookedTheme.colors.background)
    ) {
        when (uiState.currentStep) {
            0 -> PickYourVibeContent(
                selectedPersonality = uiState.selectedPersonality,
                onSelectPersonality = onPersonalitySelected,
                onNext = onNext
            )
            1 -> QuickCaptureContent(
                input = uiState.firstWinInput,
                isCreated = uiState.firstWinCreated,
                isSaving = uiState.isSavingFirstWin,
                errorMessage = uiState.firstWinError,
                onInputChanged = onFirstWinInputChanged,
                onSaveAndNext = { text ->
                    onSaveFirstWin(text)
                    onNext()
                },
                onSkipLater = onNext
            )
            2 -> MakeItReliableContent(
                notificationsGranted = uiState.notificationsGranted,
                exactAlarmsGranted = uiState.exactAlarmsGranted,
                batteryUnrestricted = uiState.batteryUnrestricted,
                onRefreshPermissions = onRefreshPermissions,
                onNext = onNext
            )
            3 -> CookedProOnboardingContent(
                availablePlans = uiState.availablePlans,
                selectedPlanId = uiState.selectedPlanId,
                isPurchasing = uiState.isPurchasing,
                isRestoring = uiState.isRestoring,
                isPro = uiState.isPro,
                errorMessage = uiState.billingErrorMessage,
                successMessage = uiState.billingSuccessMessage,
                showTermsDialog = uiState.showTermsDialog,
                showPrivacyDialog = uiState.showPrivacyDialog,
                onPlanSelected = onPlanSelected,
                onPurchase = onPurchase,
                onRestore = onRestore,
                onClearMessages = onClearBillingMessages,
                onSetTermsVisible = onSetTermsVisible,
                onSetPrivacyVisible = onSetPrivacyVisible,
                onMaybeLater = onNext
            )
        }
    }
}
