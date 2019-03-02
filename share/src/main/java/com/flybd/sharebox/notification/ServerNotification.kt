package com.flybd.sharebox.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.flybd.sharebox.R
import com.ethan.and.ui.main.MainActivity

/**
 * Created by Ethan_Xiang on 2017/9/5.
 */
class ServerNotification(context: Context) : SimpleNotification(context) {

    companion object {
        @JvmField
        var ID = 0x100
    }

    fun buildServerNotification(title: String, content: String, ticker: String): ServerNotification {
        super.buildNotification(ID++, title, content, ticker, R.mipmap.ic_launcher)
        val intent = Intent(context, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//        fullScreenIntent(getCurrentBuilder(), 100, intent)
        getCurrentBuilder()?.setContentIntent(PendingIntent.getActivity(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)) //requestCode 不能为0 否则MainActivity 将重建
        return this
    }

    override fun send(tag: String?) {
        super.sendNotification(ID, getCurrentBuilder(), tag)
    }

    override fun cancel(tag: String?) {
        super.cancelNotification(ID, tag)
    }

}