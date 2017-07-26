package com.ecjtu.sharebox.ui.dialog

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.ecjtu.sharebox.R

/**
 * Created by KerriGan on 2017/7/22.
 */
class ProgressDialog : CloseBottomSheetDialog {

    constructor(context: Context, activity: Activity? = null) : super(context, activity,R.style.WhiteProgressDialog) {
        setCanceledOnTouchOutside(false)
    }

    override fun onCreateView(): View? {
        var container = RelativeLayout(context)
        fullScreenLayout(container)
        var root = super.onCreateView()
        windowTranslucent()
        var child = layoutInflater.inflate(R.layout.dialog_progress, root as ViewGroup, false) as ViewGroup
        root.addView(child)
        var relParam = RelativeLayout.LayoutParams(-1, -2)
        relParam.addRule(RelativeLayout.CENTER_IN_PARENT)
        container.addView(root, relParam)
        setTitle("正在加载", container)
        return container
    }

    override fun onViewCreated(view: View?): Boolean {
        super.onViewCreated(view)
        return fullScreenBehavior()
    }
}