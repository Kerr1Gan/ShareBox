package com.ecjtu.sharebox.ui.dialog

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import android.widget.ListAdapter
import com.ecjtu.sharebox.ui.adapter.FileExpandableAdapter
import com.ecjtu.sharebox.ui.adapter.InternetFileExpandableAdapter
import com.ecjtu.sharebox.ui.view.FileExpandableListView

/**
 * Created by KerriGan on 2017/7/16.
 */
class InternetFilePickDialog:FilePickDialog{
    constructor(context: Context, activity: Activity? = null):super(context,activity){

    }
    private var mName:String? =null

    fun setup(name:String,holder:MutableMap<String,TabItemHolder>){
        mName=name
        setTabItemsHolder(holder)
    }

    override fun initView(vg: ViewGroup) {
        super.initView(vg)
        refresh(false)
    }

    override fun initData() {
//        super.initData()
    }

    override fun getFileAdapter(vg: FileExpandableListView): FileExpandableAdapter {
        return InternetFileExpandableAdapter(vg)
    }
}
