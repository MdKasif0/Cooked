package com.cooked.app.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.cooked.app.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Google Mobile Ads (AdMob) initialization and Rewarded Interstitial lifecycle.
 *
 * Ad Type: Rewarded Interstitial
 * Placement: High-value natural app transitions (Unlocking Pro pass, Free Rescue Plan sessions, Extended Snooze)
 * App ID: ca-app-pub-6238466387091690~5314663479
 * Production Ad Unit ID: ca-app-pub-6238466387091690/3945741481
 * Sample Test Ad Unit ID: ca-app-pub-3940256099942544/5354046379
 */
@Singleton
class CookedAdManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CookedAds"

        const val APPLICATION_ID = "ca-app-pub-6238466387091690~5314663479"
        const val REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-6238466387091690/3945741481"
        // Official Google Sample Ad Unit ID for testing Rewarded Interstitials
        const val TEST_REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"
    }

    val activeAdUnitId: String
        get() = if (BuildConfig.DEBUG) {
            TEST_REWARDED_INTERSTITIAL_AD_UNIT_ID
        } else {
            REWARDED_INTERSTITIAL_AD_UNIT_ID
        }

    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private val isInitialized = AtomicBoolean(false)

    private val _isAdReady = MutableStateFlow(false)
    val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Initializes Google Mobile Ads SDK and preloads the first Rewarded Interstitial.
     */
    fun initialize() {
        if (isInitialized.compareAndSet(false, true)) {
            try {
                Log.d(TAG, "Initializing Google Mobile Ads SDK (App ID: $APPLICATION_ID)...")
                MobileAds.initialize(context) { status ->
                    Log.d(TAG, "MobileAds initialization complete. Status: ${status.adapterStatusMap}")
                    loadRewardedInterstitial()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize MobileAds", e)
            }
        }
    }

    /**
     * Preloads a Rewarded Interstitial ad into memory for zero-latency display.
     */
    fun loadRewardedInterstitial() {
        if (_isLoading.value || rewardedInterstitialAd != null) {
            Log.d(TAG, "loadRewardedInterstitial skipped: already loading or ad ready.")
            return
        }
        _isLoading.value = true

        val unitId = activeAdUnitId
        Log.d(TAG, "Loading Rewarded Interstitial with Ad Unit: $unitId (isDebug=${BuildConfig.DEBUG})")

        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(
            context,
            unitId,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    Log.d(TAG, "Rewarded Interstitial ad loaded successfully!")
                    rewardedInterstitialAd = ad
                    _isAdReady.value = true
                    _isLoading.value = false
                    setupCallbacks(ad)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(
                        TAG,
                        "Rewarded Interstitial failed to load: ${error.message} " +
                            "(code=${error.code}, domain=${error.domain})"
                    )
                    rewardedInterstitialAd = null
                    _isAdReady.value = false
                    _isLoading.value = false
                }
            }
        )
    }

    private fun setupCallbacks(ad: RewardedInterstitialAd) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded ad dismissed. Preloading next ad...")
                rewardedInterstitialAd = null
                _isAdReady.value = false
                loadRewardedInterstitial()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "Rewarded ad failed to show: ${error.message} (code=${error.code})")
                rewardedInterstitialAd = null
                _isAdReady.value = false
                loadRewardedInterstitial()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad showed fullscreen content.")
            }
        }
    }

    /**
     * Shows the preloaded Rewarded Interstitial ad.
     *
     * @param activity Hosting Activity required by Google Mobile Ads SDK
     * @param onRewardEarned Invoked when user finishes viewing and earns the reward
     * @param onAdDismissed Invoked when the ad fullscreen content is dismissed
     * @return true if ad was presented, false if ad was not ready
     */
    fun showRewardedInterstitial(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDismissed: () -> Unit = {}
    ): Boolean {
        val ad = rewardedInterstitialAd
        if (ad == null) {
            Log.w(TAG, "showRewardedInterstitial called but ad was null. Initiating reload...")
            loadRewardedInterstitial()
            return false
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed by user.")
                rewardedInterstitialAd = null
                _isAdReady.value = false
                onAdDismissed()
                loadRewardedInterstitial()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "Ad failed to show: ${error.message}")
                rewardedInterstitialAd = null
                _isAdReady.value = false
                onAdDismissed()
                loadRewardedInterstitial()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad displayed fullscreen.")
            }
        }

        Log.d(TAG, "Showing Rewarded Interstitial ad now...")
        ad.show(activity) { rewardItem ->
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            onRewardEarned()
        }
        return true
    }
}
