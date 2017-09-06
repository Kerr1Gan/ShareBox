package com.ecjtu.sharebox.ui.activity

import android.animation.ObjectAnimator
import android.content.*
import android.graphics.drawable.RotateDrawable
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.preference.PreferenceManager
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.ecjtu.sharebox.Constants
import com.ecjtu.sharebox.PreferenceInfo
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.getMainApplication
import com.ecjtu.sharebox.presenter.MainActivityDelegate
import com.ecjtu.sharebox.service.MainService
import com.ecjtu.sharebox.ui.fragment.SplashFragment
import com.ecjtu.sharebox.util.admob.AdmobCallback
import com.ecjtu.sharebox.util.admob.AdmobManager
import org.ecjtu.easyserver.IAidlInterface
import org.ecjtu.easyserver.server.DeviceInfo
import org.ecjtu.easyserver.server.impl.service.EasyServerService
import org.ecjtu.easyserver.server.util.cache.ServerInfoParcelableHelper

class MainActivity : ImmersiveFragmentActivity() {

    companion object {
        const private val TAG = "MainActivity"
        private val MSG_SERVICE_STARTED = 0x10
        val MSG_START_SERVER = 0x11
        private val MSG_LOADING_SERVER = 0x12
        @JvmStatic
        val MSG_CLOSE_APP = -1
        const val DEBUG = true
    }

    private var mDelegate: MainActivityDelegate? = null

    private var mAnimator: ObjectAnimator? = null

    private var mReceiver: WifiApReceiver? = null

    var refreshing = true

    private var mService: IAidlInterface? = null

    private var mAdManager: AdmobManager? = null

    private var mMainService: MainService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.ImageStatusBarStyle)
        super.onCreate(savedInstanceState)
//        loadSplash()
        setContentView(R.layout.activity_main)
        var toolbar = findViewById(R.id.toolbar) as Toolbar

        setSupportActionBar(toolbar)

        mDelegate = MainActivityDelegate(this)

        var drawer = findViewById(R.id.drawer_view)

        if (isNavigationBarShow(this)) {
            drawer.setPadding(drawer.paddingLeft, drawer.paddingTop, drawer.paddingRight,
                    drawer.paddingBottom + getNavigationBarHeight(this))
            val recyclerView = mDelegate?.getRecyclerView()
            recyclerView?.setPadding(recyclerView.paddingLeft, recyclerView.paddingTop, recyclerView.paddingRight,
                    recyclerView.paddingBottom + getNavigationBarHeight(this))
        }

        //init service
        val intent = Intent(this, MainService::class.java)
        startService(intent)
        bindService(intent, mMainServiceConnection, Context.BIND_AUTO_CREATE)

        //ad
