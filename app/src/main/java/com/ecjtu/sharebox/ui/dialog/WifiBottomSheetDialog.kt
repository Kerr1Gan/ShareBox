package com.ecjtu.sharebox.ui.dialog

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.os.Handler
import android.provider.Settings
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.ActivityCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.dd.CircularProgressButton
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.presenter.MainActivityDelegate
import com.ecjtu.sharebox.ui.view.CircleProgressView
import org.ecjtu.channellibrary.wifiutil.NetworkUtil
import org.ecjtu.channellibrary.wifiutil.WifiUtil

/**
 * Created by KerriGan on 2017/6/2.
 */
open class WifiBottomSheetDialog:CloseBottomSheetDialog{

    constructor(context: Context,activity: Activity? = null):super(context,activity){

    }

    private var mHotspotName:TextInputEditText? =null

    private var mHotspotPwd:TextInputEditText? =null

    private var mNameTextInput:TextInputLayout? =null

    private var mPwdTextInput:TextInputLayout? =null

    private var mReceiver:BroadcastReceiver? =null

    private var mHandler:Handler =Handler(ownerActivity.mainLooper)

    private val DELAY_TIME = 5 * 1000L

    private var mCircularButton:CircularProgressButton? =null

    companion object {
        const val PROGRESS_START= 0
        const val PROGRESS_MIDDLE= 50
        const val PROGRESS_END=100
    }

    override fun onCreateView(): View?{
        var vg = super.onCreateView() as ViewGroup
        var child=layoutInflater.inflate(R.layout.dialog_hotspot,vg,false)
        vg.addView(child)
        initView(vg)
        return vg
    }

    private fun initView(vg:ViewGroup){
        mHotspotName=vg.findViewById(R.id.ap_name) as TextInputEditText
        mHotspotPwd=vg.findViewById(R.id.ap_pwd) as TextInputEditText

        mNameTextInput= vg.findViewById(R.id.text_input_ap) as TextInputLayout

        mPwdTextInput= vg.findViewById(R.id.text_input_pwd) as TextInputLayout

        mHotspotName?.addTextChangedListener(mTextWatcherName)
        mHotspotPwd?.addTextChangedListener(mTextWatcherPwd)

        setupCircularProgressButton(vg)

        var config=NetworkUtil.getHotSpotConfiguration(context)
        mHotspotName?.setText(config.SSID)
        mHotspotPwd?.setText(config.preSharedKey)
        if(NetworkUtil.isHotSpot(context)){
            mCircularButton?.setText("关闭")
            mCircularButton?.progress= PROGRESS_END
        }else{
            mCircularButton?.setText("开启")
            mCircularButton?.progress= PROGRESS_START
        }

        (vg.findViewById(R.id.text_title) as TextView).setText("Hotspot")
    }

    override fun dismiss() {
        super.dismiss()
        mHotspotName?.removeTextChangedListener(mTextWatcherName)
        mHotspotPwd?.removeTextChangedListener(mTextWatcherPwd)
    }

    private var mTextWatcherName = object : TextWatcher{
        override fun beforeTextChanged(s: CharSequence, start: Int,
                                       count: Int, after: Int){

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int){
            var len=s.length
            if(len>mPwdTextInput!!.counterMaxLength){
                mPwdTextInput!!.setError("名字太长啦呆逼")
            }else{
                mPwdTextInput!!.setError(null)
            }
        }


        override fun afterTextChanged(s: Editable){

        }
    }

    private var mTextWatcherPwd = object : TextWatcher{
        override fun beforeTextChanged(s: CharSequence, start: Int,
                                       count: Int, after: Int){
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int){
            var len=s.length
            if(len>mPwdTextInput!!.counterMaxLength){
                mPwdTextInput!!.setError("密码太长啦呆逼")
            }else{
                mPwdTextInput!!.setError(null)
            }
        }

        override fun afterTextChanged(s: Editable){

        }
    }

    override fun onStart() {
        super.onStart()
        mReceiver=object :BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                var status=intent?.getIntExtra("wifi_state", -1)
                if(status==13){
                    //wifi ap enabled
                    var config=NetworkUtil.getHotSpotConfiguration(context)
                    mHotspotName?.setText(config.SSID)
                    mHotspotPwd?.setText(config.preSharedKey)
                    mCircularButton?.setText("关闭")
                    mCircularButton?.progress= PROGRESS_END

                    mHandler.removeCallbacksAndMessages(null)
                }else if(status==11){
                    //wifi ap disabled
                    mCircularButton?.setText("开启")
                    mCircularButton?.progress= PROGRESS_START
                }
            }

        }

        var filter=IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED")

        context.registerReceiver(mReceiver,filter)
    }

    override fun onStop() {
        super.onStop()
        context.unregisterReceiver(mReceiver)
        mHandler.removeCallbacksAndMessages(null)
    }

    fun setupCircularProgressButton(vg:ViewGroup){
        mCircularButton = vg.findViewById(R.id.circle_progress) as CircularProgressButton
        mCircularButton?.isIndeterminateProgressMode = true
        mCircularButton?.setOnClickListener {
            if (mCircularButton?.progress == PROGRESS_START) {
                mCircularButton?.progress = PROGRESS_MIDDLE

                if (NetworkUtil.isHotSpot(context)) {
                    WifiUtil.openHotSpot(context, false, mHotspotName?.text.toString(), mHotspotPwd?.text.toString())
                    return@setOnClickListener
                }

                mHandler.postDelayed({
                    Toast.makeText(context, "No permission", Toast.LENGTH_SHORT).show()
                    context.startActivity(MainActivityDelegate.getAppDetailSettingIntent(context))
                    mCircularButton?.progress= PROGRESS_START
                    cancel()
                }, DELAY_TIME)

                WifiUtil.openHotSpot(context,true,mHotspotName?.text.toString()
                        ,mHotspotPwd?.text.toString())
            } else if (mCircularButton?.progress == PROGRESS_END) {
                mCircularButton?.progress = PROGRESS_START
                mCircularButton?.setText("开启")

                WifiUtil.openHotSpot(context,false,mHotspotName?.text.toString()
                        ,mHotspotPwd?.text.toString())
            }
        }
    }
}