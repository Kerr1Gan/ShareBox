package com.ecjtu.sharebox.notification

import android.content.Context
import android.support.v7.app.NotificationCompat

/**
 * Created by KerriGan on 2017/9/3.
 */
class SimpleNotificationManager(val context: Context) {

    private var mNotificationManagerCompat: NotificationCompat? = null

    init {
        mNotificationManagerCompat = NotificationCompat()
    }

}