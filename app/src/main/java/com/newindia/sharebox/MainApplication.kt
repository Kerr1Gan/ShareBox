package com.newindia.sharebox

import android.app.Application

/**
 * Created by KerriGan on 2017/6/9 0009.
 */
class MainApplication:Application(){


    private val mSavedStateInstance=HashMap<String,Unit>()

    override fun onCreate() {
        super.onCreate()
    }

    public fun getSavedStateInstance():Map<String,Unit>{
        return mSavedStateInstance
    }
}