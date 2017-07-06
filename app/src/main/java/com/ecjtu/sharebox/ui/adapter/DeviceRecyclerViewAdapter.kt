package com.ecjtu.sharebox.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.ecjtu.sharebox.domain.DeviceInfo
import com.ecjtu.sharebox.presenter.MainActivityDelegate

/**
 * Created by Ethan_Xiang on 2017/7/3.
 */
class DeviceRecyclerViewAdapter: RecyclerView.Adapter<DeviceRecyclerViewAdapter.VH>{

    private var mDeviceList:MutableList<DeviceInfo>? =null

    constructor(list: MutableList<DeviceInfo>):super(){
        mDeviceList=list
    }

    override fun getItemCount(): Int {
        return mDeviceList?.size ?:0
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH? {
        throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(holder: VH?, position: Int) {
        throw UnsupportedOperationException()
    }



    class VH(item:View):RecyclerView.ViewHolder(item){

    }
}



