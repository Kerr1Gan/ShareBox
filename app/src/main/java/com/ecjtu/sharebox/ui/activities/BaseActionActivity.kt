package com.ecjtu.sharebox.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity

/**
 * Created by KeriGan on 2017/6/25.
 */
abstract class BaseActionActivity:AppCompatActivity{

    private var mLocalBroadcastManger:LocalBroadcastManager? =null

    private var mIntentFilter: IntentFilter? =null

    private var mBroadcastReceiver: SimpleReceiver? =null

    constructor():super(){
        mLocalBroadcastManger= LocalBroadcastManager.getInstance(this)
        mIntentFilter= IntentFilter()
        mBroadcastReceiver=SimpleReceiver()
        registerActions(mIntentFilter)
    }


    override fun onResume() {
        super.onResume()
        mLocalBroadcastManger?.registerReceiver(mBroadcastReceiver,mIntentFilter)
    }

    override fun onStop() {
        super.onStop()
        mLocalBroadcastManger?.unregisterReceiver(mBroadcastReceiver)
    }

    inner class SimpleReceiver:BroadcastReceiver(){

        override fun onReceive(context: Context?, intent: Intent?) {
            this@BaseActionActivity.handleActions(intent?.action,intent)
        }
    }

    open fun registerActions(intentFilter:IntentFilter?){
        //to register action by override
    }

    open fun handleActions(action:String?,intent: Intent?){
        //override
    }

    open fun unregisterActions(){
        mLocalBroadcastManger?.unregisterReceiver(mBroadcastReceiver)
    }

    open fun getIntentFilter():IntentFilter?{
        return mIntentFilter
    }

    open fun registerActions(array: Array<String>,intentFilter:IntentFilter){
        for(action in array){
            intentFilter.addAction(action)
        }
        mLocalBroadcastManger?.registerReceiver(mBroadcastReceiver,intentFilter)
        mIntentFilter=intentFilter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
