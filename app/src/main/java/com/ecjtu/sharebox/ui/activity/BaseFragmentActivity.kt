package com.ecjtu.sharebox.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils

import com.ecjtu.sharebox.R

/**
 * Created by KerriGan on 2017/5/21.
 */

abstract class BaseFragmentActivity : BaseActionActivity() {

    companion object {

        private val EXTRA_FRAGMENT_NAME = "extra_fragment_name"
        private val EXTRA_FRAGMENT_ARG = "extra_fragment_arguments"

        @JvmOverloads fun newInstance(context: Context, fragment: Class<*>, bundle: Bundle? = null): Intent {
            val intent = Intent(context, BaseFragmentActivity::class.java)
            intent.putExtra(EXTRA_FRAGMENT_NAME, fragment.name)
            intent.putExtra(EXTRA_FRAGMENT_ARG, bundle)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_fragment)
        val fragmentName = intent.getStringExtra(EXTRA_FRAGMENT_NAME)
        var fragment: Fragment? = null
        if (TextUtils.isEmpty(fragmentName)) {
            //set default fragment
//            fragment = makeFragment(MainFragment::class.java!!.getName())
        } else {
            val args = intent.getBundleExtra(EXTRA_FRAGMENT_ARG)
            try {
                fragment = makeFragment(fragmentName)
                if (args != null)
                    fragment!!.arguments = args
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        if (fragment == null) return

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
    }

    fun makeFragment(name: String): Fragment? {
        try {
            val fragmentClazz = Class.forName(name)
            val fragment = fragmentClazz.newInstance() as Fragment
            return fragment
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}
