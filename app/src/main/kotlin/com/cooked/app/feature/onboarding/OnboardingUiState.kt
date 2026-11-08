package com.cooked.app.feature.onboarding

data class OnboardingUiState(
    val currentStep: Int = 0,
    val isComplete: Boolean = false
)