//        initAd()

        mReceiver = WifiApReceiver()
        var filter = IntentFilter()
        filter.addAction(mReceiver?.ACTION_WIFI_AP_CHANGED)
        filter.addAction(mReceiver?.WIFI_STATE_CHANGED_ACTION)
        filter.addAction(mReceiver?.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(mReceiver?.CONNECTIVITY_ACTION)
        filter.addAction(org.ecjtu.easyserver.server.Constants.ACTION_CLOSE_SERVER)
        filter.addAction(org.ecjtu.easyserver.server.Constants.ACTION_UPDATE_SERVER)
        registerReceiver(mReceiver, filter)
    }


    override fun onResume() {
        super.onResume()
        getMainApplication().closeActivityByIndex(1)
        var name = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL)
        (findViewById(R.id.text_name) as TextView).setText(name)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        refreshing = false
        mDelegate?.onDestroy()
        getHandler()?.removeMessages(MSG_LOADING_SERVER)
        unregisterReceiver(mReceiver)
        try {
            unbindService(mServiceConnection)
        } catch (ignore: java.lang.Exception) {
        }
        try {
            unbindService(mMainServiceConnection)
        } catch (ignore: java.lang.Exception) {
        }
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        var item = menu!!.findItem(R.id.refresh)
        var rotateDrawable = item.icon as RotateDrawable

        mAnimator = ObjectAnimator.ofInt(rotateDrawable, "level", 0, 10000) as ObjectAnimator?
        mAnimator?.setRepeatMode(ObjectAnimator.RESTART)
        mAnimator?.repeatCount = ObjectAnimator.INFINITE
        mAnimator?.setDuration(1000)
        mAnimator?.start()

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.refresh -> {
                if (mAnimator?.isRunning == true) {
                    refreshing = false
                    mAnimator?.cancel()
                } else {
                    refreshing = true
                    mAnimator?.start()
                }
            }
        }
        var result = mDelegate?.onOptionsItemSelected(item) ?: false

        if (result) {
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

        val WIFI_STATE_ENABLED = 3

        val WIFI_STATE_DISABLED = 1

        val EXTRA_WIFI_AP_STATE = "wifi_state"

        val EXTRA_WIFI_STATE = "wifi_state"

        val ACTION_WIFI_AP_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED"

        val WIFI_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_STATE_CHANGED"

        val NETWORK_STATE_CHANGED_ACTION = "android.net.wifi.STATE_CHANGE"

        val CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"

        val EXTRA_WIFI_INFO = "wifiInfo"

        val EXTRA_NETWORK_INFO = "networkInfo"

        val TYPE_MOBILE = 0

        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(EXTRA_WIFI_AP_STATE, -1)
            val action = intent.action

            if (action == ACTION_WIFI_AP_CHANGED) {
                when (state) {
                    WIFI_AP_STATE_ENABLED -> {
                        if (mDelegate?.checkCurrentNetwork(null) ?: false) {
                            getHandler()?.obtainMessage(MSG_START_SERVER)?.sendToTarget()
                        }
                        var s = ""
                        when (state) {
                            WIFI_AP_STATE_DISABLED -> s = "WIFI_AP_STATE_DISABLED"
                            WIFI_AP_STATE_DISABLING -> s = "WIFI_AP_STATE_DISABLING"
                            WIFI_AP_STATE_ENABLED -> s = "WIFI_AP_STATE_ENABLED"
                            WIFI_AP_STATE_ENABLING -> s = "WIFI_AP_STATE_ENABLED"
                            WIFI_AP_STATE_FAILED -> s = "WIFI_AP_STATE_FAILED"
                        }
                        Log.i("WifiApReceiver", "ap " + s)
                    }
                    WIFI_AP_STATE_DISABLED -> {
                        mDelegate?.checkCurrentNetwork(null)
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
                        Log.i("WifiApReceiver", "ap " + s)
                    }
                }
            } else if (action == WIFI_STATE_CHANGED_ACTION) {
                var state = intent.getIntExtra(EXTRA_WIFI_STATE, -1)
                when (state) {
                    WIFI_STATE_ENABLED -> {
                        if (mDelegate?.checkCurrentNetwork(null) ?: false) {
                            getHandler()?.obtainMessage(MSG_START_SERVER)?.sendToTarget()
                        }
                    }
                    WIFI_STATE_DISABLED -> {
                        mDelegate?.checkCurrentNetwork(null)
                    }
                }
            } else if (action.equals(NETWORK_STATE_CHANGED_ACTION)) {
                var wifiInfo = intent.getParcelableExtra<WifiInfo>(EXTRA_WIFI_INFO)
                Log.i("WifiApReceiver", "WifiInfo " + wifiInfo?.toString() ?: "null")
                if (wifiInfo != null) {
                    if (wifiInfo.bssid != null && !wifiInfo.bssid.equals("<none>")) // is a bug in ui
                        mDelegate?.checkCurrentNetwork(wifiInfo)
                }
            } else if (action.equals(CONNECTIVITY_ACTION)) {
                var info = intent.getParcelableExtra<NetworkInfo>(EXTRA_NETWORK_INFO)
                Log.i("WifiApReceiver", "NetworkInfo " + info?.toString() ?: "null")
                if (info != null && info.type == TYPE_MOBILE && (info.state == NetworkInfo.State.CONNECTED ||
                        info.state == NetworkInfo.State.DISCONNECTED)) {
                    mDelegate?.checkCurrentNetwork(null)
                }
            } else if (action == org.ecjtu.easyserver.server.Constants.ACTION_CLOSE_SERVER) {
                getHandler()?.sendEmptyMessage(MSG_CLOSE_APP)
            } else if (action == org.ecjtu.easyserver.server.Constants.ACTION_UPDATE_SERVER) {
                val pref = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                val name = pref.getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL)
                val hostIP = pref.getString(org.ecjtu.easyserver.server.Constants.PREF_KEY_HOST_IP, "")
                val port = pref.getInt(org.ecjtu.easyserver.server.Constants.PREF_KEY_HOST_PORT, 0)
                if (!TextUtils.isEmpty(hostIP)) {
                    registerServerInfo(hostIP, port, name, mutableMapOf())
                    val deviceInfo = getMainApplication().getSavedInstance().get(Constants.KEY_INFO_OBJECT) as DeviceInfo
                    val helper = ServerInfoParcelableHelper(this@MainActivity.filesDir.absolutePath)
                    helper.put(Constants.KEY_INFO_OBJECT, deviceInfo)
                    val intent = EasyServerService.getSetupServerIntent(this@MainActivity, Constants.KEY_INFO_OBJECT)
                    this@MainActivity.startService(intent)

                    getHandler()?.removeMessages(MSG_LOADING_SERVER)
                    getHandler()?.sendEmptyMessage(MSG_START_SERVER)
                }
                runOnUiThread { mDelegate?.doSearch() }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mDelegate?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e(TAG, "onServiceDisconnected " + name.toString()) // 子进程服务挂掉后会被回调
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.e(TAG, "onServiceConnected " + name.toString())
            mService = IAidlInterface.Stub.asInterface(service)
            getHandler()?.obtainMessage(MSG_SERVICE_STARTED)?.sendToTarget()
        }
    }

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        when (msg.what) {
            MSG_SERVICE_STARTED -> {
                if (mDelegate?.checkCurrentNetwork(null) ?: false) {
                    getHandler()?.obtainMessage(MSG_START_SERVER)?.sendToTarget()
                }
            }
            MSG_START_SERVER -> {
                if (mService == null) return
                var flag = false
                if (mService?.isServerAlive() == false && getHandler()?.hasMessages(MSG_LOADING_SERVER) == false) {
                    flag = true
                    Log.e(TAG, "isServerAlive false,start server")
                    var intent = EasyServerService.getApIntent(this)
                    startService(intent)
                    getHandler()?.sendEmptyMessageDelayed(MSG_LOADING_SERVER, Int.MAX_VALUE.toLong())
                } else {
                    getMainApplication().getSavedInstance().remove(Constants.KEY_SERVER_PORT)
                }

                if (!flag && mDelegate != null) {
                    var name = PreferenceManager.getDefaultSharedPreferences(this).
                            getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL)
                    if (mService != null && mService!!.ip != null && mService!!.port != 0) {
                        val deviceInfo = getMainApplication().getSavedInstance().get(Constants.KEY_INFO_OBJECT) as DeviceInfo
                        registerServerInfo(mService!!.ip, mService!!.port, name,
                                deviceInfo.fileMap)
                    }
                    runOnUiThread { mDelegate?.doSearch() }
                }
            }
            MSG_CLOSE_APP -> {
                try {
                    unbindService(mServiceConnection)
                    unbindService(mMainServiceConnection)
                } catch (e: java.lang.Exception) {
                } finally {
                    stopService(Intent(this, EasyServerService::class.java))
                    stopService(Intent(this, MainService::class.java))
                    getMainApplication().closeApp()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mDelegate?.onActivityResult(requestCode, resultCode, data)
    }

    fun registerServerInfo(hostIP: String, port: Int, name: String, mutableMap: MutableMap<String, List<String>>) {
        getMainApplication().getSavedInstance().put(Constants.KEY_SERVER_PORT, port.toString())
        val deviceInfo = getMainApplication().getSavedInstance().get(Constants.KEY_INFO_OBJECT) as DeviceInfo
        deviceInfo.apply {
            this.name = name
            this.ip = hostIP
            this.port = port
            this.icon = "/API/Icon"
            this.fileMap = mutableMap
            this.updateTime = System.currentTimeMillis()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (!DEBUG) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                this.moveTaskToBack(true)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun initAd() {
        mAdManager = AdmobManager(this)
        mAdManager?.loadInterstitialAd(getString(R.string.admob_ad_02), object : AdmobCallback {
            override fun onLoaded() {
                mAdManager?.getLatestInterstitialAd()?.show()
            }

            override fun onError() {
                mAdManager?.loadInterstitialAd(getString(R.string.admob_ad_02), this)
            }

            override fun onOpened() {
            }

            override fun onClosed() {
                mAdManager = null
            }

        })
    }

    private fun loadSplash() {
        val intent = ImmersiveFragmentActivity.newInstance(this, SplashFragment::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivity(intent)
    }

    private val mMainServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mMainService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mMainService = (service as MainService.MainServiceBinder).service

            //start server
            var intent = Intent(this@MainActivity, EasyServerService::class.java)
            startService(intent)
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun getMainService(): MainService? {
        return mMainService
    }

}
