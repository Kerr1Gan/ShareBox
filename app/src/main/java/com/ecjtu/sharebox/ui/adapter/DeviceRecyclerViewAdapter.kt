package com.ecjtu.sharebox.ui.adapter

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ecjtu.sharebox.MainApplication
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.domain.DeviceInfo
import com.ecjtu.sharebox.presenter.MainActivityDelegate
import com.ecjtu.sharebox.ui.dialog.TextItemDialog

/**
 * Created by Ethan_Xiang on 2017/7/3.
 */
class DeviceRecyclerViewAdapter : RecyclerView.Adapter<DeviceRecyclerViewAdapter.VH>,View.OnClickListener,View.OnLongClickListener{

    private var mDeviceList: MutableList<DeviceInfo>? = null

    constructor(list: MutableList<DeviceInfo>) : super() {
        mDeviceList = list
    }

    override fun getItemCount(): Int {
        return mDeviceList?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH? {
        var v = LayoutInflater.from(parent?.context).inflate(R.layout.layout_device_item, parent, false)
        v.setOnClickListener(this)
        v.setOnLongClickListener(this)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH?, position: Int) {
        var info=mDeviceList?.get(position)

        holder?.itemView?.setTag(R.id.extra_tag,position)
        Glide.with(holder?.itemView?.context).load(info?.icon).
                apply(RequestOptions().placeholder(R.mipmap.logo)).
                into(holder?.icon)
        holder?.name?.setText(info?.name)
    }

    override fun onClick(v: View?) {
        var position=v?.getTag(R.id.extra_tag)
    }

    override fun onLongClick(v: View?): Boolean {
        var position=v?.getTag(R.id.extra_tag) as Int

        var deviceInfo=mDeviceList?.get(position)

        var items= arrayOf("网络信息","取消")

        TextItemDialog(v?.context).apply {
            setupItem(items)
            setOnClickListener {index->
                if(index==0){

                }
                cancel()
            }
        }

        return true
    }

    class VH(item: View) : RecyclerView.ViewHolder(item) {
        var icon: ImageView? = null
        var name: TextView? =null
        var thumb: ImageView? =null
        var fileCount: TextView? =null
        init {
            icon=item.findViewById(R.id.icon) as ImageView
            name=item.findViewById(R.id.name) as TextView
            thumb=item.findViewById(R.id.content) as ImageView
            fileCount=item.findViewById(R.id.file_count) as TextView
        }
    }
}



