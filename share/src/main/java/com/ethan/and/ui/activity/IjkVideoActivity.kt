package com.ethan.and.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import com.flybd.sharebox.R
import com.ethan.and.ui.fragment.IjkVideoFragment
import com.flybd.sharebox.util.firebase.FirebaseManager

/**
 * Created by xiang on 2018/3/24.
 */
class IjkVideoActivity : AppCompatActivity() {

    companion object {
        const val TAG = "IjkVideoFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_ijk_video)
        FirebaseManager.logEvent(FirebaseManager.Event.OPEN_VIEDEO_ACTIVITY, null)
        if (supportFragmentManager.findFragmentByTag(TAG) == null) {
            if (intent != null) {
                val fragment = IjkVideoFragment()
                fragment.arguments = intent.extras
                fragment.arguments?.putParcelable("data", intent.data)
                supportFragmentManager.beginTransaction()
                        .add(R.id.container, fragment, TAG)
                        .commit()
            } else {
                finish()
            }
        }
    }
}