package com.cooked.app.feature.onboarding

import com.cooked.app.domain.billing.SubscriptionPlan
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.Personality

data class OnboardingUiState(
    val currentStep: Int = 0,
    val totalSteps: Int = 4,
    val selectedPersonality: Personality = Personality.SPICY,
    val firstWinInput: String = "",
    val firstWinCreated: Boolean = false,
    val isSavingFirstWin: Boolean = false,
    val firstWinError: String? = null,
    val createdItem: Item? = null,
    val notificationsGranted: Boolean = false,
    val exactAlarmsGranted: Boolean = false,
    val batteryUnrestricted: Boolean = false,
    val isComplete: Boolean = false,
    // Cooked Pro Paywall State
    val availablePlans: List<SubscriptionPlan> = emptyList(),
    val selectedPlanId: String = "",
    val isPurchasing: Boolean = false,
    val isRestoring: Boolean = false,
    val isPro: Boolean = false,
    val billingErrorMessage: String? = null,
    val billingSuccessMessage: String? = null,
    val showTermsDialog: Boolean = false,
    val showPrivacyDialog: Boolean = false
) {
    val isLastStep: Boolean
        get() = currentStep == totalSteps - 1
}
