package com.ethan.and.ui.main

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.RotateDrawable
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.*
import com.common.componentes.activity.ActionBarFragmentActivity
import com.common.componentes.activity.ImmersiveFragmentActivity
import com.common.netcore.RequestManager
import com.common.netcore.network.IRequestCallback
import com.common.utils.activity.ActivityUtil
import com.common.utils.photo.CapturePhotoHelper
import com.common.utils.photo.PickPhotoHelper
import com.ethan.and.getMainApplication
import com.ethan.and.ui.activity.SettingsActivity
import com.ethan.and.ui.adapter.DeviceRecyclerViewAdapter
import com.ethan.and.ui.dialog.*
import com.ethan.and.ui.fragment.*
import com.ethan.and.ui.state.StateMachine
import com.flybd.sharebox.BuildConfig
import com.flybd.sharebox.Constants
import com.flybd.sharebox.PreferenceInfo
import com.flybd.sharebox.R
import com.flybd.sharebox.util.firebase.FirebaseManager
import com.google.firebase.analytics.FirebaseAnalytics
import org.ecjtu.easyserver.server.DeviceInfo
import org.ecjtu.easyserver.server.impl.service.EasyServerService
import org.ecjtu.easyserver.server.util.cache.ServerInfoParcelableHelper
import java.io.File
import java.net.HttpURLConnection
import kotlin.concurrent.thread

class MainActivity : ImmersiveFragmentActivity(), MainContract.View {

    companion object {
        private const val TAG = "MainActivity"
        const val DEBUG = true
        private const val CLOSE_TIME = 3 * 1000
        private const val REQUEST_CODE = 0x10

        private const val TAG_FRAGMENT = "FilePickDialogFragment"
    }

    private var mAnimator: ObjectAnimator? = null

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

    private var mWifiImageStateMachine: StateMachine? = null

    // ...
    // Obtain the FirebaseAnalytics instance.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        loadSplash()
        setContentView(R.layout.activity_main)
        var toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        initialize()

        presenter = MainPresenter()
        presenter.onCreate(this, getHandler()!!)
        presenter.registerWifiApReceiver(this)
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
            presenter.go2Setting()
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

        initDrawerLayout()
    }

    override fun updateNetworkInfo(apName: String, isWifi: Boolean, isHotspot: Boolean, state: Int) {
        mApName.text = apName
        mWifiButton.isActivated = isWifi
        mHotspotButton.isActivated = isHotspot
        mWifiImageStateMachine?.updateView(state)
    }

    override fun updateDeviceInfo(update: Boolean, deviceInfo: MutableList<DeviceInfo>) {
        if (update) {
            mRecyclerView?.adapter?.notifyDataSetChanged()
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
            this.getHandler()?.obtainMessage(MainPresenter.MSG_CLOSE_APP)?.sendToTarget()
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

        findViewById<View>(R.id.text_feedback)?.setOnClickListener {
            try {
                ActivityUtil.jumpToMarket(this, BuildConfig.APPLICATION_ID)
            } catch (ex: Exception) {
                ex.printStackTrace()
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

    override fun onResume() {
        super.onResume()
        presenter.takeView(this)

        getMainApplication().closeActivitiesByIndex(1)
        var name = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL)
        (findViewById<View>(R.id.text_name) as TextView).setText(name)
    }

    override fun onStop() {
        super.onStop()
        presenter.dropView()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        presenter.refresh(false)
        // destroy
        mPhotoHelper?.clearCache()
        mImageHelper?.clearCache()

        presenter.onDestroy(this)

        super.onDestroy()
        System.exit(0)
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
                    presenter.refresh(false)
                    mAnimator?.cancel()
                } else {
                    presenter.refresh(true)
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
                    if (presenter.isRefreshing()) {
                        presenter.startSearch()
                    } else {
                        presenter.stopSearch()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mPhotoHelper?.onActivityResult(requestCode, resultCode, data)
        mImageHelper?.onActivityResult(requestCode, resultCode, data)
        checkIconHead()
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
        } else {
            lastBackPressTime = -1
        }
    }

    override fun handleMessage(msg: Message) {
        presenter.handleMessage(msg)
    }

    private fun loadSplash() {
        val intent = ImmersiveFragmentActivity.newInstance(this, SplashFragment::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivity(intent)
    }

    override fun permissionRejected() {
        finish()
    }
}
