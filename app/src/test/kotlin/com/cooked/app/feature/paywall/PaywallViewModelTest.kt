package com.cooked.app.feature.paywall

import android.app.Activity
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.cooked.app.core.telemetry.Telemetry
import com.cooked.app.data.billing.EntitlementRepositoryImpl
import com.cooked.app.data.billing.RevenueCatBillingManager
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.billing.BillingManager
import com.cooked.app.domain.billing.PlanPeriod
import com.cooked.app.domain.billing.PurchaseResult
import com.cooked.app.domain.billing.SubscriptionPlan
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PaywallViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var userPrefsRepo: UserPreferencesRepository
    private lateinit var entitlementRepository: EntitlementRepositoryImpl
    private lateinit var fakeBillingManager: FakeBillingManager
    private lateinit var fakeTelemetry: FakeTelemetry

    private class FakeBillingManager : BillingManager {
        val testPlans = listOf(
            SubscriptionPlan(
                productId = RevenueCatBillingManager.PRODUCT_YEARLY,
                title = "Yearly Access",
                subtitle = "7-day free trial",
                formattedPrice = "$14.99 / year",
                priceMicros = 14990000L,
                period = PlanPeriod.YEARLY,
                freeTrialDays = 7,
                isBestValue = true,
                isLifetime = false
            ),
            SubscriptionPlan(
                productId = RevenueCatBillingManager.PRODUCT_MONTHLY,
                title = "Monthly",
                subtitle = "Billed monthly",
                formattedPrice = "$2.99 / month",
                priceMicros = 2990000L,
                period = PlanPeriod.MONTHLY,
                freeTrialDays = 0,
                isBestValue = false,
                isLifetime = false
            )
        )

        private val _availablePlans = MutableStateFlow(testPlans)
        override val availablePlans: StateFlow<List<SubscriptionPlan>> = _availablePlans.asStateFlow()

        var purchaseResultToReturn: PurchaseResult = PurchaseResult.Success(RevenueCatBillingManager.PRODUCT_YEARLY)
        var restoreResultToReturn: Result<Boolean> = Result.success(true)

        override suspend fun fetchOfferings(): List<SubscriptionPlan> = testPlans
        override suspend fun purchase(activity: Activity, plan: SubscriptionPlan): PurchaseResult = purchaseResultToReturn
        override suspend fun restorePurchases(): Result<Boolean> = restoreResultToReturn
    }

    private class FakeTelemetry : Telemetry {
        var lastPlanSelected: String? = null
        var lastEntryPoint: String? = null
        var purchaseSuccessLogged: String? = null

        override val isOptedIn = flowOf(true)
        override suspend fun setOptIn(enabled: Boolean) {}
        override fun logSchedulerAnomaly(itemId: String, delayMinutes: Long, scheduledEpochMillis: Long, actualEpochMillis: Long) {}
        override fun logExactAlarmDenied() {}
        override fun logOnboardingStep(step: Int) {}
        override fun logItemCreated(kind: String, via: String) {}
        override fun logItemDone(onTime: Boolean, nags: Int) {}
        override fun logSnooze(minutes: Int) {}
        override fun logReliabilityFixTapped(oem: String, check: String) {}
        override fun logWidgetAdded(type: String) {}
        override fun logPaywallView(entryPoint: String) { lastEntryPoint = entryPoint }
        override fun logPlanSelected(planId: String) { lastPlanSelected = planId }
        override fun logPurchaseSuccess(planId: String) { purchaseSuccessLogged = planId }
        override fun logPurchaseCancelled() {}
        override fun logPurchaseRestored(isPro: Boolean) {}
        override fun logSnitchOffered() {}
        override fun logSnitchShared(app: String) {}
        override fun logScanStarted() {}
        override fun logScanItemsAdded(count: Int) {}
        override fun logTemplateUsed(type: String) {}
        override fun logCookSessionCompleted(minutes: Int) {}
        override fun logRescuePlanCreated(sessions: Int) {}
        override fun logCalendarCapacityEnabled() {}
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        userPrefsRepo = UserPreferencesRepository(context)
        entitlementRepository = EntitlementRepositoryImpl(userPrefsRepo)
        fakeBillingManager = FakeBillingManager()
        fakeTelemetry = FakeTelemetry()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        entryPoint: String = "generic",
        featureHint: String? = null
    ): PaywallViewModel {
        val handle = SavedStateHandle(
            mapOf("entryPoint" to entryPoint, "featureHint" to featureHint)
        )
        return PaywallViewModel(
            billingManager = fakeBillingManager,
            entitlementRepository = entitlementRepository,
            telemetry = fakeTelemetry,
            savedStateHandle = handle
        )
    }

    @Test
    fun init_logsPaywallViewAndSelectsBestValuePlan() = runTest(testDispatcher) {
        val viewModel = createViewModel(entryPoint = "settings_pro")
        advanceUntilIdle()

        assertThat(fakeTelemetry.lastEntryPoint).isEqualTo("settings_pro")
        val state = viewModel.uiState.value
        assertThat(state.availablePlans).isNotEmpty()
        assertThat(state.selectedPlanId).isEqualTo(RevenueCatBillingManager.PRODUCT_YEARLY)
    }

    @Test
    fun init_withFeatureHint_highlightsMatchingBenefit() = runTest(testDispatcher) {
        val viewModel = createViewModel(featureHint = "critical_alarm")
        advanceUntilIdle()

        val highlighted = viewModel.uiState.value.benefits.firstOrNull { it.isHighlighted }
        assertThat(highlighted).isNotNull()
        assertThat(highlighted?.id).isEqualTo("critical_alarm")
    }

    @Test
    fun onPlanSelected_updatesStateAndLogsTelemetry() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onPlanSelected(RevenueCatBillingManager.PRODUCT_MONTHLY)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedPlanId).isEqualTo(RevenueCatBillingManager.PRODUCT_MONTHLY)
        assertThat(fakeTelemetry.lastPlanSelected).isEqualTo(RevenueCatBillingManager.PRODUCT_MONTHLY)
    }

    @Test
    fun onRestorePurchases_successUpdatesMessage() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        fakeBillingManager.restoreResultToReturn = Result.success(true)
        viewModel.onRestorePurchases()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.successMessage).contains("restored successfully")
        assertThat(state.errorMessage).isNull()
    }

    @Test
    fun onRestorePurchases_noneFoundSetsErrorMessage() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        fakeBillingManager.restoreResultToReturn = Result.success(false)
        viewModel.onRestorePurchases()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.errorMessage).contains("No active subscription")
    }
}

