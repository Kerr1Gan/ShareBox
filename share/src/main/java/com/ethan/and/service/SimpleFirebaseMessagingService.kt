package com.ethan.and.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.text.TextUtils
import android.util.Log
import com.ethan.and.ui.main.MainActivity
import com.flybd.sharebox.BuildConfig
import com.flybd.sharebox.R
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.lang.Exception
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build


class SimpleFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val TAG = "FirebaseMsgService"
        const val SP_FIREBASE_TOKEN = "shared_preference_firebase_token"
        private const val NOTIFICATION_ID = 100
    }

    @SuppressLint("ApplySharedPref")
    override fun onNewToken(s: String?) {
        super.onNewToken(s)
        try {
            Log.i(TAG, "FCM token " + FirebaseInstanceId.getInstance().instanceId.result?.token)
            val pref = baseContext.getSharedPreferences(TAG, Context.MODE_PRIVATE)
            pref.edit().putString(SP_FIREBASE_TOKEN, s).commit()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        Log.i(TAG, "receive msg " + remoteMessage?.data.toString())
        if (remoteMessage != null && remoteMessage.notification != null) {
            Log.i(TAG, "receive notification title " + remoteMessage.notification!!.title)
            Log.i(TAG, "receive notification body " + remoteMessage.notification!!.body)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(BuildConfig.APPLICATION_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)

                val builder = Notification.Builder(this, BuildConfig.APPLICATION_ID)
                builder.setContentTitle(remoteMessage.notification!!.title)
                builder.setContentText(remoteMessage.notification!!.body)
                builder.setSmallIcon(R.mipmap.ic_launcher)
                builder.setWhen(System.currentTimeMillis())
                builder.setTicker(remoteMessage.notification!!.title!!)
                builder.setVisibility(Notification.VISIBILITY_PUBLIC)
                builder.setAutoCancel(true)
                val intent = Intent(this, MainActivity::class.java)
                builder.setContentIntent(PendingIntent.getActivity(this, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)) //requestCode 不能为0 否则MainActivity 将重建
                manager.notify(TAG, NOTIFICATION_ID, builder.build())
            } else {
                val builder = NotificationCompat.Builder(this, BuildConfig.APPLICATION_ID)
                builder.setContentTitle(remoteMessage.notification!!.title)
                builder.setContentText(remoteMessage.notification!!.body)
                builder.setSmallIcon(R.mipmap.ic_launcher)
                builder.setWhen(System.currentTimeMillis())
                builder.setTicker(remoteMessage.notification!!.title!!)
                builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                builder.setAutoCancel(true)
                val intent = Intent(this, MainActivity::class.java)
                builder.setContentIntent(PendingIntent.getActivity(this, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)) //requestCode 不能为0 否则MainActivity 将重建
                NotificationManagerCompat.from(this).notify(TAG, NOTIFICATION_ID, builder.build())
            }
        } else if (!TextUtils.isEmpty(remoteMessage?.data.toString())) {
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.i(TAG, "onDeletedMessages")
    }

}