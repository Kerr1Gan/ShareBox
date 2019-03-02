package com.ethan.and.ui.adapter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.ecjtu.componentes.activity.ActionBarFragmentActivity
import com.ecjtu.componentes.activity.RotateNoCreateActivity
import com.ecjtu.sharebox.R
import com.ethan.and.ui.dialog.TextItemDialog
import com.ethan.and.ui.fragment.IjkVideoFragment
import com.ethan.and.ui.fragment.WebViewFragment
import com.ethan.and.ui.holder.FileExpandableInfo
import com.ethan.and.ui.holder.TabItemInfo
import com.ethan.and.ui.widget.FileExpandableListView
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

    override fun initData(holder: TabItemInfo?, oldCache: List<FileExpandableInfo>?) {
        super.initData(holder, oldCache)
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var ret = super.getGroupView(groupPosition, isExpanded, convertView, parent)
        ret.findViewById<View>(R.id.select_all).visibility = View.INVISIBLE
        return ret
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var ret = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent)
        ret.findViewById<View>(R.id.check_box).visibility = View.INVISIBLE
        return ret
    }

    override fun onClick(v: View?) {
        var tag = v?.getTag()
        if (tag != null && tag is String) {
            openFile(tag)
            return
        }
        super.onClick(v)
    }

    override fun onLongClick(v: View?): Boolean {
        v?.let {
            val context = v.context
            if(v.getTag() is String){
                val dlg = TextItemDialog(context)
                var path = v.getTag() as String
                val type = FileUtil.getMediaFileTypeByName(path)
                if (type === FileUtil.MediaFileType.MOVIE) {
                    dlg.setupItem(arrayOf(context.getString(R.string.open), context.getString(R.string.cancel)))
                    dlg.setOnClickListener { integer ->
                        if (integer == 0) {
                            openFile(path)
                        } else if (integer == 1) {
                        }
                        dlg.cancel()
                    }
                } else {
                    dlg.setupItem(arrayOf(context.getString(R.string.open), context.getString(R.string.open_by_others), context.getString(R.string.cancel)))
                    dlg.setOnClickListener { integer ->
                        if (integer == 0) {
                            val bundle = WebViewFragment.openWithMIME(path)
                            val intent = ActionBarFragmentActivity.newInstance(context, WebViewFragment::class.java, bundle)
                            context.startActivity(intent)
                        } else if (integer == 1) {
                            openFile(path)
                        }
                        dlg.cancel()
                    }
                }
                dlg.show()
            }
        }
        return true
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
        val local = "http://${mDeviceInfo?.ip}:${mDeviceInfo?.port}/File/${HashUtil.BKDRHash(path!!)}"
        if (mTabHolder.type === FileUtil.MediaFileType.MOVIE) {
            val bundle = Bundle()
            bundle.putString(IjkVideoFragment.EXTRA_URI_PATH, local)
            val i = RotateNoCreateActivity.newInstance(context, IjkVideoFragment::class.java, bundle)
            context.startActivity(i)
        } else {
            try {
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.action = Intent.ACTION_VIEW
                val uri = Uri.parse(local)
                intent.setData(uri)
                context.startActivity(intent)
            } catch (ignore: Exception) {
            }
        }
    }
}
