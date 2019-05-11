package com.ethan.and.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.flybd.sharebox.R
import com.ethan.and.ui.fragment.IjkVideoFragment
import com.flybd.sharebox.util.admob.AdmobCallback
import com.flybd.sharebox.util.admob.AdmobCallbackV2
import com.flybd.sharebox.util.admob.AdmobManager
import com.flybd.sharebox.util.firebase.FirebaseManager
import com.google.android.gms.ads.reward.RewardItem

/**
 * Created by xiang on 2018/3/24.
 */
class IjkVideoActivity : AppCompatActivity() {

    companion object {
        const val TAG = "IjkVideoFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_ijk_video)
        FirebaseManager.logEvent(FirebaseManager.Event.OPEN_VIDEO_ACTIVITY, null)
        if (supportFragmentManager.findFragmentByTag(TAG) == null) {
            if (intent != null && intent.extras != null) {
                val fragment = IjkVideoFragment()
                fragment.arguments = intent.extras
                fragment.arguments?.putParcelable("data", intent.data)
                supportFragmentManager.beginTransaction()
                        .add(R.id.container, fragment, TAG)
                        .commit()
            } else {
                if (intent != null && intent.data != null) {
                    val fragment = IjkVideoFragment()
                    fragment.arguments = Bundle()
                    fragment.arguments?.putParcelable("data", intent.data)
                    supportFragmentManager.beginTransaction()
                            .add(R.id.container, fragment, TAG)
                            .commit()
                } else {
                    Toast.makeText(this, "uri is invalid", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        val ctx = this.applicationContext
        val adManager = AdmobManager(ctx)
        adManager.loadRewardAd(ctx.getString(R.string.admob_ad_03), object : AdmobCallbackV2 {
            override fun onCompleted() {
            }
            override fun onLoaded() {
                adManager.getLatestRewardAd()?.show()
            }
            override fun onError() {
                if (isFinishing) {
                    return
                }
                adManager.loadInterstitialAd(ctx.getString(R.string.admob_ad_04), object : AdmobCallback {
                    override fun onLoaded() {
                        adManager.getLatestInterstitialAd()?.show()
                    }

                    override fun onError() {
                    }

                    override fun onOpened() {
                    }

                    override fun onClosed() {
                    }

                })
            }
            override fun onOpened() {
            }
            override fun onClosed() {
            }
            override fun onReward(item: RewardItem?) {
            }
        })
    }
}