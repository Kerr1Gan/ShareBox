package com.flybd.sharebox.util.firebase

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object FirebaseManager {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    fun init(context: Context) {
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)
    }


    fun logEvent(event: String, bundle: Bundle?) {
        with(firebaseAnalytics) {
            logEvent(event, bundle)
        }
    }
//    private val firebaseAnalytics: FirebaseAnalytics by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
//        FirebaseAnalytics.getInstance(this)
//    }

    object Event {
        const val APP_RESUME = "app_resume" // (打开应用)
        const val OPEN_VIDEO_ACTIVITY = "open_video_activity" // (打开视频页面)
        const val OPEN_VIDEO_FRAGMENT = "open_video_fragment" // (打开视频页面)
    }
}