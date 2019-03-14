package com.flybd.sharebox.util.admob

import android.content.Context
import android.util.Log
import com.flybd.sharebox.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.gson.Gson
import java.lang.ref.WeakReference

/**
 * Created by Ethan_Xiang on 2017/9/1.
 */
open class AdmobManager(context: Context) : RewardedVideoAdListener, AdListener() {

    companion object {
        private const val DEBUG = false
    }

    private val mRequestBuilder = AdRequest.Builder()

    private var mRewardedAds: RewardedVideoAd? = null

    private var mInterstitialAd: InterstitialAd? = null

    private val mWeak: WeakReference<Context>

    private val mDebugDeviceId: String

    private var mInterstitialAdCallback: AdmobCallback? = null

    private var mRewardAdCallback: AdmobCallbackV2? = null

    init {
        mWeak = WeakReference(context)
        mDebugDeviceId = context.getString(R.string.admob_test_device)
        if (DEBUG) {
            mRequestBuilder.addTestDevice(mDebugDeviceId)
        }
    }

    private fun getInterstitialAd(): InterstitialAd? = if (mWeak.get() != null) InterstitialAd(mWeak.get()) else null

    private fun getRewardAd(): RewardedVideoAd? = if (mWeak.get() != null) MobileAds.getRewardedVideoAdInstance(mWeak.get()) else null

    fun loadInterstitialAd(ad: String, callback: AdmobCallback) {
        mInterstitialAd = getInterstitialAd()
        mInterstitialAdCallback = callback
        mInterstitialAd?.let {
            mInterstitialAd?.adListener = this
            mInterstitialAd?.adUnitId = ad
            mInterstitialAd?.loadAd(mRequestBuilder.build())
        }
    }

    fun loadRewardAd(ad: String, callback: AdmobCallbackV2) {
        mRewardedAds = getRewardAd()
        mRewardAdCallback = callback
        mRewardedAds?.let {
            mRewardedAds?.rewardedVideoAdListener = this
            mRewardedAds?.loadAd(ad, mRequestBuilder.build())
        }
    }

    fun getLatestInterstitialAd(): InterstitialAd? = mInterstitialAd

    fun getLatestRewardAd(): RewardedVideoAd? = mRewardedAds

    open fun onResume() {
        mWeak.get()?.let {
            mRewardedAds?.resume(mWeak.get())
        }
    }

    open fun onPause() {
        mWeak.get()?.let {
            mRewardedAds?.pause(mWeak.get())
        }
    }

    open fun onDestroy() {
        mWeak.get()?.let {
            mRewardedAds?.destroy(mWeak.get())
        }
    }

    override fun onRewardedVideoAdClosed() {
        Log.i("AdMob", "onRewardedVideoAdClosed")
        mRewardAdCallback?.onClosed()
    }

    override fun onRewardedVideoAdLeftApplication() {
        Log.i("AdMob", "onRewardedVideoAdLeftApplication")
    }

    override fun onRewardedVideoAdLoaded() {
        Log.i("AdMob", "onRewardedVideoAdLoaded")
        mRewardAdCallback?.onLoaded()
    }

    override fun onRewardedVideoAdOpened() {
        Log.i("AdMob", "onRewardedVideoAdOpened")
        mRewardAdCallback?.onOpened()
    }

    override fun onRewarded(p0: RewardItem?) {
        Log.i("AdMob", "onRewarded rewardItem " + Gson().toJson(p0))
        mRewardAdCallback?.onReward(p0)
    }

    override fun onRewardedVideoStarted() {
        Log.i("AdMob", "onRewardedVideoStarted")
        mRewardAdCallback?.onOpened()
    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
        Log.i("AdMob", "onRewardedVideoAdFailedToLoad code $p0")
        mRewardAdCallback?.onError()
    }

    override fun onAdImpression() {
    }

    override fun onAdLeftApplication() {
    }

    override fun onAdClicked() {
    }

    override fun onAdFailedToLoad(p0: Int) {
        Log.i("AdMob", "onAdFailedToLoad code $p0")
        mInterstitialAdCallback?.onError()
    }

    override fun onAdClosed() {
        Log.i("AdMob", "onAdClosed")
        mInterstitialAdCallback?.onClosed()
    }

    override fun onAdOpened() {
        Log.i("AdMob", "onAdOpened")
        mInterstitialAdCallback?.onOpened()
    }

    override fun onAdLoaded() {
        Log.i("AdMob", "onAdLoaded")
        mInterstitialAdCallback?.onLoaded()
    }
}