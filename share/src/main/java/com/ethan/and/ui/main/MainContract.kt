package com.ethan.and.ui.main

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Message
import com.ethan.and.service.MainService
import com.starwin.ethan.mvp_dagger.mvp.IPresenter
import com.starwin.ethan.mvp_dagger.mvp.IView
import org.ecjtu.easyserver.IAidlInterface
import org.ecjtu.easyserver.server.DeviceInfo

/**
 * Created by hong on 2018/12/15.
 */
class MainContract {
    interface View : IView<Presenter> {
        fun permissionRejected()
        fun updateDeviceInfo(update: Boolean, deviceInfo: MutableList<DeviceInfo>)
        fun updateNetworkInfo(apName: String, isWifi: Boolean, isHotspot: Boolean, state: Int)
        fun onFirstOpen()
    }

    interface Presenter : IPresenter<View> {
        fun registerWifiApReceiver(context: Context)
        fun onCreate(activity: Activity, handler: Handler)
        fun onDestroy(context: Context)
        fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
        fun closeApp()
        fun stopServerService()
        fun startServerService()
        fun handleMessage(msg: Message)
        fun doSearch()
        fun getMainService(): MainService?
        fun getServerService(): IAidlInterface?
        fun go2Setting()
        fun startSearch()
        fun stopSearch()
        fun refresh(isRefresh: Boolean)
        fun isRefreshing(): Boolean
    }
}