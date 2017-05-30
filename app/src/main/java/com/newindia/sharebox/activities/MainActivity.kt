package com.newindia.sharebox.activities

import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.Button
import android.widget.LinearLayout
import com.newindia.sharebox.R

class MainActivity : AppCompatActivity() {

    private var mToolbar: Toolbar? = null

    private var mDrawerLayout: DrawerLayout? = null

    private var mDrawerToggle: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mToolbar = findViewById(R.id.toolbar) as Toolbar
        mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout

        mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout
        mDrawerToggle = ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, 0, 0)
        mDrawerToggle!!.syncState()
        mDrawerLayout!!.setDrawerListener(mDrawerToggle)
        val scroll = findViewById(R.id.content) as LinearLayout
        for (i in 0..99) {
            val b = Button(this)
            b.text = "1234"
            b.layoutParams = LinearLayout.LayoutParams(-1, -2)
            scroll.addView(b)
        }
    }

}
