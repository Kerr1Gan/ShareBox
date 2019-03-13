package com.ethan.and.ui.main

import android.content.Context
import android.os.Handler
import com.starwin.ethan.mvp_dagger.mvp.IPresenter
import com.starwin.ethan.mvp_dagger.mvp.IView

/**
 * Created by hong on 2018/12/15.
 */
class MainContract {
    interface View : IView<Presenter> {
        fun permissionRejected()
    }

    interface Presenter : IPresenter<View> {
        fun registerWifiApReceiver(context: Context)
        fun onCreate(context: Context, handler: Handler)
        fun onDestroy(context: Context)
        fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    }
}