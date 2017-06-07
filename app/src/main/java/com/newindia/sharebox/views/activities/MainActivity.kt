package com.newindia.sharebox.views.activities

import android.animation.ObjectAnimator
import android.graphics.drawable.RotateDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.newindia.sharebox.R
import com.newindia.sharebox.presenter.MainActivityDelegate

//http://www.tmtpost.com/195557.html 17.6.7
class MainActivity : AppCompatActivity() {

    private var mDelegate : MainActivityDelegate? =null

    private var mAnimator : ObjectAnimator? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var toolbar=findViewById(R.id.toolbar) as Toolbar

        setSupportActionBar(toolbar)
        mDelegate= MainActivityDelegate(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity,menu)
        var item=menu!!.findItem(R.id.refresh)
        var rotateDrawable=item.icon as RotateDrawable

        mAnimator=ObjectAnimator.ofInt(rotateDrawable, "level", 0, 10000) as ObjectAnimator?
        mAnimator?.setRepeatMode(ObjectAnimator.RESTART)
        mAnimator?.repeatCount=ObjectAnimator.INFINITE
        mAnimator?.setDuration(1000)
        mAnimator?.start()

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var result=mDelegate?.onOptionsItemSelected(item) ?: false

        when(item?.itemId){
            R.id.refresh->{
                if(mAnimator!!.isRunning){
                    mAnimator?.cancel()
                }else
                    mAnimator?.start()
            }
        }

        if(result){
            return result
        }
        return super.onOptionsItemSelected(item)
    }


}
