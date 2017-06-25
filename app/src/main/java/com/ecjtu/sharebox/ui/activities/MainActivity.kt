package com.ecjtu.sharebox.ui.activities

import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.RotateDrawable
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.presenter.MainActivityDelegate
import android.app.Activity
import android.view.KeyCharacterMap
import android.view.ViewConfiguration
import android.graphics.Point
import android.os.Build



//http://www.tmtpost.com/195557.html 17.6.7
class MainActivity : BaseActionActivity() {

    private var mDelegate : MainActivityDelegate? =null

    private var mAnimator : ObjectAnimator? =null

    private var mReceiver : WifiApReceiver? =null

    var refreshing =true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var toolbar=findViewById(R.id.toolbar) as Toolbar

        setSupportActionBar(toolbar)

        mDelegate= MainActivityDelegate(this)

        var drawer=findViewById(R.id.drawer_view)

        if(isNavigationBarShow(this)){
            drawer.setPadding(drawer.paddingLeft,drawer.paddingTop,drawer.paddingRight,getNavigationBarHeight(this))
        }
    }


    override fun onResume() {
        super.onResume()
        mReceiver=WifiApReceiver()
        var filter= IntentFilter()
        filter.addAction(mReceiver?.ACTION_WIFI_AP_CHANGED)
        filter.addAction(mReceiver?.WIFI_STATE_CHANGED_ACTION)
        filter.addAction(mReceiver?.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(mReceiver?.CONNECTIVITY_ACTION)
        registerReceiver(mReceiver,filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(mReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity,menu)
        var item=menu!!.findItem(R.id.refresh)
        var rotateDrawable=item.icon as RotateDrawable

        mAnimator=ObjectAnimator.ofInt(rotateDrawable, "level", 0, 10000) as ObjectAnimator?
        mAnimator?.setRepeatMode(ObjectAnimator.RESTART)
        mAnimator?.repeatCount=ObjectAnimator.INFINITE
        mAnimator?.setDuration(1000)
        mAnimator?.start()

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId){
            R.id.refresh->{
                if(mAnimator!!.isRunning){
                    refreshing =false
                    mAnimator?.cancel()
                }else{
                    refreshing =true
                    mAnimator?.start()
                }
            }
        }
        var result=mDelegate?.onOptionsItemSelected(item) ?: false

        if(result){
            return result
        }
        return super.onOptionsItemSelected(item)
    }

    protected inner class WifiApReceiver : BroadcastReceiver() {
        val WIFI_AP_STATE_DISABLING = 10

        val WIFI_AP_STATE_DISABLED = 11

        val WIFI_AP_STATE_ENABLING = 12

        val WIFI_AP_STATE_ENABLED = 13

        val WIFI_AP_STATE_FAILED = 14

        val WIFI_STATE_ENABLED= 3

        val WIFI_STATE_DISABLED=1

        val EXTRA_WIFI_AP_STATE = "wifi_state"

        val EXTRA_WIFI_STATE= "wifi_state"

        val ACTION_WIFI_AP_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED"

        val WIFI_STATE_CHANGED_ACTION ="android.net.wifi.WIFI_STATE_CHANGED"

        val NETWORK_STATE_CHANGED_ACTION="android.net.wifi.STATE_CHANGE"

        val CONNECTIVITY_ACTION= "android.net.conn.CONNECTIVITY_CHANGE"

        val EXTRA_WIFI_INFO="wifiInfo"

        val EXTRA_NETWORK_INFO = "networkInfo"

        val TYPE_MOBILE=0

        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(EXTRA_WIFI_AP_STATE, -1)
            val action = intent.action

            if (action == ACTION_WIFI_AP_CHANGED) {
                when (state) {
                    WIFI_AP_STATE_ENABLED -> {
                        mDelegate?.checkCurrentAp(null)
                        var s = ""
                        when (state) {
                            WIFI_AP_STATE_DISABLED -> s = "WIFI_AP_STATE_DISABLED"
                            WIFI_AP_STATE_DISABLING -> s = "WIFI_AP_STATE_DISABLING"
                            WIFI_AP_STATE_ENABLED -> s = "WIFI_AP_STATE_ENABLED"
                            WIFI_AP_STATE_ENABLING -> s = "WIFI_AP_STATE_ENABLED"
                            WIFI_AP_STATE_FAILED -> s = "WIFI_AP_STATE_FAILED"
                        }
                        Log.i("WifiApReceiver","ap " +s)
                    }
                    WIFI_AP_STATE_DISABLED->{
                        mDelegate?.checkCurrentAp(null)
                    }
                    else -> {
                        var s = ""
                        when (state) {
                            WIFI_AP_STATE_DISABLED -> s = "WIFI_AP_STATE_DISABLED"
                            WIFI_AP_STATE_DISABLING -> s = "WIFI_AP_STATE_DISABLING"
                            WIFI_AP_STATE_ENABLED -> s = "WIFI_AP_STATE_ENABLED"
                            WIFI_AP_STATE_ENABLING -> s = "WIFI_AP_STATE_ENABLED"
                            WIFI_AP_STATE_FAILED -> s = "WIFI_AP_STATE_FAILED"
                        }
                        Log.i("WifiApReceiver","ap " + s)
                    }
                }
            }

            else if(action==WIFI_STATE_CHANGED_ACTION){
                var state=intent.getIntExtra(EXTRA_WIFI_STATE, -1)
                when(state){
                    WIFI_STATE_ENABLED->{
                        mDelegate?.checkCurrentAp(null)
                    }
                    WIFI_STATE_DISABLED->{
                        mDelegate?.checkCurrentAp(null)
                    }
                }
            }

            else if(action.equals(NETWORK_STATE_CHANGED_ACTION)){
                var wifiInfo=intent.getParcelableExtra<WifiInfo>(EXTRA_WIFI_INFO)
                Log.i("WifiApReceiver","WifiInfo "+  wifiInfo?.toString() ?: "null")
                if(wifiInfo!=null){
                    if(wifiInfo.bssid!=null && !wifiInfo.bssid.equals("<none>")) // is a bug in ui
                        mDelegate?.checkCurrentAp(wifiInfo)
                }
            }

            else if(action.equals(CONNECTIVITY_ACTION)){
                var info=intent.getParcelableExtra<NetworkInfo>(EXTRA_NETWORK_INFO)
                Log.i("WifiApReceiver","NetworkInfo "+ info?.toString() ?: "null")
                if(info!=null&&info.type==TYPE_MOBILE&&(info.state==NetworkInfo.State.CONNECTED||
                        info.state==NetworkInfo.State.DISCONNECTED)){
                    mDelegate?.checkCurrentAp(null)
                }
            }
        }
    }

    fun isNavigationBarShow(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val display = windowManager.defaultDisplay
            val size = Point()
            val realSize = Point()
            display.getSize(size)
            display.getRealSize(realSize)
            return realSize.y !== size.y
        } else {
            val menu = ViewConfiguration.get(activity).hasPermanentMenuKey()
            val back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
            return !(menu || back)
        }
    }

    fun getNavigationBarHeight(activity: Activity): Int {
        if (!isNavigationBarShow(activity)) {
            return 0
        }
        val resources = activity.resources
        val resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android")
        //获取NavigationBar的高度
        val height = resources.getDimensionPixelSize(resourceId)
        return height
    }


    fun getScreenHeight(activity: Activity): Int {
        return activity.windowManager.defaultDisplay.height + getNavigationBarHeight(activity)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mDelegate?.onRequestPermissionsResult(requestCode,permissions,grantResults)
    }
}
