package com.cooked.app.core.ads

import android.app.Activity
import android.content.Context
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
 * Ad Unit ID: ca-app-pub-6238466387091690/3945741481
 */
@Singleton
class CookedAdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val APPLICATION_ID = "ca-app-pub-6238466387091690~5314663479"
        const val REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-6238466387091690/3945741481"
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
                MobileAds.initialize(context) {
                    loadRewardedInterstitial()
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * Preloads a Rewarded Interstitial ad into memory for zero-latency display.
     */
    fun loadRewardedInterstitial() {
        if (_isLoading.value || rewardedInterstitialAd != null) return
        _isLoading.value = true

        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(
            context,
            REWARDED_INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    _isAdReady.value = true
                    _isLoading.value = false
                    setupCallbacks(ad)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
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
                rewardedInterstitialAd = null
                _isAdReady.value = false
                // Preload the next ad automatically
                loadRewardedInterstitial()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedInterstitialAd = null
                _isAdReady.value = false
                loadRewardedInterstitial()
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
            // If not ready, kick off a background load for next time
            loadRewardedInterstitial()
            return false
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedInterstitialAd = null
                _isAdReady.value = false
                onAdDismissed()
                loadRewardedInterstitial()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedInterstitialAd = null
                _isAdReady.value = false
                onAdDismissed()
                loadRewardedInterstitial()
            }
        }

        ad.show(activity) { _ ->
            onRewardEarned()
        }
        return true
    }
}
