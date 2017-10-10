package com.ecjtu.sharebox.ui.adapter

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.ecjtu.componentes.RotateNoCreateActivity
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.ui.dialog.FilePickDialog
import com.ecjtu.sharebox.ui.fragment.IjkVideoFragment
import com.ecjtu.sharebox.ui.widget.FileExpandableListView
import com.ecjtu.sharebox.util.cache.CacheUtil
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
        var baseUrl = ""
        var localThumb = thumb
        if (type == FileUtil.MediaFileType.APP) {
            baseUrl = "http://${mDeviceInfo?.getIp()}:${mDeviceInfo?.port}/api/Apk$localThumb"
            Glide.with(context).load(baseUrl).into(icon)
            return
        } else if (type == FileUtil.MediaFileType.IMG) {
            baseUrl = "http://${mDeviceInfo?.getIp()}:${mDeviceInfo?.port}/api/Image$localThumb"
        } else {
            baseUrl = "http://${mDeviceInfo?.getIp()}:${mDeviceInfo?.port}/api/Cache${CacheUtil.getCachePath(context, localThumb)}"
        }
        val opt = RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
        super.setGroupViewThumb(type, baseUrl, icon, text, opt)
    }

    override fun setChildViewThumb(type: FileUtil.MediaFileType?, f: String?, icon: ImageView?) {
        var baseUrl = ""
        var localThumb = f
        if (type == FileUtil.MediaFileType.APP) {
            baseUrl = "http://${mDeviceInfo?.getIp()}:${mDeviceInfo?.port}/api/Apk$localThumb"
            Glide.with(context).load(baseUrl).into(icon)
            return
        } else if (type == FileUtil.MediaFileType.IMG) {
            baseUrl = "http://${mDeviceInfo?.getIp()}:${mDeviceInfo?.port}/api/Image$localThumb"
        } else {
            baseUrl = "http://${mDeviceInfo?.getIp()}:${mDeviceInfo?.port}/api/Cache${CacheUtil.getCachePath(context, localThumb)}"
        }
        val opt = RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
        super.setChildViewThumb(type, baseUrl, icon, opt)
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
