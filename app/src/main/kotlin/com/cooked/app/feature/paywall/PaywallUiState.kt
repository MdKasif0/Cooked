package com.cooked.app.feature.paywall

data class PaywallUiState(
    val title: String = "Cooked Pro 🔥",
    val description: String = "Unlock unlimited deadline items, custom nag sounds, and deep stats.",
    val price: String = "$1.99 / month",
    val isPurchasing: Boolean = false
)

