package com.ecjtu.sharebox.ui.adapter

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.domain.DeviceInfo
import com.ecjtu.sharebox.presenter.MainActivityDelegate

/**
 * Created by Ethan_Xiang on 2017/7/3.
 */
class DeviceRecyclerViewAdapter : RecyclerView.Adapter<DeviceRecyclerViewAdapter.VH> {

    private var mDeviceList: MutableList<DeviceInfo>? = null

    constructor(list: MutableList<DeviceInfo>) : super() {
        mDeviceList = list
    }

    override fun getItemCount(): Int {
        return mDeviceList?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH? {
        var v = LayoutInflater.from(parent?.context).inflate(R.layout.layout_device_item, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH?, position: Int) {
        var info=mDeviceList?.get(position)
        TextUtils.isEmpty(info?.icon).let {

        }
        holder?.icon?.setImageResource(R.mipmap.ic_launcher)
        holder?.name?.setText(info?.name)
    }


    class VH(item: View) : RecyclerView.ViewHolder(item) {
        var icon: ImageView? = null
        var name: TextView? =null
        init {
            icon=item.findViewById(R.id.icon) as ImageView
            name=item.findViewById(R.id.name) as TextView
        }
    }
}



