package com.ethan.and.ui.main

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.LocalBroadcastManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.common.utils.activity.ActivityUtil
import com.ethan.and.db.room.RoomRepository
import com.ethan.and.db.room.ShareDatabase
import com.ethan.and.db.room.entity.IpMessage
import com.ethan.and.getMainApplication
import com.ethan.and.service.MainService
import com.ethan.and.ui.dialog.ApDataDialog
import com.flybd.sharebox.AppExecutorManager
import com.flybd.sharebox.Constants
import com.flybd.sharebox.PreferenceInfo
import com.flybd.sharebox.R
import com.flybd.sharebox.model.DeviceModel
import com.flybd.sharebox.notification.ServerNotification
import com.flybd.sharebox.util.admob.AdmobCallback
import com.flybd.sharebox.util.admob.AdmobCallbackV2
import com.flybd.sharebox.util.admob.AdmobManager
import com.flybd.sharebox.util.firebase.FirebaseManager
import com.google.android.gms.ads.reward.RewardItem
import com.google.firebase.analytics.FirebaseAnalytics
import okhttp3.*
import org.ecjtu.channellibrary.wifiutil.NetworkUtil
import org.ecjtu.easyserver.IAidlInterface
import org.ecjtu.easyserver.server.ConversionFactory
import org.ecjtu.easyserver.server.DeviceInfo
import org.ecjtu.easyserver.server.impl.service.EasyServerService
import org.ecjtu.easyserver.server.util.cache.ServerInfoParcelableHelper
import org.json.JSONObject
import java.io.IOException


class MainPresenter : MainContract.Presenter {

    companion object {
        const val REQUEST_CODE = 10000
        private const val TAG = "MainPresenter"
        private const val MSG_SERVICE_STARTED = 0x10
        const val MSG_START_SERVER = 0x11
        const val MSG_STOP_SERVER = 0x14
        private const val MSG_LOADING_SERVER = 0x12
        const val MSG_CLOSE_APP = -1
        const val DEBUG = true
    }

    private val requestPermission = arrayOf(Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE)

    private lateinit var activity: Activity
    private var adManager: AdmobManager? = null
    private var handler: Handler? = null
    private var view: MainContract.View? = null

    private var mService: IAidlInterface? = null

    private var mMainService: MainService? = null

    private var mDeviceInfoList: MutableList<DeviceInfo> = mutableListOf<DeviceInfo>()

    private var refreshing = true

    private lateinit var shareDatabase: ShareDatabase

    private var mReceiver: WifiApReceiver? = null

    private val okHttpClient = OkHttpClient()

    override fun onCreate(activity: Activity, handler: Handler) {
        this.activity = activity
        this.handler = handler
        initAd()
        doSearch()
        checkCurrentNetwork(null)

        shareDatabase = RoomRepository(activity).shareDatabase

        val filter = IntentFilter(ApDataDialog.ACTION_UPDATE_DEVICE)
        LocalBroadcastManager.getInstance(activity).registerReceiver(mUpdateDeviceInfoReceiver, filter)
        reconnectHistory()

        FirebaseManager.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
    }

    override fun onDestroy(context: Context) {
        refreshing = false
        try {
            context.unbindService(mServiceConnection)
        } catch (ignore: java.lang.Exception) {
        }
        try {
            context.unbindService(mMainServiceConnection)
        } catch (ignore: java.lang.Exception) {
        }

        context.unregisterReceiver(mReceiver)

        getHandler()?.removeMessages(MSG_LOADING_SERVER)

        LocalBroadcastManager.getInstance(context).unregisterReceiver(mUpdateDeviceInfoReceiver)

        // destroy
        getMainService()?.stopSearch()

        // release main process
        Glide.get(activity).clearMemory()
    }

    override fun registerWifiApReceiver(context: Context) {
        mReceiver = WifiApReceiver()
        var filter = IntentFilter()
        filter.addAction(mReceiver?.ACTION_WIFI_AP_CHANGED)
        filter.addAction(mReceiver?.WIFI_STATE_CHANGED_ACTION)
        filter.addAction(mReceiver?.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(mReceiver?.CONNECTIVITY_ACTION)
        filter.addAction(org.ecjtu.easyserver.server.Constants.ACTION_CLOSE_SERVER)
        filter.addAction(org.ecjtu.easyserver.server.Constants.ACTION_UPDATE_SERVER)
        context.registerReceiver(mReceiver, filter)
    }

    override fun takeView(view: MainContract.View?) {
        this.view = view
        checkPermission(REQUEST_CODE)

        //resume service
        val intent = Intent(activity, MainService::class.java)
        activity.startService(intent)
        activity.bindService(intent, mMainServiceConnection, Context.BIND_AUTO_CREATE)

        FirebaseManager.logEvent(FirebaseManager.Event.APP_RESUME, null)
        //从设置关闭wifi回到app再检查一次，xiaomi会收不到回调
        checkCurrentNetwork(null)

        if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(Constants.PREF_IS_FIRST_OPEN, true)) {
            view?.onFirstOpen()
            PreferenceManager.getDefaultSharedPreferences(activity).edit().putBoolean(Constants.PREF_IS_FIRST_OPEN, false).apply()
        }
    }

