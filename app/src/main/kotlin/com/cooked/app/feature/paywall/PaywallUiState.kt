package com.cooked.app.feature.paywall

import androidx.annotation.DrawableRes
import com.cooked.app.R
import com.cooked.app.domain.billing.SubscriptionPlan

data class PaywallBenefit(
    val id: String,
    val iconEmoji: String = "",
    @DrawableRes val iconRes: Int = R.drawable.ic_premium_crown,
    val title: String,
    val description: String,
    val isHighlighted: Boolean = false
)

data class PaywallUiState(
    val headline: String = "Never be cooked again.",
    val subheadline: String = "Everything in Free forever, plus full-screen alarm escalation, deep insights, and live countdown widgets.",
    val benefits: List<PaywallBenefit> = emptyList(),
    val availablePlans: List<SubscriptionPlan> = emptyList(),
    val selectedPlanId: String = "",
    val isPurchasing: Boolean = false,
    val isRestoring: Boolean = false,
    val isPro: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showTermsDialog: Boolean = false,
    val showPrivacyDialog: Boolean = false
)
