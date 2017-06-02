package com.newindia.sharebox.presenter

import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.widget.ViewSwitcher
import com.newindia.sharebox.R
import com.newindia.sharebox.views.activities.MainActivity

/**
 * Created by KerriGan on 2017/6/2.
 */
class MainActivityDelegate(owner:MainActivity):Delegate<MainActivity>(owner){

    private var mToolbar:Toolbar
    private var mDrawerLayout:DrawerLayout
    private var mDrawerToggle:ActionBarDrawerToggle
    private var mFloatingActionButton:FloatingActionButton
    private var mViewSwitcher:ViewSwitcher
    init {
        mToolbar = findViewById(R.id.toolbar) as Toolbar
        mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout

        mDrawerToggle = ActionBarDrawerToggle(owner, mDrawerLayout, mToolbar, 0, 0)
        mDrawerToggle!!.syncState()
        mDrawerLayout!!.setDrawerListener(mDrawerToggle)

        mFloatingActionButton=findViewById(R.id.floating_action_button) as FloatingActionButton
        mFloatingActionButton.setOnClickListener({view->
            Toast.makeText(owner,"onClick ",Toast.LENGTH_SHORT).show()
        })

        //for view switcher
        mViewSwitcher=findViewById(R.id.view_switcher) as ViewSwitcher
        var view0:View=LayoutInflater.from(owner).inflate(R.layout.layout_main_activity_data,null)
        var view1:View=LayoutInflater.from(owner).inflate(R.layout.layout_main_activity_list,null)
        mViewSwitcher.addView(view0)
        mViewSwitcher.addView(view1)

        view0.findViewById(R.id.button_help).setOnClickListener {
            var v=LayoutInflater.from(owner).inflate(R.layout.layout_main_activity_data,null)
            var dialog=BottomSheetDialog(owner)
            dialog.setContentView(v)
            dialog.show()
        }
//        val scroll = findViewById(R.id.content) as LinearLayout
//        for (i in 0..99) {
//            val b = Button(owner)
//            b.text = "1234"
//            b.layoutParams = LinearLayout.LayoutParams(-1, -2)
//            scroll.addView(b)
//        }
    }

    fun onOptionsItemSelected(item: MenuItem?): Boolean{
        when(item?.getItemId()){
            R.id.qr_code ->{
                Toast.makeText(owner,"打开QRCode",Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.refresh ->{
                Toast.makeText(owner,"刷新",Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return false
    }
}