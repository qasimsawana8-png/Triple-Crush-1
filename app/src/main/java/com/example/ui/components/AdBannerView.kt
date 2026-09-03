package com.example.ui.components

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ads.AdMobConstants
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdBannerView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adView = remember {
        AdView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val adSize = getAdaptiveAdSize(context)
            setAdSize(adSize)
            adUnitId = AdMobConstants.BANNER_AD_UNIT_ID
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d("AdBannerView", "Banner ad loaded successfully!")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(
                        "AdBannerView",
                        "Banner ad failed to load: ${error.message} (Code: ${error.code}, Domain: ${error.domain})"
                    )
                }

                override fun onAdOpened() {
                    Log.d("AdBannerView", "Banner ad opened.")
                }

                override fun onAdClosed() {
                    Log.d("AdBannerView", "Banner ad closed.")
                }
            }
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) {
        onDispose {
            try {
                adView.destroy()
            } catch (e: Exception) {
                Log.e("AdBannerView", "Error destroying adView", e)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .heightIn(min = 50.dp)
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x10000000))
            .testTag("admob_banner_view"),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { adView }
        )
    }
}

private fun getAdaptiveAdSize(context: Context): AdSize {
    return try {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val displayMetrics = DisplayMetrics()
        if (windowManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = windowManager.currentWindowMetrics
                val bounds = windowMetrics.bounds
                val density = context.resources.displayMetrics.density
                val adWidthPixels = bounds.width()
                val adWidth = (adWidthPixels / density).toInt()
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                val density = displayMetrics.density
                val adWidthPixels = displayMetrics.widthPixels
                val adWidth = (adWidthPixels / density).toInt()
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
            }
        } else {
            AdSize.BANNER
        }
    } catch (e: Exception) {
        AdSize.BANNER
    }
}

