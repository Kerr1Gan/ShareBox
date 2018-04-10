package com.ecjtu.sharebox.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.ui.fragment.IjkVideoFragment

/**
 * Created by xiang on 2018/3/24.
 */
class IjkVideoActivity : AppCompatActivity() {

    companion object {
        const val TAG = "IjkVideoFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ijk_video)

        if (supportFragmentManager.findFragmentByTag(TAG) == null) {
            val fragment = IjkVideoFragment()
            if (intent != null) {
                fragment.arguments = intent.extras
                fragment.arguments.putParcelable("data", intent.data)
                supportFragmentManager.beginTransaction()
                        .add(R.id.container, fragment, TAG)
                        .commit()
            } else {
                finish()
            }
        }
    }
}