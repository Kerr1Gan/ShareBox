package com.ecjtu.sharebox.ui.main

import android.content.Context

/**
 * Created by hong on 2018/12/15.
 */
class MainPresenter : MainContract.Presenter {
    override fun onCreate(context: Context) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy(context: Context) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun registerWifiApReceiver(context: Context) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var view: MainContract.View? = null

    override fun takeView(view: MainContract.View?) {
        this.view = view
    }

    override fun dropView() {
        this.view = null
    }


}