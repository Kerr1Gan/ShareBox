package com.ecjtu.sharebox.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.text.TextUtils
import com.ecjtu.sharebox.R

/**
 * Created by KerriGan on 2017/9/3.
 */
abstract class SimpleNotification(val context: Context) {

    private var mBuilder: NotificationCompat.Builder? = null

    open fun buildNotification(id: Int, title: String, contentText: String, ticker: String, smallIcon: Int = R.mipmap.ic_launcher): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context)
        builder.setContentTitle(title)
        builder.setContentText(contentText)
        builder.setSmallIcon(smallIcon)
        builder.setWhen(System.currentTimeMillis())
        builder.setTicker(ticker)
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        builder.setAutoCancel(true)

        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val ringtoneUri = pref.getString(context.getString(R.string.key_notification_message_ringtone),
                context.getString(R.string.key_default_notification_message_ringtone))
        val vibrate = pref.getBoolean(context.getString(R.string.key_notification_vibrate), false)
        if (!TextUtils.isEmpty(ringtoneUri)) {
            builder.setSound(Uri.parse(ringtoneUri))
        }
        if (vibrate) {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL or NotificationCompat.DEFAULT_VIBRATE)
        }
        mBuilder = builder
        return builder
    }

    open fun fullScreenIntent(builder: NotificationCompat.Builder?, requestCode: Int, intent: Intent?, highPriority: Boolean = false) {
        if (intent == null) {
            builder?.setFullScreenIntent(null, highPriority)
        } else {
            val pending = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            builder?.setFullScreenIntent(pending, highPriority)
        }
    }

    open fun sendNotification(id: Int, builder: NotificationCompat.Builder?, tag: String? = null) {
        NotificationManagerCompat.from(context).notify(tag, id, builder?.build()!!)
    }

    open fun cancelNotification(id: Int, tag: String? = null) {
        NotificationManagerCompat.from(context).cancel(tag, id)
    }

    open fun cancelAllNotification() {
        NotificationManagerCompat.from(context).cancelAll()
    }

    abstract fun send(tag: String? = null)

    abstract fun cancel(tag: String? = null)

    open fun getCurrentBuilder(): NotificationCompat.Builder? {
        return mBuilder
    }
}