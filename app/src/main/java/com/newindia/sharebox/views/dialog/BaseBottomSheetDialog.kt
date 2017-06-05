package com.newindia.sharebox.views.dialog

import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.view.View
import android.view.ViewGroup
import android.util.DisplayMetrics
import android.app.Activity
import com.newindia.sharebox.R
import com.newindia.sharebox.views.activities.MainActivity


/**
 * Created by KerriGan on 2017/6/2.
 */

abstract class BaseBottomSheetDialog:BottomSheetDialog{

    constructor(context: Context,activity: Activity? = null):super(context){
        //do nothing
        ownerActivity=activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        if(ownerActivity==null)
            return
        val screenHeight = getScreenHeight(ownerActivity)
        val statusBarHeight = getStatusBarHeight(context)
        val dialogHeight = screenHeight - statusBarHeight
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, if (dialogHeight == 0) ViewGroup.LayoutParams.MATCH_PARENT else dialogHeight)
    }

    private fun init(){
        initializeDialog()
        var view= onCreateView()
        setContentView(view)
        onViewCreated(view)
    }

    protected open fun initializeDialog(){
//        requestWindowFeature(FEATURE_NO_TITLE)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

    protected open fun onCreateView():View?{
        return null
    }

    protected open fun onViewCreated(view:View?){
    }

    private fun getScreenHeight(activity: Activity): Int {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    private fun getStatusBarHeight(context: Context): Int {
        var statusBarHeight = 0
        val res = context.resources
        val resourceId = res.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = res.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }

    protected fun transparentDialog(){
        getWindow().findViewById(R.id.design_bottom_sheet)
                .setBackgroundResource(android.R.color.transparent)
    }
}