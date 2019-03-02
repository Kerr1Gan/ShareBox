package com.ethan.and.ui.dialog

import android.app.Activity
import android.content.Context
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import com.flybd.sharebox.R
import com.ethan.and.ui.adapter.FileExpandableAdapter
import com.ethan.and.ui.adapter.InternetFileExpandableAdapter
import com.ethan.and.ui.holder.TabItemInfo
import com.ethan.and.ui.widget.FileExpandableListView
import org.ecjtu.easyserver.server.DeviceInfo

/**
 * Created by KerriGan on 2017/7/16.
 */
class InternetFilePickDialog : FilePickDialog {

    private val mDeviceInfo: DeviceInfo

    constructor(context: Context, activity: Activity? = null, deviceInfo: DeviceInfo) : super(context, activity) {
        mDeviceInfo = deviceInfo
    }

    private var mName: String? = null

    fun setup(name: String, holder: MutableMap<String, TabItemInfo>) {
        mName = name
        setTabItemsHolder(holder)
    }

    override fun initView(vg: ViewGroup) {
        super.initView(vg)
        refresh(false)
        var toolbar = vg.findViewById<View>(R.id.toolbar) as Toolbar
        toolbar.title = mName
        toolbar.menu.clear()
    }

    override fun initData() {
//        super.initData()
    }

    override fun getFileAdapter(vg: FileExpandableListView, title: String): FileExpandableAdapter {
        return InternetFileExpandableAdapter(vg).apply { setDeviceInfo(mDeviceInfo) }
    }

    override fun isLoadCache(): Boolean {
        return false
    }
}
