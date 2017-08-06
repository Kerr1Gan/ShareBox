package com.ecjtu.sharebox.presenter

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.ecjtu.sharebox.Constants
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.domain.PreferenceInfo
import com.ecjtu.sharebox.getMainApplication
import com.ecjtu.sharebox.ui.activity.MainActivity
import com.ecjtu.sharebox.ui.activity.SettingsActivity
import com.ecjtu.sharebox.ui.adapter.DeviceRecyclerViewAdapter
import com.ecjtu.sharebox.ui.dialog.ApDataDialog
import com.ecjtu.sharebox.ui.dialog.EditNameDialog
import com.ecjtu.sharebox.ui.dialog.TextItemDialog
import com.ecjtu.sharebox.ui.dialog.WifiBottomSheetDialog
import com.ecjtu.sharebox.ui.fragment.FilePickDialogFragment
import com.ecjtu.sharebox.util.photo.CapturePhotoHelper
import com.ecjtu.sharebox.util.photo.PickPhotoHelper
import org.ecjtu.channellibrary.devicesearch.DeviceSearcher
import org.ecjtu.channellibrary.devicesearch.DiscoverHelper
import org.ecjtu.channellibrary.wifiutil.NetworkUtil
import org.ecjtu.easyserver.server.DeviceInfo
import java.io.File
import java.lang.Exception


/**
 * Created by KerriGan on 2017/6/2.
 */
class MainActivityDelegate(owner: MainActivity) : Delegate<MainActivity>(owner), ActivityCompat.OnRequestPermissionsResultCallback {

    private var mToolbar: Toolbar
    private var mDrawerLayout: DrawerLayout
    private var mDrawerToggle: ActionBarDrawerToggle
    private var mFloatingActionButton: FloatingActionButton
    private var mViewSwitcher: ViewSwitcher? = null
    private var mWifiButton: Button
    private var mHotspotButton: Button
    private var mApName: TextView
    private var mWifiImage: ImageView
    private var mTextName: TextView? = null

    private val REQUEST_CODE = 0x10;

    private var mServerSet = mutableListOf<DeviceSearcher.DeviceBean>()

    private var mClientSet = mutableListOf<DeviceSearcher.DeviceBean>()

    private var mDiscoverHelper: DiscoverHelper? = null

    private val mRequestPermission = arrayOf(Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE)

    private var mRecyclerView: RecyclerView? = null

    private var mDeviceInfoList: MutableList<DeviceInfo> = mutableListOf<DeviceInfo>()

    private var mPhotoHelper: CapturePhotoHelper? = null

    private var mImageHelper: PickPhotoHelper? = null

    private val DELAY_TIME=5000L

