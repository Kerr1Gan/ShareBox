package com.ecjtu.sharebox.ui.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.ecjtu.sharebox.R

/**
 * Created by Ethan_Xiang on 2017/10/27.
 */
class DeviceRecyclerInfo(item: View) : RecyclerView.ViewHolder(item) {
    var icon: ImageView? = null
    var name: TextView? = null
    var thumb: ImageView? = null
    var fileCount: TextView? = null

    init {
        icon = item.findViewById<View>(R.id.icon) as ImageView
        name = item.findViewById<View>(R.id.name) as TextView
        thumb = item.findViewById<View>(R.id.content) as ImageView
        fileCount = item.findViewById<View>(R.id.file_count) as TextView
    }
}