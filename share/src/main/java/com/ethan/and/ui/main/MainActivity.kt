package com.ethan.and.ui.main

import android.Manifest
import android.animation.ObjectAnimator
import android.content.*
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.RotateDrawable
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.*
import com.bumptech.glide.Glide
import com.common.componentes.activity.ActionBarFragmentActivity
import com.common.componentes.activity.ImmersiveFragmentActivity
import com.common.netcore.RequestManager
import com.common.netcore.network.IRequestCallback
import com.common.utils.activity.ActivityUtil
import com.common.utils.photo.CapturePhotoHelper
import com.common.utils.photo.PickPhotoHelper
import com.ethan.and.db.room.RoomRepository
import com.ethan.and.db.room.ShareDatabase
import com.ethan.and.db.room.entity.IpMessage
import com.ethan.and.getMainApplication
import com.ethan.and.service.MainService
import com.ethan.and.ui.activity.SettingsActivity
import com.ethan.and.ui.adapter.DeviceRecyclerViewAdapter
import com.ethan.and.ui.dialog.*
import com.ethan.and.ui.fragment.*
import com.ethan.and.ui.state.StateMachine
import com.flybd.sharebox.Constants
import com.flybd.sharebox.PreferenceInfo
import com.flybd.sharebox.R
import com.flybd.sharebox.model.DeviceModel
import com.flybd.sharebox.notification.ServerNotification
import com.flybd.sharebox.util.admob.AdmobCallback
import com.flybd.sharebox.util.admob.AdmobManager
import okhttp3.*
import org.ecjtu.channellibrary.devicesearch.DeviceSearcher
import org.ecjtu.channellibrary.wifiutil.NetworkUtil
import org.ecjtu.easyserver.IAidlInterface
import org.ecjtu.easyserver.server.ConversionFactory
import org.ecjtu.easyserver.server.DeviceInfo
import org.ecjtu.easyserver.server.impl.service.EasyServerService
import org.ecjtu.easyserver.server.util.cache.ServerInfoParcelableHelper
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import kotlin.concurrent.thread

class MainActivity : ImmersiveFragmentActivity(), MainContract.View {

    companion object {
        private const val TAG = "MainActivity"
        private const val MSG_SERVICE_STARTED = 0x10
        const val MSG_START_SERVER = 0x11
        const val MSG_STOP_SERVER = 0x14
        private const val MSG_LOADING_SERVER = 0x12
        const val MSG_CLOSE_APP = -1
        const val DEBUG = true
        private const val CLOSE_TIME = 3 * 1000
        private const val REQUEST_CODE = 0x10

        private const val TAG_FRAGMENT = "FilePickDialogFragment"
    }

    private var mAnimator: ObjectAnimator? = null

    private var mReceiver: WifiApReceiver? = null

    var refreshing = true

    private var mService: IAidlInterface? = null

    private var mMainService: MainService? = null

    private lateinit var presenter: MainContract.Presenter

    private var lastBackPressTime = -1L

    private lateinit var mToolbar: Toolbar
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private lateinit var mFloatingActionButton: FloatingActionButton
    private var mViewSwitcher: ViewSwitcher? = null
    private lateinit var mWifiButton: Button
    private lateinit var mHotspotButton: Button
    private lateinit var mApName: TextView
    private lateinit var mWifiImage: ImageView
    private var mTextName: TextView? = null

    private val mRequestPermission = arrayOf(Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_SETTINGS)

    private var mRecyclerView: RecyclerView? = null

    private var mDeviceInfoList: MutableList<DeviceInfo> = mutableListOf<DeviceInfo>()

    private var mPhotoHelper: CapturePhotoHelper? = null

    private var mImageHelper: PickPhotoHelper? = null

    private val DELAY_TIME = 8000L

    private var mWifiImageStateMachine: StateMachine? = null

    private lateinit var shareDatabase: ShareDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        loadSplash()
        setContentView(R.layout.activity_main)
        var toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        initialize()

