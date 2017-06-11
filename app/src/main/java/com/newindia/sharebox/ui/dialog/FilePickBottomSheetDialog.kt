package com.newindia.sharebox.ui.dialog

import android.app.Activity
import android.content.Context
import com.newindia.sharebox.R
import android.support.design.widget.BottomSheetBehavior
import android.view.*


/**
 * Created by KerriGan on 2017/6/2.
 */
class FilePickBottomSheetDialog:BaseBottomSheetDialog{

    constructor(context: Context,activity: Activity? = null):super(context,activity){

    }

    override fun initializeDialog() {
        super.initializeDialog()
    }

    override fun onCreateView(): View? {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        var vg= layoutInflater.inflate(R.layout.layout_main_activity_data,null)

        val display = ownerActivity.getWindowManager().getDefaultDisplay()
        val width = display.getWidth()
        val height = display.height/*getScreenHeight(ownerActivity)+getStatusBarHeight(context)*/

        vg.layoutParams=ViewGroup.LayoutParams(width,height)
        return vg
    }

    override fun onViewCreated(view: View?):Boolean {
        super.onViewCreated(view)
        var behavior = BottomSheetBehavior.from(findViewById(android.support.design.R.id.design_bottom_sheet))
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed=true
        return true
    }
}