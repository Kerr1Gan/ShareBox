package com.ecjtu.sharebox.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Message
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import com.ecjtu.sharebox.async.MemoryUnLeakHandler
import java.lang.ref.WeakReference

/**
 * Created by KeriGan on 2017/6/25.
 */
abstract class BaseActionActivity:AppCompatActivity,MemoryUnLeakHandler.IHandleMessage{

    private var mLocalBroadcastManger:LocalBroadcastManager? =null

    private var mIntentFilter: IntentFilter? =null

    private var mBroadcastReceiver: SimpleReceiver? =null

    private var mSimpleHandler:SimpleHandler? =null
    constructor():super(){
        mLocalBroadcastManger= LocalBroadcastManager.getInstance(this)
        mIntentFilter= IntentFilter()
        mBroadcastReceiver=SimpleReceiver()
        registerActions(mIntentFilter)

        mSimpleHandler= SimpleHandler(this)
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

    fun getHandler():Handler?{
        return mSimpleHandler
    }

    override fun handleMessage(msg: Message) {
        //do nothing
    }

    class SimpleHandler(host:BaseActionActivity):
            MemoryUnLeakHandler<BaseActionActivity>(host){
    }
}
