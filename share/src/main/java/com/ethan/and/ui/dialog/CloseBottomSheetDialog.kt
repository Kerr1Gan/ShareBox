package com.ethan.and.ui.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.flybd.sharebox.R

/**
 * Created by KerriGan on 2017/6/5.
 */
open class CloseBottomSheetDialog : BaseBottomSheetDialog, View.OnClickListener {

    constructor(context: Context, activity: Activity? = null, theme: Int = 0) : super(context, activity, theme) {
        //do nothing
    }

    override fun onCreateView(): View? {
        var bg = layoutInflater.inflate(R.layout.dialog_close, null)
        bg.setBackgroundColor(Color.TRANSPARENT)
        return bg
    }

    override fun onViewCreated(view: View?): Boolean {
        super.onViewCreated(view)
        transparentDialog()
        var v = findViewById<View>(R.id.btn_close)

        (v?.parent as ViewGroup).setOnClickListener() {
            dismiss()
        }
        return false
    }

    override fun onClick(v: View?) {
        dismiss()
    }

    open fun setTitle(title: String, vg: ViewGroup? = null) {
        if (vg == null)
            (findViewById<View>(R.id.text_title) as TextView).setText(title)
        else
            (vg.findViewById<View>(R.id.text_title) as TextView).setText(title)
    }
}