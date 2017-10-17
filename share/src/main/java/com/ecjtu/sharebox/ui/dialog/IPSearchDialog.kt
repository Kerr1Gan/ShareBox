package com.ecjtu.sharebox.ui.dialog

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import com.ecjtu.sharebox.R

/**
 * Created by Ethan_Xiang on 2017/10/17.
 */
class IPSearchDialog(activity: Activity) : CloseBottomSheetDialog(activity, activity,R.style.BottomSheetDialogStyle) {

    override fun onCreateView(): View? {
        val supView = super.onCreateView()
        setTitle("IP搜索", supView as ViewGroup)
        val child = layoutInflater.inflate(R.layout.layout_ip_search_dialog, supView, false)
        supView.addView(child)
        return supView
    }

    override fun onViewCreated(view: View?): Boolean {

        return super.onViewCreated(view)
    }
}
