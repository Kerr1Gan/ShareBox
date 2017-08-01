package com.ecjtu.sharebox.ui.adapter

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.network.AsyncNetwork
import com.ecjtu.sharebox.network.IRequestCallback
import com.ecjtu.sharebox.ui.dialog.ApDataDialog
import com.ecjtu.sharebox.ui.dialog.FilePickDialog
import com.ecjtu.sharebox.ui.dialog.InternetFilePickDialog
import com.ecjtu.sharebox.ui.dialog.TextItemDialog
import org.ecjtu.easyserver.server.DeviceInfo
import java.io.File
import java.lang.Exception
import java.lang.ref.WeakReference
import java.net.HttpURLConnection

/**
 * Created by Ethan_Xiang on 2017/7/3.
 */
class DeviceRecyclerViewAdapter : RecyclerView.Adapter<DeviceRecyclerViewAdapter.VH>,View.OnClickListener,
View.OnLongClickListener{

    private var mDeviceList: MutableList<DeviceInfo>? = null

    private var mWeakRef: WeakReference<Activity>? =null

    constructor(list: MutableList<DeviceInfo>,activity: Activity) : super() {
        mDeviceList = list
        mWeakRef= WeakReference(activity)
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

        var iconUrl="${info?.ip}:${info?.port}${info?.icon}"
        holder?.itemView?.setTag(R.id.extra_tag,position)
        Glide.with(holder?.itemView?.context).load("http://"+iconUrl).
                apply(RequestOptions().placeholder(R.mipmap.logo)).
                into(holder?.icon)
        holder?.name?.setText(info?.name)

        if(info?.fileMap==null){
            AsyncNetwork().requestDeviceInfo("${info?.ip}:${info?.port}",object :IRequestCallback{
                override fun onSuccess(httpURLConnection: HttpURLConnection?, response: String) {
//                    Info.json2DeviceInfo(JSONObject(response)).apply {
//                        info?.fileMap=fileMap
//                    }
//                    mWeakRef?.get()?.runOnUiThread {
//
//                    }
                }

                override fun onError(httpURLConnection: HttpURLConnection?, exception: Exception) {
                }
            })
        }else{
            //do nothing
        }
    }

    override fun onClick(v: View?) {
        var position=v?.getTag(R.id.extra_tag) as Int
        var deviceInfo=mDeviceList?.get(position)

        AsyncNetwork().requestDeviceInfo("${deviceInfo?.ip}:${deviceInfo?.port}",object :IRequestCallback{
            override fun onSuccess(httpURLConnection: HttpURLConnection?, response: String) {
//                Info.json2DeviceInfo(JSONObject(response)).apply {
//                    deviceInfo?.fileMap=fileMap
//                }
                mWeakRef?.get()?.runOnUiThread {
                    if(mWeakRef?.get()!=null){
                        InternetFilePickDialog(mWeakRef?.get()!!,mWeakRef?.get()).apply {
                            var holders:MutableMap<String, FilePickDialog.TabItemHolder> = mutableMapOf()
                            if(deviceInfo?.fileMap?.entries!=null){
                                for(entry in deviceInfo!!.fileMap!!.entries){
                                    var type=FilePickDialog.string2MediaFileType(entry.key)
                                    var fileList= mutableListOf<File>()
                                    for(child in entry.value){
                                        fileList.add(File(child))
                                    }
                                    var holder=FilePickDialog.TabItemHolder(entry.key,type,
                                            null,
                                            fileList)
                                    holders.put(entry.key,holder)
                                }
                            }
                            setup(deviceInfo?.name!!,holders)
                            show()
                        }
                    }
                }
            }

            override fun onError(httpURLConnection: HttpURLConnection?, exception: Exception) {
                mWeakRef?.get()?.runOnUiThread {
                    Toast.makeText(mWeakRef?.get()!!,"对方还未准备好，请稍后再试",Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onLongClick(v: View?): Boolean {
        var position=v?.getTag(R.id.extra_tag) as Int
        var deviceInfo=mDeviceList?.get(position)
        TextItemDialog(v.context).apply {
            setupItem(arrayOf("详细信息","取消"))
            setOnClickListener { index->
                if(index==0){
                    if(mWeakRef?.get()!=null&&mWeakRef!!.get()!=null){
                        ApDataDialog(v.context,mWeakRef?.get()!!).apply {
                            setup(deviceInfo!!.ip,deviceInfo!!.port)
                        }.show()
                    }
                }
                cancel()
            }
        }.show()

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



