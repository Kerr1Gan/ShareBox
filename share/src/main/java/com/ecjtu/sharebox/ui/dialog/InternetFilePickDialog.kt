package com.ecjtu.sharebox.ui.dialog

import android.app.Activity
import android.content.Context
import android.support.v7.widget.Toolbar
import android.view.ViewGroup
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.ui.adapter.FileExpandableAdapter
import com.ecjtu.sharebox.ui.adapter.InternetFileExpandableAdapter
import com.ecjtu.sharebox.ui.holder.TabItemProperty
import com.ecjtu.sharebox.ui.widget.FileExpandableListView
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

    fun setup(name: String, holder: MutableMap<String, TabItemProperty>) {
        mName = name
        setTabItemsHolder(holder)
    }

    override fun initView(vg: ViewGroup) {
        super.initView(vg)
        refresh(false)
        var toolbar = vg.findViewById(R.id.toolbar) as Toolbar
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