        mReceiver = WifiApReceiver()
        var filter = IntentFilter()
        filter.addAction(mReceiver?.ACTION_WIFI_AP_CHANGED)
        filter.addAction(mReceiver?.WIFI_STATE_CHANGED_ACTION)
        filter.addAction(mReceiver?.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(mReceiver?.CONNECTIVITY_ACTION)
        filter.addAction(org.ecjtu.easyserver.server.Constants.ACTION_CLOSE_SERVER)
        filter.addAction(org.ecjtu.easyserver.server.Constants.ACTION_UPDATE_SERVER)
        registerReceiver(mReceiver, filter)

        presenter = MainPresenter()
    }

    private fun initialize() {
        mToolbar = findViewById(R.id.toolbar)
        mDrawerLayout = findViewById(R.id.drawer_layout)

        mDrawerToggle = ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, 0, 0)
        mDrawerToggle.syncState()
        mDrawerLayout.setDrawerListener(mDrawerToggle)

        mFloatingActionButton = findViewById<FloatingActionButton>(R.id.floating_action_button)
        mFloatingActionButton.setOnClickListener { view ->
            val dlg = FilePickDialogFragment(this)
            dlg.show(this.supportFragmentManager, TAG_FRAGMENT)
        }

        //for view switcher
        mViewSwitcher = findViewById(R.id.view_switcher)
        var view0: View = LayoutInflater.from(this).inflate(R.layout.layout_main_activity_data, null)
        var view1: View = LayoutInflater.from(this).inflate(R.layout.layout_main_activity_list, null)

        mViewSwitcher?.addView(view0)
        mViewSwitcher?.addView(view1)

        var drawer = findViewById<View>(R.id.drawer_view)
        if (isNavigationBarShow(this)) {
            drawer.setPadding(drawer.paddingLeft, drawer.paddingTop, drawer.paddingRight,
                    drawer.paddingBottom + getNavigationBarHeight(this))
            val recyclerView = mRecyclerView
            recyclerView?.setPadding(recyclerView.paddingLeft, recyclerView.paddingTop, recyclerView.paddingRight,
                    recyclerView.paddingBottom + getNavigationBarHeight(this))
        }

        view0.findViewById<View>(R.id.button_help).setOnClickListener {
            val intent = ActionBarFragmentActivity.newInstance(this, HelpFragment::class.java, title = "Help")
            this.startActivity(intent)
        }

        mWifiButton = findViewById<Button>(R.id.btn_wifi)
        mHotspotButton = findViewById<Button>(R.id.btn_hotspot)


        mWifiButton.setOnClickListener {
            val intent = Intent()
            val action = arrayOf(WifiManager.ACTION_PICK_WIFI_NETWORK, Settings.ACTION_WIFI_SETTINGS)
            for (str in action) {
                try {
                    intent.action = Settings.ACTION_WIFI_SETTINGS
                    this.startActivity(intent)
                    break
                } catch (ex: Exception) {
                }
            }
        }
        mHotspotButton.setOnClickListener {
            for (index in 0 until mRequestPermission.size) {
                if (ActivityCompat.checkSelfPermission(this@MainActivity, mRequestPermission[index]) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@MainActivity, mRequestPermission, REQUEST_CODE)
                    return@setOnClickListener
                }
            }

