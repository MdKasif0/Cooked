package com.cooked.app.data.billing

import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.billing.BillingIssueState
import com.cooked.app.domain.billing.EntitlementRepository
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class EntitlementRepositoryImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : EntitlementRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _billingIssue = MutableStateFlow<BillingIssueState?>(null)
    override val billingIssue: Flow<BillingIssueState?> = _billingIssue.asStateFlow()

    override val isPro: Flow<Boolean> = userPreferencesRepository.isPro.distinctUntilChanged()

    init {
        setupRevenueCatListener()
    }

    private fun setupRevenueCatListener() {
        try {
            if (Purchases.isConfigured) {
                Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
                    processCustomerInfo(customerInfo)
                }
            }
        } catch (_: Exception) {}
    }

    override suspend fun checkEntitlements(): Boolean {
        if (!Purchases.isConfigured) {
            return false
        }

        return suspendCoroutine { continuation ->
            try {
                Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                    override fun onReceived(customerInfo: com.revenuecat.purchases.CustomerInfo) {
                        val active = processCustomerInfo(customerInfo)
                        continuation.resume(active)
                    }

                    override fun onError(error: com.revenuecat.purchases.PurchasesError) {
                        continuation.resume(false)
                    }
                })
            } catch (e: Exception) {
                continuation.resume(false)
            }
        }
    }

    override suspend fun refresh() {
        checkEntitlements()
    }

    override suspend fun grantProPass(durationHours: Int) {
        userPreferencesRepository.setIsPro(true)
    }

    override suspend fun setLocalProForTesting(enabled: Boolean) {
        userPreferencesRepository.setIsPro(enabled)
    }

    private fun processCustomerInfo(customerInfo: com.revenuecat.purchases.CustomerInfo): Boolean {
        val entitlement = customerInfo.entitlements[RevenueCatBillingManager.ENTITLEMENT_ID]
        val isActive = entitlement?.isActive == true

        // Detect billing issues (grace period, account hold, billing retry)
        // If billing issues exist, we show a friendly in-app banner instead of silently downgrading
        if (entitlement != null && entitlement.billingIssueDetectedAt != null) {
            _billingIssue.value = BillingIssueState.IN_GRACE_PERIOD
        } else {
            _billingIssue.value = null
        }

        scope.launch {
            userPreferencesRepository.setIsPro(isActive)
        }

        return isActive
    }
}

