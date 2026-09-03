package com.example

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.ads.AdMobManager
import com.google.android.gms.ads.MobileAds

class TripleCrushApp : Application(), Application.ActivityLifecycleCallbacks {

    private var currentActivity: Activity? = null
    private var startedActivityCount = 0

    private var isFirstLaunch = true

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)

        // Initialize Google Mobile Ads SDK safely in background
        try {
            MobileAds.initialize(this) {
                AdMobManager.instance.loadAppOpenAd(this)
                AdMobManager.instance.loadInterstitialAd(this)
                AdMobManager.instance.loadRewardedAd(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        if (!AdMobManager.instance.isShowingAppOpenAd) {
            currentActivity = activity
        }
        startedActivityCount++
        if (startedActivityCount == 1) {
            // App entered foreground
            if (isFirstLaunch) {
                isFirstLaunch = false
                // Skip interrupting initial app launch
            } else {
                AdMobManager.instance.showAppOpenAdIfAvailable(activity)
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (!AdMobManager.instance.isShowingAppOpenAd) {
            currentActivity = activity
        }
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        startedActivityCount--
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
