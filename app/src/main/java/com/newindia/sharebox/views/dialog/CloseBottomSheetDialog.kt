package com.newindia.sharebox.views.dialog

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.newindia.sharebox.R

/**
 * Created by KerriGan on 2017/6/5.
 */
open class CloseBottomSheetDialog:BaseBottomSheetDialog,View.OnClickListener{

    constructor(context: Context, activity: Activity? = null):super(context,activity){
        //do nothing
    }

    override fun onCreateView(): View? {
        var bg=layoutInflater.inflate(R.layout.layout_close_dialog,null)
        bg.setBackgroundColor(Color.TRANSPARENT)
        return bg
    }

    override fun onViewCreated(view: View?):Boolean {
        super.onViewCreated(view)
        transparentDialog()
        var v=findViewById(R.id.btn_close)
        v?.setOnClickListener(){
            dismiss()
        }
        return false
    }

    override fun onClick(v: View?) {
        dismiss()
    }
}