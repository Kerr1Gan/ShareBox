package com.ecjtu.sharebox.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ecjtu.sharebox.ui.activity.MainActivity
import com.ecjtu.sharebox.util.activity.ActivityUtil

/**
 * Created by Ethan_Xiang on 2017/9/5.
 */
class ServerComingNotification(context: Context) : SimpleNotification(context) {

    companion object {
        const val ID = 0x100
    }

    fun buildServerComingNotification(title: String, content: String, ticker: String): ServerComingNotification {
        super.buildNotification(ID, title, content, ticker)
        fullScreenIntent(getCurrentBuilder(), 0, null)
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        getCurrentBuilder()?.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
        return this
    }

    override fun send(tag: String?) {
        super.sendNotification(ID, getCurrentBuilder(), tag)
    }

    override fun cancel(tag: String?) {
        super.cancelNotification(ID, tag)
    }

}