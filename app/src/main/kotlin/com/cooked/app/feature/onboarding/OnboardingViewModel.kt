package com.cooked.app.feature.onboarding

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.telemetry.Telemetry
import com.cooked.app.data.dao.ItemEventDao
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.data.entity.ItemEventEntity
import com.cooked.app.domain.billing.BillingManager
import com.cooked.app.domain.billing.EntitlementRepository
import com.cooked.app.domain.billing.PurchaseResult
import com.cooked.app.domain.billing.SubscriptionPlan
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.model.Personality
import com.cooked.app.domain.usecase.CreateItemFromExternal
import com.cooked.app.scheduling.battery.ReliabilityChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val createItemFromExternal: CreateItemFromExternal,
    private val reliabilityChecker: ReliabilityChecker,
    private val itemEventDao: ItemEventDao,
    private val billingManager: BillingManager,
    private val entitlementRepository: EntitlementRepository,
    private val telemetry: Telemetry
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        refreshPermissions()
        loadPlans()
        observeEntitlements()
    }

    private fun loadPlans() {
        viewModelScope.launch {
            val plans = billingManager.fetchOfferings()
            val defaultSelected = plans.firstOrNull { it.isBestValue }?.productId
                ?: plans.firstOrNull()?.productId
                ?: ""
            _uiState.update {
                it.copy(
                    availablePlans = plans,
                    selectedPlanId = defaultSelected
                )
            }
        }
    }

    private fun observeEntitlements() {
        viewModelScope.launch {
            entitlementRepository.isPro.collect { isPro ->
                _uiState.update { it.copy(isPro = isPro) }
            }
        }
    }

    fun onNext() {
        val current = _uiState.value.currentStep
        logStepCompleted(current)

        if (current >= _uiState.value.totalSteps - 1) {
            completeOnboarding()
        } else {
            val nextStep = current + 1
            _uiState.update { it.copy(currentStep = nextStep) }
            refreshPermissions()
            if (nextStep == _uiState.value.totalSteps - 1) {
                telemetry.logPaywallView("onboarding")
            }
        }
    }

    fun onSkip() {
        logStepCompleted(-1) // skipped
        completeOnboarding()
    }

    fun onPersonalitySelected(personality: Personality) {
        viewModelScope.launch {
            userPreferencesRepository.setPersonality(personality)
            _uiState.update { it.copy(selectedPersonality = personality) }
        }
    }

    fun onFirstWinInputChanged(text: String) {
        _uiState.update { it.copy(firstWinInput = text, firstWinError = null) }
    }

    fun saveFirstWin(text: String, onSuccess: (() -> Unit)? = null) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        if (_uiState.value.isSavingFirstWin) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingFirstWin = true, firstWinError = null) }
            val result = createItemFromExternal(trimmed)
            if (result.isSuccess) {
                val item = result.getOrNull()
                _uiState.update {
                    it.copy(
                        firstWinCreated = true,
                        isSavingFirstWin = false,
                        createdItem = item,
                        firstWinInput = text,
                        firstWinError = null
                    )
                }
                onSuccess?.invoke()
            } else {
                _uiState.update {
                    it.copy(
                        isSavingFirstWin = false,
                        firstWinError = result.exceptionOrNull()?.message ?: "Could not save task. Please try again."
                    )
                }
            }
        }
    }

    fun refreshPermissions() {
        val report = reliabilityChecker.check()
        _uiState.update {
            it.copy(
                notificationsGranted = report.notificationPermissionGranted,
                exactAlarmsGranted = report.canScheduleExactAlarms,
                batteryUnrestricted = report.isIgnoringBatteryOptimizations
            )
        }
    }

    // --- Cooked Pro & Billing Actions ---

    fun onPlanSelected(planId: String) {
        _uiState.update { it.copy(selectedPlanId = planId) }
        telemetry.logPlanSelected(planId)
    }

    fun purchaseSelectedPlan(activity: Activity) {
        val selectedId = _uiState.value.selectedPlanId
        val plan = _uiState.value.availablePlans.firstOrNull { it.productId == selectedId } ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true, billingErrorMessage = null, billingSuccessMessage = null) }
            val result = billingManager.purchase(activity, plan)
            _uiState.update { it.copy(isPurchasing = false) }

            when (result) {
                is PurchaseResult.Success -> {
                    telemetry.logPurchaseSuccess(result.productId)
                    entitlementRepository.refresh()
                    _uiState.update { it.copy(billingSuccessMessage = "Welcome to Cooked Pro!") }
                    completeOnboarding()
                }
                is PurchaseResult.UserCancelled -> {
                    // Do not treat cancel as an error
                }
                is PurchaseResult.Error -> {
                    _uiState.update { it.copy(billingErrorMessage = result.message) }
                }
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true, billingErrorMessage = null, billingSuccessMessage = null) }
            val result = billingManager.restorePurchases()
            _uiState.update { it.copy(isRestoring = false) }

            result.onSuccess { hasPro ->
                if (hasPro) {
                    entitlementRepository.refresh()
                    _uiState.update { it.copy(billingSuccessMessage = "Cooked Pro restored!") }
                    completeOnboarding()
                } else {
                    _uiState.update { it.copy(billingErrorMessage = "No active subscription found to restore.") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(billingErrorMessage = e.message ?: "Failed to restore purchases.") }
            }
        }
    }

    fun setTermsVisible(visible: Boolean) {
        _uiState.update { it.copy(showTermsDialog = visible) }
    }

    fun setPrivacyVisible(visible: Boolean) {
        _uiState.update { it.copy(showPrivacyDialog = visible) }
    }

    fun clearBillingMessages() {
        _uiState.update { it.copy(billingErrorMessage = null, billingSuccessMessage = null) }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingDone(true)
            _uiState.update { it.copy(isComplete = true) }
        }
    }

    private fun logStepCompleted(step: Int) {
        viewModelScope.launch {
            try {
                itemEventDao.insertEvent(
                    ItemEventEntity(
                        id = UUID.randomUUID().toString(),
                        itemId = "onboarding_flow",
                        type = ItemEventType.CREATED,
                        at = Instant.now(),
                        meta = mapOf("onboarding_step" to step.toString())
                    )
                )
            } catch (_: Exception) {}
        }
    }
}
