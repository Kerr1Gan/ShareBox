package com.ecjtu.sharebox.ui.adapter

import android.view.View
import android.view.ViewGroup
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.ui.dialog.FilePickDialog
import com.ecjtu.sharebox.ui.view.FileExpandableListView
import org.ecjtu.easyserver.server.DeviceInfo

/**
 * Created by KerriGan on 2017/7/16.
 */
class InternetFileExpandableAdapter(expandableListView: FileExpandableListView) :
        FileExpandableAdapter(expandableListView) {

    private var mDeviceInfo: DeviceInfo? = null

    override fun initData(holder: FilePickDialog.TabItemHolder?, oldCache: List<VH>?) {
        super.initData(holder, oldCache)
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var ret = super.getGroupView(groupPosition, isExpanded, convertView, parent)
        ret.findViewById(R.id.select_all).visibility = View.INVISIBLE
        return ret
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var ret = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent)
        ret.findViewById(R.id.check_box).visibility = View.INVISIBLE
        return ret
    }

    override fun onClick(v: View?) {
        var tag = v?.getTag()
        if (tag != null && tag is String) {
            var path = java.lang.String(tag)
            openFile("${mDeviceInfo?.ip}:${mDeviceInfo?.port}/API/File/${path.hashCode()}")
            return
        }
        super.onClick(v)
    }

    override fun setup(title: String) {
        //do nothing
    }

    fun setDeviceInfo(deviceInfo: DeviceInfo) {
        mDeviceInfo = deviceInfo
    }
}
