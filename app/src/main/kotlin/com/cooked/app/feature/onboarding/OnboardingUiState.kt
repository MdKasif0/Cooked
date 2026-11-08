package com.cooked.app.feature.onboarding

import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.Personality

data class OnboardingUiState(
    val currentStep: Int = 0,
    val totalSteps: Int = 5,
    val selectedPersonality: Personality = Personality.SPICY,
    val firstWinInput: String = "",
    val firstWinCreated: Boolean = false,
    val createdItem: Item? = null,
    val notificationsGranted: Boolean = false,
    val exactAlarmsGranted: Boolean = false,
    val batteryUnrestricted: Boolean = false,
    val isComplete: Boolean = false
) {
    val isLastStep: Boolean
        get() = currentStep == totalSteps - 1
}