            val dlg = WifiBottomSheetDialog(this, this)
            dlg.show()
        }

        mApName = findViewById<TextView>(R.id.ap_name)

        mRecyclerView = view1 as RecyclerView
        mRecyclerView?.adapter = DeviceRecyclerViewAdapter(mDeviceInfoList, this)

        var manager: LinearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mRecyclerView?.layoutManager = manager

        mWifiImage = findViewById<ImageView>(R.id.image_wifi)

        mWifiImageStateMachine = object : StateMachine(this, R.array.main_activity_delegate_array, mWifiImage) {
            override fun updateView(index: Int) {
                val value = getArrayRefByIndex(index)
                value?.let {
                    mWifiImage.setImageResource(value)
                }
            }
        }

        checkCurrentNetwork(null)

        initDrawerLayout()

        shareDatabase = RoomRepository(this).shareDatabase
        doSearch()

        val filter = IntentFilter(ApDataDialog.ACTION_UPDATE_DEVICE)
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateDeviceInfoReceiver, filter)

        reconnectHistory()
    }

    fun doSearch() {

        val name = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL)
        var port = PreferenceManager.getDefaultSharedPreferences(this).getInt(Constants.PREF_SERVER_PORT, 0)

        if (port == 0) {
            port = this.getServerService()?.port ?: 0
            if (port == 0) return
        }

        this.getMainService()?.createHelper(name, port, "/API/Icon")
        this.getMainService()?.setMessageListener { ip, _, msg ->
            val state = this.getMainApplication().getSavedInstance().get(Constants.AP_STATE)
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

    private fun isSelf(self: String, ip: String): Boolean {
        if (DEBUG) return false

        if (self == ip) {
            return true
        }
        return false
    }

    private fun reconnectHistory() {
        thread {
            val okHttpClient = OkHttpClient()
            val listMsg = shareDatabase.ipMessageDao().allMessage
            for (msg in listMsg) {
                val request = Request.Builder()
                        .url("http://${msg.ip}:${msg.port}/api/Info")
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

        if (!flag) {
            val deviceInfo = DeviceInfo(deviceModel.name, ip, deviceModel.port, deviceModel.icon)
            mDeviceInfoList.add(deviceInfo)
            mRecyclerView?.adapter?.notifyDataSetChanged()
        } else {

            if (deviceModel.port != 0) {
                var needUpdate = false
                if (old?.port != deviceModel.port || old.icon != deviceModel.icon) {
                    needUpdate = true
                    Log.e(TAG, "need update recycler view")
                }

                old?.name = deviceModel.name
                old?.port = deviceModel.port
                old?.icon = deviceModel.icon

                if (needUpdate) {
                    mRecyclerView?.adapter?.notifyDataSetChanged()
                }
            }
        }

        val index = mViewSwitcher?.indexOfChild(mRecyclerView)
        val nextIndex = mViewSwitcher?.indexOfChild(mViewSwitcher?.nextView)
        if (mDeviceInfoList.size != 0) {
            if (index == nextIndex) {
                mViewSwitcher?.showNext()
            }
        } else {
            if (index != nextIndex) {
                mViewSwitcher?.showNext()
            }
        }
    }

    private fun initDrawerLayout() {
        mTextName = findViewById<TextView>(R.id.text_name)
        val locale = this.resources.configuration.locale
        val lan = locale.language.toLowerCase() + "_" + locale.country.toLowerCase()
        var faq = "faq.html"
        if (lan == "zh_cn") {
            faq = "faq_$lan.html"
        }
        findViewById<View>(R.id.text_faq)?.setOnClickListener {
            var intent = ActionBarFragmentActivity.newInstance(this, WebViewFragment::class.java,
                    WebViewFragment.openInnerUrl(faq), "FAQ")
            this.startActivity(intent)
        }

        findViewById<View>(R.id.text_setting)?.setOnClickListener {
            this.startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<View>(R.id.text_help)?.setOnClickListener {
            val intent = ActionBarFragmentActivity.newInstance(this, HelpFragment::class.java, title = "Help")
            this.startActivity(intent)
        }

        findViewById<View>(R.id.btn_close)?.setOnClickListener {
            this.getHandler()?.obtainMessage(MainActivity.MSG_CLOSE_APP)?.sendToTarget()
        }

        findViewById<View>(R.id.icon)?.setOnClickListener {
            var dlg = TextItemDialog(this)
            dlg.setupItem(arrayOf(this.getString(R.string.pick_from_camera), this.getString(R.string.pick_from_album), this.getString(R.string.cancel)))
            dlg.setOnClickListener { index ->
                if (index == 0) {
                    mImageHelper = null
                    mPhotoHelper = CapturePhotoHelper(this)
                    mPhotoHelper?.takePhoto()
                } else if (index == 1) {
                    mPhotoHelper = null
                    mImageHelper = PickPhotoHelper(this)
                    mImageHelper?.takePhoto()
                }
                dlg.cancel()
            }
            dlg.show()
        }

        findViewById<View>(R.id.text_name)?.setOnClickListener {
            var dlg = EditNameDialog(activity = this, context = this)
            dlg.show()
            dlg.setOnDismissListener {
                mTextName?.text = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL)
            }
        }

        mTextName?.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL))
        checkIconHead()
    }

    fun checkIconHead() {
        val iconFile = File(this.filesDir, Constants.ICON_HEAD)
        if (iconFile.exists()) {
            val icon = findViewById<View>(R.id.drawer_view)?.findViewById<View>(R.id.icon) as ImageView //有相同id 找到错误的view
            icon.setImageBitmap(BitmapFactory.decodeFile(iconFile.absolutePath))
            thread {
                var deviceInfo = this.getMainApplication().getSavedInstance().get(Constants.KEY_INFO_OBJECT) as DeviceInfo?
                deviceInfo?.iconPath = this.filesDir.absolutePath + "/" + Constants.ICON_HEAD
                val helper = ServerInfoParcelableHelper(this.filesDir.absolutePath)
                helper.put(Constants.KEY_INFO_OBJECT, deviceInfo)
                val intent = EasyServerService.getSetupServerIntent(this, Constants.KEY_INFO_OBJECT)
                this.startService(intent)
            }
        }
    }

    fun checkCurrentNetwork(info: WifiInfo?): Boolean {
        var hasAccess = false

        if (NetworkUtil.isWifi(this) || info != null) {
            var wifiInfo: WifiInfo? = null
            if (info != null)
                wifiInfo = info
            else
                wifiInfo = NetworkUtil.getConnectWifiInfo(this)

            mApName.setText(getRealName(wifiInfo!!.ssid))
            mWifiButton.isActivated = true
            mHotspotButton.isActivated = false
            mWifiImageStateMachine?.updateView(0)
            hasAccess = true
            this.getMainApplication().getSavedInstance().put(Constants.AP_STATE, Constants.NetWorkState.WIFI)

            this.getHandler()?.obtainMessage(MainActivity.MSG_START_SERVER)?.sendToTarget()
        } else if (NetworkUtil.isHotSpot(this)) {
            var config = NetworkUtil.getHotSpotConfiguration(this)
            mApName.setText(getRealName(config.SSID))
            mWifiButton.isActivated = false
            mHotspotButton.isActivated = true
            mWifiImageStateMachine?.updateView(1)
            hasAccess = true
            this.getMainApplication().getSavedInstance().put(Constants.AP_STATE, Constants.NetWorkState.AP)

            this.getHandler()?.obtainMessage(MainActivity.MSG_START_SERVER)?.sendToTarget()
        } else if (NetworkUtil.isMobile(this)) {
            mApName.setText(R.string.cellular)
            mWifiImageStateMachine?.updateView(2)

            mWifiButton.isActivated = false
            mHotspotButton.isActivated = false
            hasAccess = false
            this.getMainApplication().getSavedInstance().put(Constants.AP_STATE, Constants.NetWorkState.MOBILE)
        } else {
            mApName.setText(R.string.no_internet)
            mWifiImageStateMachine?.updateView(2)
            mWifiButton.isActivated = false
            mHotspotButton.isActivated = false
            hasAccess = false
            this.getMainApplication().getSavedInstance().put(Constants.AP_STATE, Constants.NetWorkState.NONE)
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

    private val mUpdateDeviceInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.extras != null) {
                val ip = intent.extras.getString(ApDataDialog.EXTRA_IP)
                if (!TextUtils.isEmpty(ip)) {
                    val json = intent.extras.getString(ApDataDialog.EXTRA_JSON)
                    try {
                        val deviceInfo = ConversionFactory.json2DeviceInfo(JSONObject(json))
                        ServerNotification(context!!).buildServerNotification(getString(R.string.searched_new_device), deviceInfo.name,
                                getString(R.string.app_name) + ":" + getString(R.string.find_new_device)).send()
                        if (mDeviceInfoList.indexOf(deviceInfo) < 0) {
                            mDeviceInfoList.add(deviceInfo)
                            mRecyclerView?.adapter?.notifyDataSetChanged()
                        }
                    } catch (ex: Exception) {
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        presenter.takeView(this)

        getMainApplication().closeActivitiesByIndex(1)
        var name = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL)
        (findViewById<View>(R.id.text_name) as TextView).setText(name)

        //resume service
        val intent = Intent(this, MainService::class.java)
        startService(intent)
        bindService(intent, mMainServiceConnection, Context.BIND_AUTO_CREATE)

    }

    override fun onStop() {
        super.onStop()
        presenter.dropView()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        refreshing = false
        // destroy
        getMainService()?.stopSearch()
        mPhotoHelper?.clearCache()
        mImageHelper?.clearCache()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateDeviceInfoReceiver)
        shareDatabase.close()

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
        // release main process
        System.exit(0)
        Glide.get(this).clearMemory()
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
        var result = {
            when (item?.getItemId()) {
                R.id.qr_code -> {

                    var map = this.getMainApplication().getSavedInstance()
                    var state = map.get(Constants.AP_STATE)
                    if (state == Constants.NetWorkState.MOBILE || state == Constants.NetWorkState.NONE) {
                        Toast.makeText(this, R.string.need_wifi_or_hotspot, Toast.LENGTH_SHORT).show()
                    } else {
                        val dialog = ApDataDialog(this)
                        SimpleDialogFragment(dialog).show(this.supportFragmentManager, "ap_data_dialog")
                    }
                    true
                }
                R.id.refresh -> {
                    if (this.refreshing) {
                        this.getMainService()?.startSearch()
                    } else {
                        this.getMainService()?.stopSearch()
                    }
                    true
                }
                R.id.search_ip -> {
                    val dlg = IPSearchDialog(this)
                    dlg.setCallback { ip ->
                        RequestManager.requestDeviceInfo(ip, object : IRequestCallback {
                            override fun onSuccess(httpURLConnection: HttpURLConnection?, response: String) {
                                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent().apply {
                                    setAction(ApDataDialog.ACTION_UPDATE_DEVICE).putExtra(ApDataDialog.EXTRA_IP, ip)
                                    putExtra(ApDataDialog.EXTRA_JSON, response)
                                })
                            }
                        })
                    }
                    dlg.show()
                }
            }
            false
        }

        if (result.invoke()) {
            return true
        }
        return super.onOptionsItemSelected(item)
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
                runOnUiThread { doSearch() }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_CODE) return
        var hasPermission = true

        for (index in 0 until mRequestPermission.size) {
            if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false
            }

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, mRequestPermission[index])) {
                this.startActivity(ActivityUtil.getAppDetailSettingIntent(this))
                return
            }
        }

        if (hasPermission) {
            var dialog = ApDataDialog(this)
            SimpleDialogFragment(dialog).show(this.supportFragmentManager, "ap_data_dialog")
        }
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
                if (checkCurrentNetwork(null) ?: false) {
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
                } else if (getHandler()?.hasMessages(MSG_LOADING_SERVER) == false) {
//                    PreferenceManager.getDefaultSharedPreferences(this).edit().remove(Constants.PREF_SERVER_PORT).apply()
                }

                if (!flag) {
                    var name = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL)
                    if (mService != null && mService!!.ip != null && mService!!.port != 0) {
                        val deviceInfo = getMainApplication().getSavedInstance().get(Constants.KEY_INFO_OBJECT) as DeviceInfo?
                        deviceInfo?.let {
                            registerServerInfo(mService!!.ip, mService!!.port, name,
                                    deviceInfo.fileMap)
                        }
                    }
                    runOnUiThread { doSearch() }
                }
            }
            MSG_STOP_SERVER -> {
                stopServerService()
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
        mPhotoHelper?.onActivityResult(requestCode, resultCode, data)
        mImageHelper?.onActivityResult(requestCode, resultCode, data)
        checkIconHead()
    }

    fun registerServerInfo(hostIP: String, port: Int, name: String, mutableMap: MutableMap<String, List<String>>) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(Constants.PREF_SERVER_PORT, port).apply()
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

    override fun onBackPressed() {
        if (lastBackPressTime < 0) {
            lastBackPressTime = System.currentTimeMillis()
            return
        }

        if (System.currentTimeMillis() - lastBackPressTime < CLOSE_TIME) {
            super.onBackPressed()
        }
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
            startServerService()
        }
    }

    fun getMainService(): MainService? {
        return mMainService
    }

    fun getServerService(): IAidlInterface? {
        return mService
    }

    fun stopServerService() {
        Log.i(TAG, "stopServerService")
        if (mService == null) return
        try {
            unbindService(mServiceConnection)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            stopService(Intent(this, EasyServerService::class.java))
            mService = null
        }
    }

    fun startServerService() {
        Log.i(TAG, "startServerService")
        if (mService == null) {
            var intent = Intent(this@MainActivity, EasyServerService::class.java)
            startService(intent)
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun permissionRejected() {
        finish()
    }
}
