package com.newindia.sharebox.views.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.newindia.sharebox.R
import com.newindia.sharebox.presenter.MainActivityDelegate

class MainActivity : AppCompatActivity() {

    private var mDelegate : MainActivityDelegate? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var toolbar=findViewById(R.id.toolbar) as Toolbar
//        toolbar.inflateMenu(R.menu.menu_main_activity)
        setSupportActionBar(toolbar)
        mDelegate= MainActivityDelegate(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var result=mDelegate?.onOptionsItemSelected(item) ?: false
        if(result){
            return result
        }
        return super.onOptionsItemSelected(item)
    }
}
