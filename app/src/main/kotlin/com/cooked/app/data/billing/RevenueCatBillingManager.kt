package com.cooked.app.data.billing

import android.app.Activity
import android.content.Context
import com.cooked.app.domain.billing.BillingManager
import com.cooked.app.domain.billing.PlanPeriod
import com.cooked.app.domain.billing.PurchaseResult
import com.cooked.app.domain.billing.SubscriptionPlan
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchasesErrorCode
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.models.StoreTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class RevenueCatBillingManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) : BillingManager {

    companion object {
        const val ENTITLEMENT_ID = "pro"
        const val PRODUCT_YEARLY = "cooked_pro_yearly"
        const val PRODUCT_MONTHLY = "cooked_pro_monthly"
        const val PRODUCT_LIFETIME = "cooked_pro_lifetime"

        const val ERROR_STORE_UNAVAILABLE =
            "The store isn't available right now. Check your connection and try again."
        const val ERROR_NOT_ENTITLED =
            "Payment went through but Pro isn't active yet. Try Restore Purchases in a moment."
    }

    private val _availablePlans = MutableStateFlow<List<SubscriptionPlan>>(defaultFallbackPlans())
    override val availablePlans: StateFlow<List<SubscriptionPlan>> = _availablePlans.asStateFlow()

    private val packageMap = mutableMapOf<String, Package>()

    init {
        initializeRevenueCatIfConfigured()
    }

    private fun initializeRevenueCatIfConfigured() {
        RevenueCatSetup.ensureConfigured(context)
    }

    override suspend fun fetchOfferings(): List<SubscriptionPlan> {
        if (!Purchases.isConfigured) {
            return _availablePlans.value
        }

        return suspendCancellableCoroutine { continuation ->
            try {
                Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
                    override fun onReceived(offerings: com.revenuecat.purchases.Offerings) {
                        val current = offerings.current
                        if (current == null || current.availablePackages.isEmpty()) {
                            continuation.resume(_availablePlans.value)
                            return
                        }

                        packageMap.clear()
                        val plans = current.availablePackages.mapNotNull { pkg ->
                            val product = pkg.product
                            val productId = product.id
                            packageMap[productId] = pkg

                            when {
                                productId.contains("yearly") || pkg.packageType.name == "ANNUAL" -> {
                                    SubscriptionPlan(
                                        productId = productId,
                                        title = "Yearly Access",
                                        subtitle = "7-day free trial, then billed annually",
                                        formattedPrice = product.price.formatted,
                                        priceMicros = product.price.amountMicros,
                                        period = PlanPeriod.YEARLY,
                                        freeTrialDays = 7,
                                        isBestValue = true,
                                        isLifetime = false
                                    )
                                }
                                productId.contains("monthly") || pkg.packageType.name == "MONTHLY" -> {
                                    SubscriptionPlan(
                                        productId = productId,
                                        title = "Monthly",
                                        subtitle = "Billed monthly, cancel anytime",
                                        formattedPrice = product.price.formatted,
                                        priceMicros = product.price.amountMicros,
                                        period = PlanPeriod.MONTHLY,
                                        freeTrialDays = 0,
                                        isBestValue = false,
                                        isLifetime = false
                                    )
                                }
                                productId.contains("lifetime") || pkg.packageType.name == "LIFETIME" -> {
                                    SubscriptionPlan(
                                        productId = productId,
                                        title = "Lifetime",
                                        subtitle = "Pay once, unlock Cooked Pro forever",
                                        formattedPrice = product.price.formatted,
                                        priceMicros = product.price.amountMicros,
                                        period = PlanPeriod.LIFETIME,
                                        freeTrialDays = 0,
                                        isBestValue = false,
                                        isLifetime = true
                                    )
                                }
                                else -> null
                            }
                        }

                        val result = if (plans.isNotEmpty()) plans else defaultFallbackPlans()
                        _availablePlans.value = result
                        continuation.resume(result)
                    }

                    override fun onError(error: PurchasesError) {
                        continuation.resume(_availablePlans.value)
                    }
                })
            } catch (e: Exception) {
                continuation.resume(_availablePlans.value)
            }
        }
    }

    override suspend fun purchase(activity: Activity, plan: SubscriptionPlan): PurchaseResult {
        if (!Purchases.isConfigured) {
            return PurchaseResult.Error(ERROR_STORE_UNAVAILABLE)
        }

        // Plans shown from the offline fallback list have no store package; retry loading
        // offerings once, and never grant anything without a real package.
        if (packageMap[plan.productId] == null) {
            fetchOfferings()
        }
        val pkg = packageMap[plan.productId]
            ?: return PurchaseResult.Error(ERROR_STORE_UNAVAILABLE)

        return suspendCancellableCoroutine { continuation ->
            try {
                val params = com.revenuecat.purchases.PurchaseParams.Builder(activity, pkg).build()
                Purchases.sharedInstance.purchase(
                    params,
                    object : PurchaseCallback {
                        override fun onCompleted(
                            storeTransaction: StoreTransaction,
                            customerInfo: com.revenuecat.purchases.CustomerInfo
                        ) {
                            val isEntitled = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
                            if (isEntitled) {
                                continuation.resume(PurchaseResult.Success(plan.productId))
                            } else {
                                continuation.resume(PurchaseResult.Error(ERROR_NOT_ENTITLED))
                            }
                        }

                        override fun onError(error: PurchasesError, userCancelled: Boolean) {
                            if (userCancelled || error.code == PurchasesErrorCode.PurchaseCancelledError) {
                                continuation.resume(PurchaseResult.UserCancelled)
                            } else {
                                continuation.resume(PurchaseResult.Error(error.message))
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                continuation.resume(PurchaseResult.Error(e.localizedMessage ?: "Purchase failed"))
            }
        }
    }

    override suspend fun restorePurchases(): Result<Boolean> {
        if (!Purchases.isConfigured) {
            return Result.failure(IllegalStateException(ERROR_STORE_UNAVAILABLE))
        }

        return suspendCancellableCoroutine { continuation ->
            try {
                Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
                    override fun onReceived(customerInfo: com.revenuecat.purchases.CustomerInfo) {
                        val hasPro = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
                        continuation.resume(Result.success(hasPro))
                    }

                    override fun onError(error: PurchasesError) {
                        continuation.resume(Result.failure(Exception(error.message)))
                    }
                })
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    private fun defaultFallbackPlans(): List<SubscriptionPlan> = listOf(
        SubscriptionPlan(
            productId = PRODUCT_YEARLY,
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
            productId = PRODUCT_MONTHLY,
            title = "Monthly",
            subtitle = "Billed monthly, cancel anytime",
            formattedPrice = "$2.99 / month",
            priceMicros = 2990000L,
            period = PlanPeriod.MONTHLY,
            freeTrialDays = 0,
            isBestValue = false,
            isLifetime = false
        ),
        SubscriptionPlan(
            productId = PRODUCT_LIFETIME,
            title = "Lifetime",
            subtitle = "Pay once, unlock Cooked Pro forever",
            formattedPrice = "$39.99",
            priceMicros = 39990000L,
            period = PlanPeriod.LIFETIME,
            freeTrialDays = 0,
            isBestValue = false,
            isLifetime = true
        )
    )
}
