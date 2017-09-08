package com.ecjtu.sharebox.ui.adapter

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.ui.activity.RotateNoCreateActivity
import com.ecjtu.sharebox.ui.dialog.FilePickDialog
import com.ecjtu.sharebox.ui.fragment.IjkVideoFragment
import com.ecjtu.sharebox.ui.widget.FileExpandableListView
import com.ecjtu.sharebox.util.file.FileUtil
import com.ecjtu.sharebox.util.hash.HashUtil
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
            openFile("http://${mDeviceInfo?.ip}:${mDeviceInfo?.port}/File/${HashUtil.BKDRHash(tag)}")
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

    override fun setGroupViewThumb(type: FileUtil.MediaFileType?, thumb: String?, icon: ImageView?, text: TextView?) {
        val baseUrl = "http://${mDeviceInfo?.getIp()}:${mDeviceInfo?.port}/API/Cache$thumb"
        super.setGroupViewThumb(type, baseUrl, icon, text)
    }

    override fun setChildViewThumb(type: FileUtil.MediaFileType?, f: String?, icon: ImageView?) {
        val baseUrl = "http://${mDeviceInfo?.getIp()}:${mDeviceInfo?.port}/API/Cache$f"
        super.setChildViewThumb(type, baseUrl, icon)
    }

    override fun openFile(path: String?) {
        if (mTabHolder.type === FileUtil.MediaFileType.MOVIE) {
            val bundle = Bundle()
            bundle.putString(IjkVideoFragment.EXTRA_URI_PATH, path)
            val i = RotateNoCreateActivity.newInstance(context, IjkVideoFragment::class.java, bundle)
            context.startActivity(i)
        }
    }
}
