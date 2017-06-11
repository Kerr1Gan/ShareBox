package com.newindia.sharebox.ui.dialog

import android.app.Activity
import android.content.Context
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.newindia.sharebox.R
import com.newindia.sharebox.ui.views.CircleProgressView

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

    private var mCircleProgress:CircleProgressView? =null

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

        mCircleProgress= vg.findViewById(R.id.circle_progress) as CircleProgressView?

        mCircleProgress?.backgroundColor=context.resources.getColor(R.color.colorPrimary)
        mCircleProgress?.progressColor=context.resources.getColor(android.R.color.holo_green_light)
        mCircleProgress?.textColor=context.resources.getColor(android.R.color.white)
        mCircleProgress?.setStartText("Start")
        mCircleProgress?.setOnClickListener {
            mCircleProgress?.setProgress(50,true,10000)
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
}