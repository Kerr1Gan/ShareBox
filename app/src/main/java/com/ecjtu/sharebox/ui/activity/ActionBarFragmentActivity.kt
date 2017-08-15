package com.ecjtu.sharebox.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem

class ActionBarFragmentActivity : BaseFragmentActivity() {
    companion object {
        @JvmOverloads fun newInstance(context: Context, fragment: Class<*>, bundle: Bundle? = null,
                                      clazz: Class<out Activity> = getActivityClazz()): Intent {
            return BaseFragmentActivity.newInstance(context, fragment, bundle, clazz)
        }

        fun getActivityClazz(): Class<out Activity> {
            return ActionBarFragmentActivity::class.java
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): kotlin.Boolean {
        if(item?.itemId == android.R.id.home){
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