    override fun checkPermission(requestCode: Int): Boolean {
        var ret = true
        for (perm in requestPermission) {
            if (ActivityCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, requestPermission, REQUEST_CODE)
                ret = false
            }
        }
        return ret
    }

    override fun dropView() {
        this.view = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        var isGranted = true
        for (grant in grantResults) {
            if (grant != PackageManager.PERMISSION_GRANTED) {
                isGranted = false
                break
            }
        }
        if (requestCode == REQUEST_CODE) {
            if (!isGranted) {
                view?.permissionRejected()
            } else {
                getHandler()?.post {
                    checkCurrentNetwork(null)
                }
            }
        }

        if (!isGranted) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.warning)
                    .setMessage(R.string.authorization_is_required)
                    .setPositiveButton(android.R.string.ok) { dialog, which ->
                        val intent = ActivityUtil.getAppDetailSettingIntent(activity)
                        try {
                            activity.startActivity(intent)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }.create().show()
        }
    }

    private fun initAd() {
        adManager = AdmobManager(activity)
        adManager?.loadRewardAd(activity.getString(R.string.admob_ad_03), object : AdmobCallbackV2 {
            override fun onCompleted() {
            }

            override fun onLoaded() {
                adManager?.getLatestRewardAd()?.show()
            }

            override fun onError() {
                if (view == null) {
                    return
                }
                adManager?.loadInterstitialAd(activity.getString(R.string.admob_ad_04), object : AdmobCallback {

                    override fun onLoaded() {
                        adManager?.getLatestInterstitialAd()?.show()
                    }

                    override fun onError() {
                        if (view == null) {
                            return
                        }
                        adManager?.loadInterstitialAd(activity.getString(R.string.admob_ad_04), this)
                    }

                    override fun onOpened() {
                    }

                    override fun onClosed() {
                        adManager = null
                    }

                })
            }

            override fun onOpened() {
            }

            override fun onClosed() {
            }

            override fun onReward(item: RewardItem?) {

            }

        })
    }

    override fun startSearch() {
        getMainService()?.startSearch()
    }

    override fun stopSearch() {
        getMainService()?.stopSearch()
    }

    override fun go2Setting() {
        val intent = Intent()
        val action = arrayOf(WifiManager.ACTION_PICK_WIFI_NETWORK, Settings.ACTION_WIFI_SETTINGS)
        for (str in action) {
            try {
                intent.action = Settings.ACTION_WIFI_SETTINGS
                activity.startActivity(intent)
                break
            } catch (ex: Exception) {
            }
        }
    }

    fun checkCurrentNetwork(info: WifiInfo?): Boolean {
        var hasAccess = false

        if (NetworkUtil.isWifi(activity) || info != null) {
            var wifiInfo: WifiInfo? = null
            wifiInfo = info ?: NetworkUtil.getConnectWifiInfo(activity)

            val name = if ("<unknown ssid>".equals(wifiInfo!!.ssid)) {
                NetworkUtil.getConnectWifiNameV2(activity)
            } else {
                getRealName(wifiInfo.ssid)
            }
            view?.updateNetworkInfo(name, true, false, 0)
            hasAccess = true
            activity.getMainApplication().getSavedInstance().put(Constants.AP_STATE, Constants.NetWorkState.WIFI)

            this.getHandler()?.obtainMessage(MSG_START_SERVER)?.sendToTarget()
        } else if (NetworkUtil.isHotSpot(activity)) {
            var config = NetworkUtil.getHotSpotConfiguration(activity)
            if (config != null) {
                view?.updateNetworkInfo(getRealName(config.SSID), false, true, 1)
            } else {
                view?.updateNetworkInfo(activity.getString(R.string.hotspot), false, true, 1)
            }

            hasAccess = true
            this.getHandler()?.obtainMessage(MSG_START_SERVER)?.sendToTarget()
            activity.getMainApplication().getSavedInstance().put(Constants.AP_STATE, Constants.NetWorkState.AP)

            // for self test
            var ips = NetworkUtil.getLocalApIps()
            var ip = "192.168.43.1"
            if (TextUtils.isEmpty(ip) && ips.isNotEmpty())
                ip = ips[0]
            else {
                ips = NetworkUtil.getLocalWLANIps()
                if (ips.isNotEmpty()) {
                    ip = ips[0]
                }
            }
            getInfo(ip, "8000")
        } else if (NetworkUtil.isMobile(activity)) {
            view?.updateNetworkInfo(activity.getString(R.string.cellular), false, false, 2)

            hasAccess = false
            activity.getMainApplication().getSavedInstance().put(Constants.AP_STATE, Constants.NetWorkState.MOBILE)
        } else {
            hasAccess = false
            view?.updateNetworkInfo(activity.getString(R.string.no_internet), false, false, 2)
            activity.getMainApplication().getSavedInstance().put(Constants.AP_STATE, Constants.NetWorkState.NONE)
        }
        return hasAccess
    }

    private fun getRealName(name: String): String {
        var str = name
        if (str[0] == '"')
            str = str.drop(1)

        if (str[str.length - 1] == '"')
            str = str.dropLast(1)
        return str
    }

    fun getHandler(): Handler? {
        return handler
    }

    override fun startServerService() {
        Log.i(TAG, "startServerService")
        if (mService == null) {
            var intent = Intent(activity, EasyServerService::class.java)
            activity.startService(intent)
            activity.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun stopServerService() {
        Log.i(TAG, "stopServerService")
        if (mService == null) return
        try {
            activity.unbindService(mServiceConnection)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            activity.stopService(Intent(activity, EasyServerService::class.java))
            mService = null
        }
    }


    fun registerServerInfo(hostIP: String, port: Int, name: String, mutableMap: MutableMap<String, List<String>>) {
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putInt(Constants.PREF_SERVER_PORT, port).apply()
        val deviceInfo = activity.getMainApplication().getSavedInstance().get(Constants.KEY_INFO_OBJECT) as DeviceInfo
        deviceInfo.apply {
            this.name = name
            this.ip = hostIP
            this.port = port
            this.icon = "/API/Icon"
            this.fileMap = mutableMap
            this.updateTime = System.currentTimeMillis()
        }
    }

    override fun doSearch() {

        val name = PreferenceManager.getDefaultSharedPreferences(activity).getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL)
        var port = PreferenceManager.getDefaultSharedPreferences(activity).getInt(Constants.PREF_SERVER_PORT, 0)

        if (port == 0) {
            port = this.getServerService()?.port ?: 0
            if (port == 0) return
        }

        this.getMainService()?.createHelper(name, port, "/API/Icon")
        this.getMainService()?.setMessageListener { ip, _, msg ->
            val state = activity.getMainApplication().getSavedInstance().get(Constants.AP_STATE)
            var self = ""
            if (state == Constants.NetWorkState.WIFI) {
                val ips = NetworkUtil.getLocalWLANIps()
                if (ips.isNotEmpty())
                    self = NetworkUtil.getLocalWLANIps()[0]
            } else if (state == Constants.NetWorkState.AP) {
                val ips = NetworkUtil.getLocalApIps()
                if (ips.isNotEmpty())
                    self = NetworkUtil.getLocalApIps()[0]
            }
            val msgStr = String(msg)
            val params = msgStr.split(",")
            if (params.size >= 3) {
                try {
                    val devModel = DeviceModel(params[0], params[1].toInt(), params[2])
                    if (!isSelf(self, ip)) {
                        this.getHandler()?.post {
                            applyDeviceInfo(ip, devModel)
                        }
                        val ipMsg = IpMessage(ip, devModel.port.toString())
                        val dao = shareDatabase.ipMessageDao()
                        val messageList = dao.allMessage
                        if (messageList.indexOf(ipMsg) < 0) {
                            shareDatabase.ipMessageDao().insert(ipMsg)
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

        if (this.refreshing) {
            this.getMainService()?.startSearch()
        }
    }

    override fun getMainService(): MainService? {
        return mMainService
    }

    override fun getServerService(): IAidlInterface? {
        return mService
    }

    private fun isSelf(self: String, ip: String): Boolean {
        if (DEBUG) return false

        if (self == ip) {
            return true
        }
        return false
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

    private val mMainServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mMainService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mMainService = (service as MainService.MainServiceBinder).service

            //start server
            startServerService()
        }
    }

    private fun applyDeviceInfo(ip: String, deviceModel: DeviceModel) {
        var flag: Boolean
        flag = false
        var old: DeviceInfo? = null
        for (info in mDeviceInfoList) {
            if (info.ip.equals(ip)) {
                flag = true
                old = info
            }
        }
        var needUpdate = false
        if (!flag) {
            val deviceInfo = DeviceInfo(deviceModel.name, ip, deviceModel.port, deviceModel.icon)
            mDeviceInfoList.add(deviceInfo)
            needUpdate = true
        } else {

            if (deviceModel.port != 0) {
                var localNeedUpdate = false
                if (old?.port != deviceModel.port || old.icon != deviceModel.icon) {
                    localNeedUpdate = true
                    Log.e(TAG, "need update recycler view")
                }

                old?.name = deviceModel.name
                old?.port = deviceModel.port
                old?.icon = deviceModel.icon

                needUpdate = localNeedUpdate
            }
        }
        view?.updateDeviceInfo(needUpdate, mDeviceInfoList)
    }

    override fun closeApp() {
        try {
            activity.unbindService(mServiceConnection)
            activity.unbindService(mMainServiceConnection)
        } catch (e: java.lang.Exception) {
        } finally {
            activity.stopService(Intent(activity, EasyServerService::class.java))
            activity.stopService(Intent(activity, MainService::class.java))
            activity.getMainApplication().closeApp()
        }
    }

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            MSG_SERVICE_STARTED -> {
                if (checkCurrentNetwork(null) ?: false) {
                    getHandler()?.obtainMessage(MSG_START_SERVER)?.sendToTarget()
                }
            }
            MSG_START_SERVER -> {
                if (mService == null) {
                    getHandler()?.post {
                        startServerService()
                    }
                    return
                }
                var flag = false
                if (mService?.isServerAlive() == false && getHandler()?.hasMessages(MSG_LOADING_SERVER) == false) {
                    flag = true
                    Log.e(TAG, "isServerAlive false,start server")
                    var intent = EasyServerService.getApIntent(activity)
                    activity.startService(intent)
                    getHandler()?.sendEmptyMessageDelayed(MSG_LOADING_SERVER, Int.MAX_VALUE.toLong())
                } else if (getHandler()?.hasMessages(MSG_LOADING_SERVER) == false) {
//                    PreferenceManager.getDefaultSharedPreferences(this).edit().remove(Constants.PREF_SERVER_PORT).apply()
                }

                if (!flag) {
                    var name = PreferenceManager.getDefaultSharedPreferences(activity).getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL)
                    if (mService != null && mService!!.ip != null && mService!!.port != 0) {
                        val deviceInfo = activity.getMainApplication().getSavedInstance().get(Constants.KEY_INFO_OBJECT) as DeviceInfo?
                        deviceInfo?.let {
                            registerServerInfo(mService!!.ip, mService!!.port, name,
                                    deviceInfo.fileMap)
                        }
                    }
                    getHandler()?.post {
                        doSearch()
                    }
                }
            }
            MSG_STOP_SERVER -> {
                stopServerService()
                getHandler()?.post {
                    checkCurrentNetwork(null)
                }
            }
            MSG_CLOSE_APP -> {
                closeApp()
            }
        }
    }

    private fun reconnectHistory() {
        AppExecutorManager.getInstance().diskIO().execute {
            val listMsg = shareDatabase.ipMessageDao().allMessage
            for (msg in listMsg) {
                getInfo(msg.ip, msg.port)
            }
        }
    }

    private fun getInfo(ip: String, port: String) {
        val request = Request.Builder()
                .url("http://${ip}:${port}/api/Info")
                .method("GET", null)
                .build()
        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val res = response.body()?.string()
                if (!TextUtils.isEmpty(res)) {
                    try {
                        val jObj = JSONObject(res)
                        val ip = jObj.optString("ip")
                        val port = jObj.optString("port")
                        val name = jObj.optString("name")
                        val icon = jObj.optString("icon")
                        val deviceModel = DeviceModel(name, port.toInt(), icon)
                        getHandler()?.post {
                            applyDeviceInfo(ip, deviceModel)
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
            }
        })
    }

    override fun refresh(isRefresh: Boolean) {
        this.refreshing = isRefresh
    }

    override fun isRefreshing(): Boolean {
        return refreshing
    }

    private val mUpdateDeviceInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.extras != null) {
                val ip = intent.extras.getString(ApDataDialog.EXTRA_IP)
                if (!TextUtils.isEmpty(ip)) {
                    val json = intent.extras.getString(ApDataDialog.EXTRA_JSON)
                    try {
                        val deviceInfo = ConversionFactory.json2DeviceInfo(JSONObject(json))
                        ServerNotification(context!!).buildServerNotification(activity.getString(R.string.searched_new_device), deviceInfo.name,
                                activity.getString(R.string.app_name) + ":" + activity.getString(R.string.find_new_device)).send()
                        if (mDeviceInfoList.indexOf(deviceInfo) < 0) {
                            mDeviceInfoList.add(deviceInfo)
                            view?.updateDeviceInfo(true, mDeviceInfoList)
                        }
                    } catch (ex: Exception) {
                    }
                }
            }
        }
    }

    private inner class WifiApReceiver : BroadcastReceiver() {
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
                        if (checkCurrentNetwork(null) ?: false) {
                            if (mService != null) {
                                getHandler()?.obtainMessage(MSG_START_SERVER)?.sendToTarget()
                            } else {
                                startServerService()
                            }
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
                        getHandler()?.obtainMessage(MSG_STOP_SERVER)?.sendToTarget()
                        checkCurrentNetwork(null)
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
            } else if (action == WIFI_STATE_CHANGED_ACTION) { // wifi 连接上时，有可能不会回调
                val localState = intent.getIntExtra(EXTRA_WIFI_STATE, -1)
                when (localState) {
                    WIFI_STATE_ENABLED -> {
                        if (checkCurrentNetwork(null) ?: false) {
                            if (mService != null) {
                                getHandler()?.obtainMessage(MSG_START_SERVER)?.sendToTarget()
                            } else {
                                startServerService()
                            }
                        }
                    }
                    WIFI_STATE_DISABLED -> {
                        getHandler()?.obtainMessage(MSG_STOP_SERVER)?.sendToTarget()
                        checkCurrentNetwork(null)
                    }
                }
            } else if (action.equals(NETWORK_STATE_CHANGED_ACTION)) {
                var wifiInfo = intent.getParcelableExtra<WifiInfo>(EXTRA_WIFI_INFO)
                Log.i("WifiApReceiver", "WifiInfo " + wifiInfo?.toString() ?: "null")
                if (wifiInfo != null) {
                    if (wifiInfo.bssid != null && !wifiInfo.bssid.equals("<none>")) // is a bug in ui
                        checkCurrentNetwork(wifiInfo)
                }
            } else if (action.equals(CONNECTIVITY_ACTION)) {
                var info = intent.getParcelableExtra<NetworkInfo>(EXTRA_NETWORK_INFO)
                Log.i("WifiApReceiver", "NetworkInfo " + info?.toString() ?: "null")
                if (info != null && info.type == TYPE_MOBILE && (info.state == NetworkInfo.State.CONNECTED ||
                                info.state == NetworkInfo.State.DISCONNECTED)) {
                    checkCurrentNetwork(null)
                } else if (info != null && (info.state == NetworkInfo.State.CONNECTED)) {
                    if (checkCurrentNetwork(null) == true) {
                        startServerService()
                    }
                }
            } else if (action == org.ecjtu.easyserver.server.Constants.ACTION_CLOSE_SERVER) {
                getHandler()?.sendEmptyMessage(MSG_CLOSE_APP)
            } else if (action == org.ecjtu.easyserver.server.Constants.ACTION_UPDATE_SERVER) {
                val pref = PreferenceManager.getDefaultSharedPreferences(context)
                val name = pref.getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL)
                val hostIP = pref.getString(org.ecjtu.easyserver.server.Constants.PREF_KEY_HOST_IP, "")
                val port = pref.getInt(org.ecjtu.easyserver.server.Constants.PREF_KEY_HOST_PORT, 0)
                if (!TextUtils.isEmpty(hostIP)) {
                    registerServerInfo(hostIP, port, name, mutableMapOf())
                    val deviceInfo = (context as Activity).getMainApplication().getSavedInstance().get(Constants.KEY_INFO_OBJECT) as DeviceInfo
                    val helper = ServerInfoParcelableHelper(context.filesDir.absolutePath)
                    helper.put(Constants.KEY_INFO_OBJECT, deviceInfo)
                    val intent = EasyServerService.getSetupServerIntent(context, Constants.KEY_INFO_OBJECT)
                    context.startService(intent)

                    getHandler()?.removeMessages(MSG_LOADING_SERVER)
                    getHandler()?.sendEmptyMessage(MSG_START_SERVER)
                }
                (context as Activity).runOnUiThread { doSearch() }
            }
        }
    }
}