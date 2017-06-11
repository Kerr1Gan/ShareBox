package com.newindia.sharebox.presenter

import android.content.Intent
import android.net.wifi.WifiInfo
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.newindia.sharebox.R
import com.newindia.sharebox.domain.PreferenceInfo
import com.newindia.sharebox.ui.activities.MainActivity
import com.newindia.sharebox.ui.dialog.WifiBottomSheetDialog
import org.ecjtu.channellibrary.wifiutils.NetworkUtil
import com.newindia.sharebox.ui.dialog.ApDataDialog
import com.newindia.sharebox.ui.dialog.EditNameDialog


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
    private var mTextName:TextView? =null

    init {
        mToolbar = findViewById(R.id.toolbar) as Toolbar
        mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout

        mDrawerToggle = ActionBarDrawerToggle(owner, mDrawerLayout, mToolbar, 0, 0)
        mDrawerToggle!!.syncState()
        mDrawerLayout!!.setDrawerListener(mDrawerToggle)

        mFloatingActionButton=findViewById(R.id.floating_action_button) as FloatingActionButton
        mFloatingActionButton.setOnClickListener({view->
            mViewSwitcher?.showNext()
        })

        //for view switcher
        mViewSwitcher=findViewById(R.id.view_switcher) as ViewSwitcher
        var view0:View=LayoutInflater.from(owner).inflate(R.layout.layout_main_activity_data,null)
        var view1:View=LayoutInflater.from(owner).inflate(R.layout.layout_main_activity_list,null)
        mViewSwitcher?.addView(view0)
        mViewSwitcher?.addView(view1)

        view0.findViewById(R.id.button_help).setOnClickListener {

        }

        mWifiButton=findViewById(R.id.btn_wifi) as Button
        mHotspotButton=findViewById(R.id.btn_hotspot) as Button


        mWifiButton.setOnClickListener {
            val intent = Intent()
//            intent.action = "android.net.wifi.PICK_WIFI_NETWORK"
            intent.action =Settings.ACTION_WIFI_SETTINGS
            owner.startActivity(intent)
        }
        mHotspotButton.setOnClickListener {
            var dlg=WifiBottomSheetDialog(owner,owner)
            dlg.show()
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

        checkCurrentAp(null)

        initDrawerLayout()

    }

    private fun initDrawerLayout(){
        mTextName=findViewById(R.id.text_name) as TextView

        findViewById(R.id.text_faq)?.setOnClickListener {

        }

        findViewById(R.id.text_setting)?.setOnClickListener {

        }

        findViewById(R.id.text_help)?.setOnClickListener {

        }

        findViewById(R.id.text_name)?.setOnClickListener {
            var dlg=EditNameDialog(activity = owner,context =owner )
            dlg.show()
            dlg.setOnDismissListener({
                mTextName?.setText(PreferenceManager.getDefaultSharedPreferences(owner).
                        getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL))
            })
        }

        mTextName?.setText(PreferenceManager.getDefaultSharedPreferences(owner).
                getString(PreferenceInfo.PREF_DEVICE_NAME, Build.MODEL))
    }

    class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    }

    fun onOptionsItemSelected(item: MenuItem?): Boolean{

        when(item?.getItemId()){
            R.id.qr_code ->{
                var dialog=ApDataDialog(owner,owner)
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


    public fun checkCurrentAp(info:WifiInfo?){
        if(NetworkUtil.isWifi(owner) || info!=null){
            var wifiInfo:WifiInfo?=null
            if(info!=null)
                wifiInfo=info
            else
                wifiInfo=NetworkUtil.getConnectWifiInfo(owner)

            mApName.setText(getRealName(wifiInfo!!.ssid))
            mWifiButton.isActivated=true
            mHotspotButton.isActivated=false
            mWifiImage.setImageResource(R.mipmap.wifi)
        }else if(NetworkUtil.isHotSpot(owner)){
            var config=NetworkUtil.getHotSpotConfiguration(owner)
            mApName.setText(getRealName(config.SSID))
            mWifiButton.isActivated=false
            mHotspotButton.isActivated=true
            mWifiImage.setImageResource(R.mipmap.hotspot)
        }else if(NetworkUtil.isMobile(owner)){
            mApName.setText(getRealName("Cellular"))
            mWifiImage.setImageResource(R.mipmap.wifi_off)

            mWifiButton.isActivated=false
            mHotspotButton.isActivated=false
        }else{
            mApName.setText(getRealName("No Internet"))
            mWifiImage.setImageResource(R.mipmap.wifi_off)
            mWifiButton.isActivated=false
            mHotspotButton.isActivated=false
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



}