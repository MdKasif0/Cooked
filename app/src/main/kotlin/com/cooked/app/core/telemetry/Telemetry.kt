package com.cooked.app.core.telemetry

import kotlinx.coroutines.flow.Flow

interface Telemetry {
    val isOptedIn: Flow<Boolean>

    // Opt-in / Opt-out
    suspend fun setOptIn(enabled: Boolean)

    // Scheduler Anomalies (non-fatal Crashlytics logging)
    fun logSchedulerAnomaly(itemId: String, delayMinutes: Long, scheduledEpochMillis: Long, actualEpochMillis: Long)
    fun logExactAlarmDenied()

    // Analytics Events (anonymous, NEVER personal data / titles / notes)
    fun logOnboardingStep(step: Int)
    fun logItemCreated(kind: String, via: String)
    fun logItemDone(onTime: Boolean, nags: Int)
    fun logSnooze(minutes: Int)
    fun logReliabilityFixTapped(oem: String, check: String)
    fun logWidgetAdded(type: String)
    fun logPaywallView(entryPoint: String)
    fun logPlanSelected(planId: String)
    fun logPurchaseSuccess(planId: String)
    fun logPurchaseCancelled()
    fun logPurchaseRestored(isPro: Boolean)
}
