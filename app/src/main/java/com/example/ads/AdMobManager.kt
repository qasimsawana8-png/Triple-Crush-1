package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.Date

class AdMobManager private constructor() {

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false
    private var appOpenLoadTime: Long = 0
    var isShowingAppOpenAd: Boolean = false
        private set

    companion object {
        private const val TAG = "AdMobManager"
        val instance: AdMobManager by lazy { AdMobManager() }
    }

    // ==========================================
    // Interstitial Ads
    // ==========================================
    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return

        isInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AdMobConstants.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial ad successfully loaded.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onDismissed: () -> Unit = {}) {
        val currentAd = interstitialAd
        if (currentAd != null) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd(activity.applicationContext)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitialAd(activity.applicationContext)
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad showed fullscreen content.")
                }
            }
            currentAd.show(activity)
        } else {
            loadInterstitialAd(activity.applicationContext)
            onDismissed()
        }
    }

    // ==========================================
    // Rewarded Ads
    // ==========================================
    fun loadRewardedAd(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return

        isRewardedLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            AdMobConstants.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded ad successfully loaded.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.w(TAG, "Rewarded ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun isRewardedAdReady(): Boolean = rewardedAd != null

    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: (rewardAmount: Int) -> Unit,
        onAdDismissedOrFailed: () -> Unit = {}
    ) {
        val currentAd = rewardedAd
        if (currentAd != null) {
            var rewardEarned = false
            var earnedAmount = 50

            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd(activity.applicationContext)
                    if (rewardEarned) {
                        onUserEarnedReward(earnedAmount)
                    }
                    onAdDismissedOrFailed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadRewardedAd(activity.applicationContext)
                    onAdDismissedOrFailed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad showed fullscreen content.")
                }
            }

            currentAd.show(activity) { rewardItem ->
                rewardEarned = true
                earnedAmount = if (rewardItem.amount > 0) rewardItem.amount else 50
            }
        } else {
            loadRewardedAd(activity.applicationContext)
            onAdDismissedOrFailed()
        }
    }

    // ==========================================
    // App Open Ads
    // ==========================================
    private fun isAppOpenAdAvailable(): Boolean {
        val wasLoadTimeLessThan4HoursAgo = (Date().time - appOpenLoadTime) < (4 * 3600000)
        return appOpenAd != null && wasLoadTimeLessThan4HoursAgo
    }

    fun loadAppOpenAd(context: Context) {
        if (isAppOpenAdAvailable() || isAppOpenLoading) return

        isAppOpenLoading = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AdMobConstants.APP_OPEN_AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isAppOpenLoading = false
                    appOpenLoadTime = Date().time
                    Log.d(TAG, "App Open ad loaded.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    appOpenAd = null
                    isAppOpenLoading = false
                    Log.w(TAG, "App Open ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun showAppOpenAdIfAvailable(activity: Activity, onShowComplete: () -> Unit = {}) {
        if (isShowingAppOpenAd) {
            onShowComplete()
            return
        }

        if (!isAppOpenAdAvailable()) {
            loadAppOpenAd(activity.applicationContext)
            onShowComplete()
            return
        }

        val ad = appOpenAd ?: run {
            onShowComplete()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAppOpenAd = false
                loadAppOpenAd(activity.applicationContext)
                onShowComplete()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAppOpenAd = false
                loadAppOpenAd(activity.applicationContext)
                onShowComplete()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAppOpenAd = true
            }
        }
        ad.show(activity)
    }
}
