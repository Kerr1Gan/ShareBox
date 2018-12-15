package com.ecjtu.sharebox.ui.main

import android.content.Context
import com.starwin.ethan.mvp_dagger.mvp.IPresenter
import com.starwin.ethan.mvp_dagger.mvp.IView

/**
 * Created by hong on 2018/12/15.
 */
class MainContract {
    interface View : IView<Presenter> {

    }

    interface Presenter : IPresenter<View> {
        fun registerWifiApReceiver(context: Context)
        fun onCreate(context: Context)
        fun onDestroy(context: Context)
    }
}