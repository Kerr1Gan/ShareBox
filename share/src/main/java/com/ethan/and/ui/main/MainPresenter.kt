package com.ethan.and.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.support.v4.app.ActivityCompat
import com.flybd.sharebox.R
import com.flybd.sharebox.util.admob.AdmobCallback
import com.flybd.sharebox.util.admob.AdmobManager


class MainPresenter : MainContract.Presenter {

    companion object {
        const val REQUEST_CODE = 10000
    }

    private val requestPermission = arrayOf(Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)

    private lateinit var context: Context
    private var adManager: AdmobManager? = null
    private var handler: Handler? = null
    private var view: MainContract.View? = null


    override fun onCreate(context: Context, handler: Handler) {
        this.context = context
        this.handler = handler
        initAd()
    }

    override fun onDestroy(context: Context) {
    }

    override fun registerWifiApReceiver(context: Context) {
    }

    override fun takeView(view: MainContract.View?) {
        this.view = view
        for (perm in requestPermission) {
            if (ActivityCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context as Activity, requestPermission, REQUEST_CODE)
            }
        }
    }

    override fun dropView() {
        this.view = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE) {
            var isGranted = true
            for (grant in grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false
                    break
                }
            }
            if (!isGranted) {
                view?.permissionRejected()
            }
        }
    }

    private fun initAd() {
        adManager = AdmobManager(context)
        adManager?.loadInterstitialAd(context.getString(R.string.admob_ad_02), object : AdmobCallback {
            override fun onLoaded() {
                adManager?.getLatestInterstitialAd()?.show()
            }

            override fun onError() {
                adManager?.loadInterstitialAd(context.getString(R.string.admob_ad_02), this)
            }

            override fun onOpened() {
            }

            override fun onClosed() {
                adManager = null
            }

        })
    }

}