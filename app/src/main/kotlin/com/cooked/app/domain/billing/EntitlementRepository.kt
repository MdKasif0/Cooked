package com.cooked.app.domain.billing

import kotlinx.coroutines.flow.Flow

enum class BillingIssueState {
    IN_GRACE_PERIOD,
    ON_ACCOUNT_HOLD,
    BILLING_RETRY
}

/**
 * Single source of truth for Cooked Pro status across the entire app.
 * Follows the Product Brief: never scatter entitlement checks across views/receivers.
 */
interface EntitlementRepository {
    val isPro: Flow<Boolean>
    val billingIssue: Flow<BillingIssueState?>

    suspend fun checkEntitlements(): Boolean
    suspend fun refresh()
    suspend fun setLocalProForTesting(enabled: Boolean)
}