    companion object {
        //Settings.ACTION_APPLICATION_DETAIL_SETTING
        fun getAppDetailSettingIntent(context: Context): Intent {
            var localIntent = Intent()
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 9) {
                localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                localIntent.setData(Uri.fromParts("package", context.getPackageName(), null))
            } else if (Build.VERSION.SDK_INT <= 8) {
                localIntent.setAction(Intent.ACTION_VIEW)
                localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails")
                localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName())
            }
            return localIntent
        }

        const val DEBUG = true
    }

    init {
        mToolbar = findViewById(R.id.toolbar) as Toolbar
        mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout

        mDrawerToggle = ActionBarDrawerToggle(owner, mDrawerLayout, mToolbar, 0, 0)
        mDrawerToggle!!.syncState()
        mDrawerLayout!!.setDrawerListener(mDrawerToggle)

        mFloatingActionButton = findViewById(R.id.floating_action_button) as FloatingActionButton
        mFloatingActionButton.setOnClickListener({ view ->
            //            mViewSwitcher?.showNext()
            var dlg = FilePickDialogFragment(owner)
            dlg.show(owner.supportFragmentManager, "FilePickDialogFragment")
        })

        //for view switcher
        mViewSwitcher = findViewById(R.id.view_switcher) as ViewSwitcher
        var view0: View = LayoutInflater.from(owner).inflate(R.layout.layout_main_activity_data, null)
        var view1: View = LayoutInflater.from(owner).inflate(R.layout.layout_main_activity_list, null)
        mViewSwitcher?.addView(view0)
        mViewSwitcher?.addView(view1)

        view0.findViewById(R.id.button_help).setOnClickListener {

        }

        mWifiButton = findViewById(R.id.btn_wifi) as Button
        mHotspotButton = findViewById(R.id.btn_hotspot) as Button


        mWifiButton.setOnClickListener {
            val intent = Intent()
            val action = arrayOf(WifiManager.ACTION_PICK_WIFI_NETWORK, Settings.ACTION_WIFI_SETTINGS)
            for (str in action) {
                try {
                    intent.action = Settings.ACTION_WIFI_SETTINGS
                    owner.startActivity(intent)
                    break
                } catch (ex: Exception) {
                }
            }
        }
        mHotspotButton.setOnClickListener {
            for (index in 0..mRequestPermission.size - 1) {
                if (ActivityCompat.checkSelfPermission(owner, mRequestPermission[index]) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(owner, mRequestPermission, REQUEST_CODE)
                    return@setOnClickListener
                }
            }

            var dlg = WifiBottomSheetDialog(owner, owner)
            dlg.show()
        }

        mApName = findViewById(R.id.ap_name) as TextView

        mRecyclerView = view1 as RecyclerView
        mRecyclerView?.adapter = DeviceRecyclerViewAdapter(mDeviceInfoList, owner)

        var manager: LinearLayoutManager = LinearLayoutManager(owner, LinearLayoutManager.VERTICAL, false)
        mRecyclerView?.layoutManager = manager

        mWifiImage = findViewById(R.id.image_wifi) as ImageView

        checkCurrentAp(null)

        initDrawerLayout()

        doSearch()
    }

    private fun initDrawerLayout() {
        mTextName = findViewById(R.id.text_name) as TextView

        findViewById(R.id.text_faq)?.setOnClickListener {

        }

        findViewById(R.id.text_setting)?.setOnClickListener {
            owner.startActivity(Intent(owner,SettingsActivity::class.java))
        }

        findViewById(R.id.text_help)?.setOnClickListener {

        }

        findViewById(R.id.btn_close)?.setOnClickListener {
            owner.getHandler()?.obtainMessage(MainActivity.MSG_CLOSE_APP)?.sendToTarget()
        }

        findViewById(R.id.icon)?.setOnClickListener {
            var dlg = TextItemDialog(owner)
            dlg.setupItem(arrayOf("从照相机选择", "从相册选择", "取消"))
            dlg.setOnClickListener { index ->
                if (index == 0) {
                    mPhotoHelper = CapturePhotoHelper(owner)
                    mPhotoHelper?.takePhoto()
                } else if (index == 1) {
                    mImageHelper = PickPhotoHelper(owner)
                    mImageHelper?.takePhoto()
                }
                dlg.cancel()
            }
            dlg.show()
        }

        findViewById(R.id.text_name)?.setOnClickListener {
            var dlg = EditNameDialog(activity = owner, context = owner)
            dlg.show()
            dlg.setOnDismissListener({
                mTextName?.setText(PreferenceManager.getDefaultSharedPreferences(owner).
                        getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL))
            })
        }

        mTextName?.setText(PreferenceManager.getDefaultSharedPreferences(owner).
                getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL))
        checkIconHead()
    }

    fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.getItemId()) {
            R.id.qr_code -> {

                var map = owner.getMainApplication().getSavedInstance()
                var state = map.get(Constants.AP_STATE)

                if (state == Constants.NetWorkState.MOBILE || state == Constants.NetWorkState.NONE) {
                    Toast.makeText(owner, "需要连接WIFI或者开启热点", Toast.LENGTH_SHORT).show()
                } else {
                    var dialog = ApDataDialog(owner, owner)
                    dialog.show()
                }
                return true
            }
            R.id.refresh -> {
                if (owner.refreshing) {
                    mDiscoverHelper?.prepare(owner, true, true)
                    mDiscoverHelper?.start(true, true)
                } else {
                    mDiscoverHelper?.stop(true, true)
                }
                return true
            }
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != REQUEST_CODE) return
        var hasPermission = true

        for (index in 0..mRequestPermission.size - 1) {
            if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false
            }

            if (!ActivityCompat.shouldShowRequestPermissionRationale(owner, mRequestPermission[index])) {
                owner.startActivity(getAppDetailSettingIntent(owner))
                return
            }
        }

        if (hasPermission) {
            var dialog = ApDataDialog(owner, owner)
            dialog.show()
        }
    }

    fun checkCurrentAp(info: WifiInfo?): Boolean {
        var hasAccess = false

        if (NetworkUtil.isWifi(owner) || info != null) {
            var wifiInfo: WifiInfo? = null
            if (info != null)
                wifiInfo = info
            else
                wifiInfo = NetworkUtil.getConnectWifiInfo(owner)

            mApName.setText(getRealName(wifiInfo!!.ssid))
            mWifiButton.isActivated = true
            mHotspotButton.isActivated = false
            mWifiImage.setImageResource(R.mipmap.wifi)
            hasAccess = true
            owner.getMainApplication().getSavedInstance().put(Constants.AP_STATE, Constants.NetWorkState.WIFI)
        } else if (NetworkUtil.isHotSpot(owner)) {
            var config = NetworkUtil.getHotSpotConfiguration(owner)
            mApName.setText(getRealName(config.SSID))
            mWifiButton.isActivated = false
            mHotspotButton.isActivated = true
            mWifiImage.setImageResource(R.mipmap.hotspot)
            hasAccess = true
            owner.getMainApplication().getSavedInstance().put(Constants.AP_STATE, Constants.NetWorkState.AP)
        } else if (NetworkUtil.isMobile(owner)) {
            mApName.setText(getRealName("Cellular"))
            mWifiImage.setImageResource(R.mipmap.wifi_off)

            mWifiButton.isActivated = false
            mHotspotButton.isActivated = false
            hasAccess = false
            owner.getMainApplication().getSavedInstance().put(Constants.AP_STATE, Constants.NetWorkState.MOBILE)
        } else {
            mApName.setText(getRealName("No Internet"))
            mWifiImage.setImageResource(R.mipmap.wifi_off)
            mWifiButton.isActivated = false
            mHotspotButton.isActivated = false
            hasAccess = false
            owner.getMainApplication().getSavedInstance().put(Constants.AP_STATE, Constants.NetWorkState.NONE)
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

    fun doSearch() {

        var name = PreferenceManager.getDefaultSharedPreferences(owner).
                getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL)

        var obj = owner.getMainApplication().getSavedInstance().get(Constants.KEY_SERVER_PORT)
        var port = ""
        if (obj != null)
            port = obj as String

        if (TextUtils.isEmpty(port)) return

        mDiscoverHelper?.stop(true, true)

        mDiscoverHelper = DiscoverHelper(owner, name, port, "/API/Icon")
        mDiscoverHelper?.setMessageListener { msg, deviceSet, handler ->
            var state = owner.getMainApplication().getSavedInstance().get(Constants.AP_STATE)
            var ip = ""
            if (state == Constants.NetWorkState.WIFI) {
                val ips=NetworkUtil.getLocalWLANIps()
                if(ips.isNotEmpty())
                    ip = NetworkUtil.getLocalWLANIps()[0]
            } else if (state == Constants.NetWorkState.AP) {
                val ips=NetworkUtil.getLocalApIps()
                if(ips.isNotEmpty())
                    ip = NetworkUtil.getLocalApIps()[0]
            }
            when (msg) {
                DiscoverHelper.MSG_FIND_DEVICE -> {

                    for (obj in deviceSet) {
                        if (isSelf(ip, obj)) continue
                        val index = mClientSet.indexOf(obj)
                        if (index < 0) {
                            mClientSet.add(obj)
                        } else {
                            val old = mClientSet.get(index)
                            old.name = obj.name
                            old.room = obj.room
                        }
                    }
                    applyDeviceInfo(mClientSet)
                    if (owner.refreshing) {
                        var msg=handler.obtainMessage(DiscoverHelper.MSG_START_FIND_DEVICE)
                        handler.sendMessageDelayed(msg,DELAY_TIME)
                    }
                }
                DiscoverHelper.MSG_BEING_SEARCHED -> {
                    for (obj in deviceSet) {
                        if (isSelf(ip, obj)) continue
                        var index = mClientSet.indexOf(obj)
                        if (index < 0) {
                            mServerSet.add(obj)
                        } else {
                            var old = mClientSet.get(index)
                            old.name = obj.name
                            old.room = obj.room
                        }
                    }
                    applyDeviceInfo(mServerSet)
                    if (owner.refreshing) {
                        var msg=handler.obtainMessage(DiscoverHelper.MSG_START_BEING_SEARCHED)
                        handler.sendMessageDelayed(msg,DELAY_TIME)
                    }
                }
                DiscoverHelper.MSG_START_FIND_DEVICE -> {
                    mDiscoverHelper?.prepare(owner, true, true)
                    mDiscoverHelper?.start(true, true)
                }
                DiscoverHelper.MSG_START_BEING_SEARCHED -> {
                    mDiscoverHelper?.prepare(owner, true, true)
                    mDiscoverHelper?.start(true, true)
                }
            }
        }
        if (owner.refreshing) {
            mDiscoverHelper?.prepare(owner, true, true)
            mDiscoverHelper?.start(true, true)
        }
    }

    private fun applyDeviceInfo(mutableSet: MutableList<DeviceSearcher.DeviceBean>) {
        var flag: Boolean
        for (bean in mutableSet) {
            flag = false
            var old: DeviceInfo? = null
            for (info in mDeviceInfoList) {
                if (info.ip.equals(bean.ip)) {
                    flag = true
                    old = info
                }
            }
            if (!flag) {
                var data = bean.name
                var arr = data.split(",")
                var port = 0
                try {
                    port = Integer.parseInt(arr[1])
                } catch (e: Exception) {
                    port = 0
                }
                var deviceInfo = DeviceInfo(arr[0], bean.ip, port, arr[2])
                mDeviceInfoList.add(deviceInfo)
                mRecyclerView?.adapter?.notifyDataSetChanged()
            } else {
                var data = bean.name
                var arr = data.split(",")
                var port = 0
                try {
                    port = Integer.parseInt(arr[1])
                } catch (e: Exception) {
                    port = 0
                }
                if (port == 0) continue
                var needUpdate = false

                if (old?.port != port || old?.icon != arr[2]) needUpdate = true

                old?.name = arr[0]
                old?.port = port
                old?.icon = arr[2]

                if (needUpdate) {
                    mRecyclerView?.adapter?.notifyDataSetChanged()
                }
            }
        }

        var index = mViewSwitcher?.indexOfChild(mRecyclerView)
        var nextIndex = mViewSwitcher?.indexOfChild(mViewSwitcher?.nextView)
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


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mPhotoHelper?.onActivityResult(requestCode, resultCode, data)
        mImageHelper?.onActivityResult(requestCode, resultCode, data)
        checkIconHead()
    }

    fun checkIconHead() {
        var iconFile = File(owner.filesDir, Constants.ICON_HEAD)
        if (iconFile.exists()) {
            var icon = findViewById(R.id.drawer_view)?.findViewById(R.id.icon) as ImageView //有相同id 找到错误的view
            icon.setImageBitmap(BitmapFactory.decodeFile(iconFile.absolutePath))
        }
    }

    private fun isSelf(ip: String, device: DeviceSearcher.DeviceBean): Boolean {
        if (DEBUG) return false

        if (ip == device.ip) {
            return true
        }
        return false
    }

    fun onDestroy() {
        mDiscoverHelper?.stop(true, true)
    }

    fun hasDiscovered(): Boolean {
        return if (mDiscoverHelper != null) return true else return false
    }
}