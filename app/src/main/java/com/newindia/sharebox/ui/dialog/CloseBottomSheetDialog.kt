package com.newindia.sharebox.ui.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.newindia.sharebox.R

/**
 * Created by KerriGan on 2017/6/5.
 */
open class CloseBottomSheetDialog:BaseBottomSheetDialog,View.OnClickListener{

    constructor(context: Context, activity: Activity? = null):super(context,activity){
        //do nothing
    }

    override fun onCreateView(): View? {
        var bg=layoutInflater.inflate(R.layout.dialog_close,null)
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

    open fun setTitle(title:String,vg:ViewGroup? =null){
        if(vg==null)
            (findViewById(R.id.text_title) as TextView).setText(title)
        else
            (vg.findViewById(R.id.text_title) as TextView).setText(title)
    }
}