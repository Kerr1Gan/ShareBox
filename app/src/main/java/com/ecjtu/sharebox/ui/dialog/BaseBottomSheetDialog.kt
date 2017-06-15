package com.ecjtu.sharebox.ui.dialog

import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.view.View
import android.view.ViewGroup
import android.util.DisplayMetrics
import android.app.Activity
import android.support.design.widget.BottomSheetBehavior
import android.view.WindowManager
import com.ecjtu.sharebox.R


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
        var immersive=init()
        if(ownerActivity==null)
            return
        val screenHeight = getScreenHeight(ownerActivity)
        val statusBarHeight = getStatusBarHeight(context)
        val dialogHeight = if(immersive) screenHeight else screenHeight- statusBarHeight
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, if (dialogHeight == 0) ViewGroup.LayoutParams.MATCH_PARENT else dialogHeight)
    }

    private fun init():Boolean{
        initializeDialog()
        var view= onCreateView()
        setContentView(view)
        return onViewCreated(view)
    }

    protected open fun initializeDialog(){
//        requestWindowFeature(FEATURE_NO_TITLE)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

    protected open fun onCreateView():View?{
        var behavior = BottomSheetBehavior.from(findViewById(android.support.design.R.id.design_bottom_sheet))
        return null
    }

    /**
     *  返回true 则为沉浸式对话框
     */
    protected open fun onViewCreated(view:View?):Boolean{
        return false
    }

    protected fun getScreenHeight(activity: Activity): Int {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    protected fun getStatusBarHeight(context: Context): Int {
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

    public fun fullScreenBehavior():Boolean{
        var behavior = BottomSheetBehavior.from(findViewById(android.support.design.R.id.design_bottom_sheet))
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed=true
        return true
    }

    public fun fullScreenLayout(view: View?):View?{
        val display = ownerActivity.getWindowManager().getDefaultDisplay()
        val width = display.getWidth()
        val height = display.height/*getScreenHeight(ownerActivity)+getStatusBarHeight(context)*/

        view?.layoutParams= ViewGroup.LayoutParams(width,height)
        return view
    }

    public fun windowTranslucent(){
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
}