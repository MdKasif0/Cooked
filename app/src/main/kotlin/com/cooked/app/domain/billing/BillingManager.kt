package com.cooked.app.domain.billing

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

data class SubscriptionPlan(
    val productId: String,
    val title: String,
    val subtitle: String,
    val formattedPrice: String,
    val priceMicros: Long,
    val period: PlanPeriod,
    val freeTrialDays: Int = 0,
    val isBestValue: Boolean = false,
    val isLifetime: Boolean = false
)

enum class PlanPeriod {
    YEARLY,
    MONTHLY,
    LIFETIME
}

sealed interface PurchaseResult {
    data class Success(val productId: String) : PurchaseResult
    data object UserCancelled : PurchaseResult
    data class Error(val message: String) : PurchaseResult
}

interface BillingManager {
    val availablePlans: StateFlow<List<SubscriptionPlan>>

    suspend fun fetchOfferings(): List<SubscriptionPlan>
    suspend fun purchase(activity: Activity, plan: SubscriptionPlan): PurchaseResult
    suspend fun restorePurchases(): Result<Boolean>
}
