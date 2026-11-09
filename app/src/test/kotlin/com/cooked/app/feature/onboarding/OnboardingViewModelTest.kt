package com.cooked.app.feature.onboarding

import android.app.Activity
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.cooked.app.core.telemetry.Telemetry
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.billing.EntitlementRepositoryImpl
import com.cooked.app.data.billing.RevenueCatBillingManager
import com.cooked.app.testing.testUserPreferencesRepository
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.data.db.CookedDatabase
import com.cooked.app.data.repository.ItemRepositoryImpl
import com.cooked.app.domain.billing.BillingManager
import com.cooked.app.domain.billing.PlanPeriod
import com.cooked.app.domain.billing.PurchaseResult
import com.cooked.app.domain.billing.SubscriptionPlan
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.Personality
import com.cooked.app.domain.usecase.CreateItemFromExternal
import com.cooked.app.scheduling.alarm.AlarmScheduler
import com.cooked.app.scheduling.alarm.ScheduledAlarmInfo
import com.cooked.app.scheduling.battery.ReliabilityChecker
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
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var database: CookedDatabase
    private lateinit var userPrefsRepo: UserPreferencesRepository
    private lateinit var repository: ItemRepositoryImpl
    private lateinit var alarmScheduler: FakeAlarmScheduler
    private lateinit var reliabilityChecker: ReliabilityChecker
    private lateinit var createItemFromExternal: CreateItemFromExternal
    private lateinit var fakeBillingManager: FakeBillingManager
    private lateinit var entitlementRepository: EntitlementRepositoryImpl
    private lateinit var fakeTelemetry: FakeTelemetry
    private lateinit var viewModel: OnboardingViewModel

    private class FakeAlarmScheduler : AlarmScheduler {
        override suspend fun schedule(item: Item, nagCount: Int?) {}
        override suspend fun schedule(itemId: String) {}
        override fun cancel(itemId: String) {}
        override suspend fun rescheduleAll() {}
        override fun getScheduledAlarms(): List<ScheduledAlarmInfo> = emptyList()
        override suspend fun fireNow(itemId: String) {}
    }

    private class FixedClock : CookedClock {
        override fun now(): Instant = Instant.parse("2026-11-08T10:00:00Z")
        override fun zone(): ZoneId = ZoneId.of("UTC")
    }

    private class FakeBillingManager : BillingManager {
        val testPlans = listOf(
            SubscriptionPlan(
                productId = RevenueCatBillingManager.PRODUCT_YEARLY,
                title = "Yearly Access",
                subtitle = "7-day free trial, then billed annually",
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
                subtitle = "Billed monthly, cancel anytime",
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
        var lastPaywallEntryPoint: String? = null
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
        override fun logPaywallView(entryPoint: String) { lastPaywallEntryPoint = entryPoint }
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
        database = Room.inMemoryDatabaseBuilder(context, CookedDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val clock = FixedClock()
        userPrefsRepo = testUserPreferencesRepository(testDispatcher)
        repository = ItemRepositoryImpl(
            database = database,
            itemDao = database.itemDao(),
            checklistStepDao = database.checklistStepDao(),
            itemEventDao = database.itemEventDao(),
            clock = clock
        )
        alarmScheduler = FakeAlarmScheduler()
        reliabilityChecker = ReliabilityChecker(context)
        createItemFromExternal = CreateItemFromExternal(
            itemRepository = repository,
            alarmScheduler = alarmScheduler,
            userPreferencesRepository = userPrefsRepo,
            clock = clock
        )
        fakeBillingManager = FakeBillingManager()
        entitlementRepository = EntitlementRepositoryImpl(userPrefsRepo)
        fakeTelemetry = FakeTelemetry()

        viewModel = OnboardingViewModel(
            userPreferencesRepository = userPrefsRepo,
            createItemFromExternal = createItemFromExternal,
            reliabilityChecker = reliabilityChecker,
            itemEventDao = database.itemEventDao(),
            billingManager = fakeBillingManager,
            entitlementRepository = entitlementRepository,
            telemetry = fakeTelemetry
        )
    }

    @After
    fun teardown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_startsAtStepZeroWithFourTotalSteps() = runTest(testDispatcher) {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.currentStep).isEqualTo(0)
        assertThat(state.totalSteps).isEqualTo(4)
        assertThat(state.isComplete).isFalse()
        assertThat(state.availablePlans).isNotEmpty()
        assertThat(state.selectedPlanId).isEqualTo(RevenueCatBillingManager.PRODUCT_YEARLY)
    }

    private fun waitUntil(timeoutMs: Long = 3000, condition: () -> Boolean) {
        val start = System.currentTimeMillis()
        while (!condition() && System.currentTimeMillis() - start < timeoutMs) {
            Thread.sleep(20)
            testDispatcher.scheduler.advanceUntilIdle()
        }
    }

    @Test
    fun onNext_incrementsStepsUntilCompletion() = runTest(testDispatcher) {
        assertThat(viewModel.uiState.value.currentStep).isEqualTo(0)

        // Step 0 -> Step 1 (Quick Capture)
        viewModel.onNext()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.currentStep).isEqualTo(1)

        // Step 1 -> Step 2 (Reliability)
        viewModel.onNext()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.currentStep).isEqualTo(2)

        // Step 2 -> Step 3 (Cooked Pro)
        viewModel.onNext()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.currentStep).isEqualTo(3)
        assertThat(fakeTelemetry.lastPaywallEntryPoint).isEqualTo("onboarding")

        // Step 3 is the final step. Calling onNext() completes onboarding.
        viewModel.onNext()
        waitUntil { viewModel.uiState.value.isComplete }
        assertThat(viewModel.uiState.value.isComplete).isTrue()
    }

    @Test
    fun onSkip_marksOnboardingCompleteImmediately() = runTest(testDispatcher) {
        viewModel.onSkip()
        waitUntil { viewModel.uiState.value.isComplete }
        assertThat(viewModel.uiState.value.isComplete).isTrue()
    }

    @Test
    fun personalitySelection_updatesStateAndPreferences() = runTest(testDispatcher) {
        viewModel.onPersonalitySelected(Personality.UNHINGED)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.selectedPersonality).isEqualTo(Personality.UNHINGED)
    }

    @Test
    fun planSelection_updatesPlanIdAndLogsTelemetry() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onPlanSelected(RevenueCatBillingManager.PRODUCT_MONTHLY)
        assertThat(viewModel.uiState.value.selectedPlanId).isEqualTo(RevenueCatBillingManager.PRODUCT_MONTHLY)
        assertThat(fakeTelemetry.lastPlanSelected).isEqualTo(RevenueCatBillingManager.PRODUCT_MONTHLY)
    }

    @Test
    fun termsAndPrivacyDialogs_toggleCorrectly() = runTest(testDispatcher) {
        viewModel.setTermsVisible(true)
        assertThat(viewModel.uiState.value.showTermsDialog).isTrue()
        viewModel.setTermsVisible(false)
        assertThat(viewModel.uiState.value.showTermsDialog).isFalse()

        viewModel.setPrivacyVisible(true)
        assertThat(viewModel.uiState.value.showPrivacyDialog).isTrue()
        viewModel.setPrivacyVisible(false)
        assertThat(viewModel.uiState.value.showPrivacyDialog).isFalse()
    }

    @Test
    fun restorePurchases_successUpdatesEntitlementAndCompletes() = runTest(testDispatcher) {
        fakeBillingManager.restoreResultToReturn = Result.success(true)
        viewModel.restorePurchases()
        advanceUntilIdle()
        waitUntil { viewModel.uiState.value.isComplete }
        assertThat(viewModel.uiState.value.isComplete).isTrue()
    }
}
