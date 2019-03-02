package com.ethan.and.ui.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.Fragment
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.ecjtu.sharebox.R
import com.ethan.and.ui.fragment.SimpleDialogFragment


/**
 * Created by KerriGan on 2017/6/2.
 */

abstract class BaseBottomSheetDialog : BottomSheetDialog, SimpleDialogFragment.IActivityResult {

    private var mFragmentHost: Fragment? = null

    constructor(context: Context, activity: Activity? = null, theme: Int = 0) : super(context, theme) {
        //do nothing
        if (activity != null)
            ownerActivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var immersive = init()
        if (ownerActivity == null)
            return
        val screenHeight = getScreenHeight(ownerActivity)
        val statusBarHeight = getStatusBarHeight(context)
        val dialogHeight = if (immersive) screenHeight else screenHeight - statusBarHeight
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, if (dialogHeight == 0) ViewGroup.LayoutParams.MATCH_PARENT else dialogHeight)
    }

    private fun init(): Boolean {
        initializeDialog()
        var view = onCreateView()
        setContentView(view)
        return onViewCreated(view)
    }

    protected open fun initializeDialog() {
//        requestWindowFeature(FEATURE_NO_TITLE)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

    protected open fun onCreateView(): View? {
        var behavior = BottomSheetBehavior.from(findViewById<View>(android.support.design.R.id.design_bottom_sheet))
        return null
    }

    /**
     *  返回true 则为沉浸式对话框
     */
    protected open fun onViewCreated(view: View?): Boolean {
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

    protected fun transparentDialog() {
        getWindow().findViewById<View>(R.id.design_bottom_sheet)
                .setBackgroundResource(android.R.color.transparent)
    }

    public fun fullScreenBehavior(): Boolean {
        var behavior = BottomSheetBehavior.from(findViewById<View>(android.support.design.R.id.design_bottom_sheet))
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        return true
    }

    public fun fullScreenLayout(view: View?): View? {
        val display = ownerActivity.getWindowManager().getDefaultDisplay()
        val width = display.getWidth()
        val height = display.height/*getScreenHeight(ownerActivity)+getStatusBarHeight(context)*/
        var layoutParams = view?.layoutParams ?: ViewGroup.LayoutParams(width, height)
        view?.layoutParams = layoutParams.apply { this?.width = width;this?.height = height }
        return view
    }

    public fun windowTranslucent() {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    }

    override fun setFragmentHost(fragment: Fragment?) {
        mFragmentHost = fragment
    }

    override fun getFragmentHost(): Fragment? {
        return mFragmentHost
    }

}