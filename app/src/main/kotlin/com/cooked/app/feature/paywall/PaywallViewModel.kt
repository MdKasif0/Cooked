package com.cooked.app.feature.paywall

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.R
import com.cooked.app.core.ads.CookedAdManager
import com.cooked.app.core.telemetry.Telemetry
import com.cooked.app.data.billing.RevenueCatBillingManager
import com.cooked.app.domain.billing.BillingManager
import com.cooked.app.domain.billing.EntitlementRepository
import com.cooked.app.domain.billing.PurchaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val entitlementRepository: EntitlementRepository,
    private val adManager: CookedAdManager,
    private val telemetry: Telemetry,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val entryPoint: String = savedStateHandle.get<String>("entryPoint") ?: "generic"
    private val featureHint: String? = savedStateHandle.get<String>("featureHint")

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        telemetry.logPaywallView(entryPoint)
        initializeBenefits()
        observeEntitlements()
        loadPlans()
    }

    private fun initializeBenefits() {
        val benefits = listOf(
            PaywallBenefit(
                id = "critical_alarm",
                iconRes = R.drawable.ic_emergency_siren,
                title = "Critical Alarm Alerts",
                description = "Full-screen waking alarms that bypass silent mode until explicitly acknowledged.",
                isHighlighted = featureHint == "critical_alarm"
            ),
            PaywallBenefit(
                id = "custom_nag",
                iconRes = R.drawable.ic_cat_bell,
                title = "Custom Nag Intervals & Rules",
                description = "Set per-item 1m to 60m custom nagging frequency and max-nag safety limits.",
                isHighlighted = featureHint == "custom_nag"
            ),
            PaywallBenefit(
                id = "insights_range",
                iconRes = R.drawable.ic_chart_analytics,
                title = "Deep Insights & All History",
                description = "Unlock 30d/90d/All productivity charts, snooze heatmaps, and latency trends.",
                isHighlighted = featureHint == "insights_range"
            ),
            PaywallBenefit(
                id = "live_widgets",
                iconRes = R.drawable.ic_cat_routine,
                title = "Countdown & Score Widgets",
                description = "Live urgency countdowns and Cooked Score gauges directly on your home screen.",
                isHighlighted = featureHint == "live_widgets"
            )
        )
        _uiState.update { it.copy(benefits = benefits) }
    }

    private fun observeEntitlements() {
        viewModelScope.launch {
            entitlementRepository.isPro.collect { isPro ->
                _uiState.update { it.copy(isPro = isPro) }
            }
        }
    }

    private fun loadPlans() {
        viewModelScope.launch {
            val plans = billingManager.fetchOfferings()
            val defaultSelected = plans.firstOrNull { it.isBestValue }?.productId
                ?: plans.firstOrNull()?.productId
                ?: RevenueCatBillingManager.PRODUCT_YEARLY

            _uiState.update {
                it.copy(
                    availablePlans = plans,
                    selectedPlanId = defaultSelected
                )
            }
        }
    }

    fun onPlanSelected(planId: String) {
        _uiState.update { it.copy(selectedPlanId = planId) }
        telemetry.logPlanSelected(planId)
    }

    fun onPurchase(activity: Activity) {
        val selectedId = _uiState.value.selectedPlanId
        val plan = _uiState.value.availablePlans.firstOrNull { it.productId == selectedId } ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true, errorMessage = null, successMessage = null) }
            val result = billingManager.purchase(activity, plan)
            _uiState.update { it.copy(isPurchasing = false) }

            when (result) {
                is PurchaseResult.Success -> {
                    telemetry.logPurchaseSuccess(result.productId)
                    entitlementRepository.refresh()
                    _uiState.update { it.copy(successMessage = "Welcome to Cooked Pro!") }
                }
                is PurchaseResult.UserCancelled -> {
                    telemetry.logPurchaseCancelled()
                }
                is PurchaseResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
        }
    }

    fun onRestorePurchases() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true, errorMessage = null, successMessage = null) }
            val result = billingManager.restorePurchases()
            _uiState.update { it.copy(isRestoring = false) }

            result.fold(
                onSuccess = { hasPro ->
                    telemetry.logPurchaseRestored(hasPro)
                    if (hasPro) {
                        entitlementRepository.refresh()
                        _uiState.update { it.copy(successMessage = "Cooked Pro restored successfully!") }
                    } else {
                        _uiState.update { it.copy(errorMessage = "No active subscription found for this account.") }
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.localizedMessage ?: "Restore failed.") }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun setTermsDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showTermsDialog = visible) }
    }

    fun setPrivacyDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showPrivacyDialog = visible) }
    }

    fun onClaimRewardedProPass(activity: Activity) {
        val shown = adManager.showRewardedInterstitial(
            activity = activity,
            onRewardEarned = {
                viewModelScope.launch {
                    entitlementRepository.grantProPass(24)
                    _uiState.update { it.copy(successMessage = "24-Hour Pro Pass unlocked! All premium features active.") }
                }
            },
            onAdDismissed = {}
        )
        if (!shown) {
            _uiState.update { it.copy(errorMessage = "Rewarded ad is loading. Please try again in a moment.") }
        }
    }
}
