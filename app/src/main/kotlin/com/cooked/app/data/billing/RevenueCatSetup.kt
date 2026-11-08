package com.cooked.app.data.billing

import android.content.Context
import com.cooked.app.BuildConfig
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration

/**
 * Single place that configures the RevenueCat SDK.
 *
 * The public Google API key comes from the build (`-Pcooked.revenuecatApiKey=goog_...` or the
 * `COOKED_REVENUECAT_API_KEY` environment variable). Without a real key the SDK is left
 * unconfigured and every purchase/restore reports "store unavailable" instead of pretending to
 * succeed.
 */
object RevenueCatSetup {

    val apiKey: String get() = BuildConfig.REVENUECAT_API_KEY

    val hasValidApiKey: Boolean
        get() = apiKey.startsWith("goog_") && !apiKey.contains("placeholder")

    /** Configures RevenueCat if a real key is present. Safe to call repeatedly. */
    @Synchronized
    fun ensureConfigured(context: Context): Boolean {
        if (Purchases.isConfigured) return true
        if (!hasValidApiKey) return false
        return try {
            Purchases.configure(
                PurchasesConfiguration.Builder(context.applicationContext, apiKey).build()
            )
            true
        } catch (_: Exception) {
            // e.g. no Play Services in a test environment
            false
        }
    }
}
