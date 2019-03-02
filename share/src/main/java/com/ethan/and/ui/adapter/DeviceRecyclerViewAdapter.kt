package com.ethan.and.ui.adapter

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.ecjtu.netcore.RequestManager
import com.ecjtu.netcore.network.IRequestCallback
import com.ecjtu.netcore.network.IRequestCallbackV2
import com.ecjtu.sharebox.R
import com.ethan.and.ui.dialog.ApDataDialog
import com.ethan.and.ui.dialog.InternetFilePickDialog
import com.ethan.and.ui.dialog.TextItemDialog
import com.ethan.and.ui.fragment.SimpleDialogFragment
import com.ethan.and.ui.holder.DeviceRecyclerInfo
import com.ethan.and.ui.holder.TabItemInfo
import com.ecjtu.sharebox.util.file.FileUtil
import org.ecjtu.easyserver.server.ConversionFactory
import org.ecjtu.easyserver.server.DeviceInfo
import org.json.JSONObject
import java.lang.Exception
import java.lang.ref.WeakReference
import java.net.HttpURLConnection

/**
 * Created by Ethan_Xiang on 2017/7/3.
 */
class DeviceRecyclerViewAdapter : RecyclerView.Adapter<DeviceRecyclerInfo>, View.OnClickListener,
        View.OnLongClickListener {

    private var mDeviceList: MutableList<DeviceInfo>? = null

    private var mWeakRef: WeakReference<Activity>? = null

    constructor(list: MutableList<DeviceInfo>, activity: Activity) : super() {
        mDeviceList = list
        mWeakRef = WeakReference(activity)
    }

    override fun getItemCount(): Int {
        return mDeviceList?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DeviceRecyclerInfo? {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.layout_device_item, parent, false)
        v.setOnClickListener(this)
        v.setOnLongClickListener(this)
        return DeviceRecyclerInfo(v)
    }

    override fun onBindViewHolder(holder: DeviceRecyclerInfo?, position: Int) {
        val info = mDeviceList?.get(position)

        val iconUrl = "${info?.ip}:${info?.port}${info?.icon}"
        holder?.itemView?.setTag(R.id.extra_tag, position)
        Glide.with(holder?.itemView?.context).load("http://" + iconUrl).
                apply(RequestOptions().placeholder(R.drawable.ic_boy).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)).
                into(holder?.icon) // 规避缓存机制导致图片不刷新

        holder?.name?.setText(info?.name)

        if (info?.fileMap == null) {
            RequestManager.requestDeviceInfo("${info?.ip}:${info?.port}", object : IRequestCallback {
                override fun onSuccess(httpURLConnection: HttpURLConnection?, response: String) {
                    ConversionFactory.json2DeviceInfo(JSONObject(response)).apply {
                        info?.fileMap = fileMap
                    }
//                    mWeakRef?.get()?.runOnUiThread {
//
//                    }
                }
            })
        } else {
            //do nothing
        }
    }

    override fun onClick(v: View?) {
        val position = v?.getTag(R.id.extra_tag) as Int
        val deviceInfo = mDeviceList?.get(position)

        RequestManager.requestDeviceInfo("${deviceInfo?.ip}:${deviceInfo?.port}", object : IRequestCallbackV2 {
            override fun onSuccess(httpURLConnection: HttpURLConnection?, response: String) {
                ConversionFactory.json2DeviceInfo(JSONObject(response)).apply {
                    deviceInfo?.fileMap = fileMap
                }
                mWeakRef?.get()?.runOnUiThread {
                    if (mWeakRef?.get() != null) {
                        InternetFilePickDialog(mWeakRef?.get()!!, mWeakRef?.get(), deviceInfo!!).apply {
                            val holders: MutableMap<String, TabItemInfo> = mutableMapOf()
                            if (deviceInfo.fileMap?.entries != null) {
                                for (entry in deviceInfo.fileMap!!.entries) {
                                    val type = FileUtil.string2MediaFileType(entry.key)
                                    val holder = TabItemInfo(entry.key, type,
                                            null,
                                            entry.value)
                                    holders.put(entry.key, holder)
                                }
                            }
                            setup(deviceInfo.name!!, holders)
                            show()
                        }
                    }
                }
            }

            override fun onError(httpURLConnection: HttpURLConnection?, exception: Exception) {
                mWeakRef?.get()?.runOnUiThread {
                    Toast.makeText(mWeakRef?.get()!!, R.string.client_has_not_yet_ready_try_later, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onLongClick(v: View?): Boolean {
        val position = v?.getTag(R.id.extra_tag) as Int
        val deviceInfo = mDeviceList?.get(position)
        TextItemDialog(v.context).apply {
            setupItem(arrayOf(v.context.getString(R.string.details), v.context.getString(R.string.cancel)))
            setOnClickListener { index ->
                if (index == 0) {
                    if (mWeakRef?.get() != null && mWeakRef!!.get() != null) {
                        val activity = mWeakRef?.get()
                        if (activity is FragmentActivity) {
                            SimpleDialogFragment(ApDataDialog(mWeakRef?.get()!!).apply {
                                setup(deviceInfo!!.ip, deviceInfo.port)
                            }).show(activity.supportFragmentManager, "ap_data_dialog")
                        } else {
                            ApDataDialog(mWeakRef?.get()!!).apply {
                                setup(deviceInfo!!.ip, deviceInfo.port)
                            }.show()
                        }
                    }
                }
                cancel()
            }
        }.show()

        return true
    }
}



