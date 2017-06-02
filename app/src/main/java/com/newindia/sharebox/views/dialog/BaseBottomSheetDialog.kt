package com.newindia.sharebox.views.dialog

import android.content.Context
import android.support.design.widget.BottomSheetDialog
import android.view.View

/**
 * Created by KerriGan on 2017/6/2.
 */

abstract class BaseBottomSheetDialog:BottomSheetDialog{

    constructor(context: Context):super(context){
        init()
    }

    private fun init(){
        initializeDialog()
        var view=onCreate()
        setContentView(view)
        onViewCreated(view)
    }

    protected open fun initializeDialog(){
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

    protected open fun onCreate():View?{
        return null
    }

    protected open fun onViewCreated(view:View?){

    }

}