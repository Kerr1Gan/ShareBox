package com.ethan.and.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.ethan.and.ui.main.MainActivity


class SplashActivity : AppCompatActivity() {

    companion object {
        private var sIsShow = false
    }

    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(mainLooper)
    }

    override fun onResume() {
        super.onResume()
        if (!sIsShow) {
            handler?.postDelayed({
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }, 500)
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        sIsShow = true
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            handler?.removeCallbacksAndMessages(null)
            handler = null
        }
    }
}
