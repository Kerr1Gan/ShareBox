package com.ecjtu.sharebox.ui.dialog

import android.app.Activity
import android.content.Context

/**
 * Created by KerriGan on 2017/7/16.
 */
class InternetFilePickDialog:FilePickDialog{
    constructor(context: Context, activity: Activity? = null):super(context,activity){

    }

    private var mName:String? =null

    override fun initData() {
        super.initData()
    }

    fun setup(name:String,holder:MutableMap<String,TabItemHolder>){
        mName=name
        setTabItemsHolder(holder)
    }
}
