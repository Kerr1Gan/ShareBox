package com.newindia.sharebox.presenter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiConfiguration
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.newindia.sharebox.R
import com.newindia.sharebox.views.activities.MainActivity
import com.newindia.sharebox.views.dialog.FilePickBottomSheetDialog
import com.newindia.sharebox.views.dialog.WifiBottomSheetDialog
import org.ecjtu.channellibrary.wifiutils.NetworkUtil
import android.support.v4.app.ActivityCompat.startActivity



/**
 * Created by KerriGan on 2017/6/2.
 */
class MainActivityDelegate(owner:MainActivity):Delegate<MainActivity>(owner){

    private var mToolbar:Toolbar
    private var mDrawerLayout:DrawerLayout
    private var mDrawerToggle:ActionBarDrawerToggle
    private var mFloatingActionButton:FloatingActionButton
    private var mViewSwitcher:ViewSwitcher? = null
    private var mWifiButton:Button
    private var mHotspotButton:Button
    private var mApName:TextView
    private var mWifiImage:ImageView
    init {
        mToolbar = findViewById(R.id.toolbar) as Toolbar
        mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout

        mDrawerToggle = ActionBarDrawerToggle(owner, mDrawerLayout, mToolbar, 0, 0)
        mDrawerToggle!!.syncState()
        mDrawerLayout!!.setDrawerListener(mDrawerToggle)

        mFloatingActionButton=findViewById(R.id.floating_action_button) as FloatingActionButton
        mFloatingActionButton.setOnClickListener({view->
            Toast.makeText(owner,"onClick ",Toast.LENGTH_SHORT).show()
            mViewSwitcher?.showNext()
        })

        //for view switcher
        mViewSwitcher=findViewById(R.id.view_switcher) as ViewSwitcher
        var view0:View=LayoutInflater.from(owner).inflate(R.layout.layout_main_activity_data,null)
        var view1:View=LayoutInflater.from(owner).inflate(R.layout.layout_main_activity_list,null)
        mViewSwitcher?.addView(view0)
        mViewSwitcher?.addView(view1)

        view0.findViewById(R.id.button_help).setOnClickListener {
            var dialog=WifiBottomSheetDialog(owner,owner)
            dialog.show()
        }

        mWifiButton=findViewById(R.id.btn_wifi) as Button
        mHotspotButton=findViewById(R.id.btn_hotspot) as Button


        mWifiButton.setOnClickListener {
//            mWifiButton.isActivated=!mWifiButton.isActivated
            val intent = Intent()
            intent.action = "android.net.wifi.PICK_WIFI_NETWORK"
            owner.startActivity(intent)
        }
        mHotspotButton.setOnClickListener {
//            mHotspotButton.isActivated=!mHotspotButton.isActivated
        }

        mApName=findViewById(R.id.ap_name) as TextView

        var recycler=view1 as RecyclerView
        recycler.adapter=object : RecyclerView.Adapter<Holder>(){

            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): Holder {
                return Holder(Button(owner))
            }

            override fun onBindViewHolder(holder: Holder?, position: Int) {
                (holder!!.itemView as Button).setText(""+position)
            }

            override fun getItemCount(): Int = 1000


        }
        var manager: LinearLayoutManager = LinearLayoutManager(owner, LinearLayoutManager.VERTICAL,false)
        recycler.layoutManager=manager

        mWifiImage=findViewById(R.id.image_wifi) as ImageView

        checkCurrentAp()
    }

    class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    }

    fun onOptionsItemSelected(item: MenuItem?): Boolean{

        when(item?.getItemId()){
            R.id.qr_code ->{
                Toast.makeText(owner,"打开QRCode",Toast.LENGTH_SHORT).show()
                var dialog=FilePickBottomSheetDialog(owner,owner)
                dialog.show()
                return true
            }
            R.id.refresh ->{
                Toast.makeText(owner,"刷新",Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return false
    }

    protected fun checkCurrentAp(){
        if(NetworkUtil.isWifi(owner)){
            var wifiInfo=NetworkUtil.getConnectWifiInfo(owner)
            mApName.setText(getRealName(wifiInfo.ssid))
            mWifiButton.isActivated=!mWifiButton.isActivated
            mWifiImage.setImageResource(R.mipmap.wifi)
        }else if(NetworkUtil.isHotSpot(owner)){
            var config=NetworkUtil.getHotSpotConfiguration(owner)
            mApName.setText(getRealName(config.SSID))
            mHotspotButton.isActivated=!mHotspotButton.isActivated
            mWifiImage.setImageResource(R.mipmap.hotspot)
        }else if(NetworkUtil.isMobile(owner)){
            mApName.setText(getRealName("Cellular"))
            mWifiImage.setImageResource(R.mipmap.wifi_off)
        }
    }

    private fun getRealName(name:String):String{
        var str=name
        if(str[0]=='"')
            str=str.drop(1)

        if(str[str.length-1]=='"')
            str=str.dropLast(1)
        return str
    }

    protected inner class WifiApReceiver : BroadcastReceiver() {
        val WIFI_AP_STATE_DISABLING = 10

        val WIFI_AP_STATE_DISABLED = 11

        val WIFI_AP_STATE_ENABLING = 12

        val WIFI_AP_STATE_ENABLED = 13

        val WIFI_AP_STATE_FAILED = 14

        val EXTRA_WIFI_AP_STATE = "wifi_state"

        val ACTION_WIFI_AP_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED"

        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(EXTRA_WIFI_AP_STATE, -1)
            val action = intent.action

            this@MainActivityDelegate.checkCurrentAp()

            if (action == ACTION_WIFI_AP_CHANGED) {
                when (state) {
                    WIFI_AP_STATE_ENABLED -> {


                        var s = ""
                        when (state) {
                            WIFI_AP_STATE_DISABLED -> s = "WIFI_AP_STATE_DISABLED"
                            WIFI_AP_STATE_DISABLING -> s = "WIFI_AP_STATE_DISABLING"
                            WIFI_AP_STATE_ENABLED -> s = "WIFI_AP_STATE_ENABLED"
                            WIFI_AP_STATE_ENABLING -> s = "WIFI_AP_STATE_ENABLED"
                            WIFI_AP_STATE_FAILED -> s = "WIFI_AP_STATE_FAILED"
                        }
                        Log.i("WifiApReceiver", s)
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
                        Log.i("WifiApReceiver", s)
                    }
                }
            }
        }
    }

}