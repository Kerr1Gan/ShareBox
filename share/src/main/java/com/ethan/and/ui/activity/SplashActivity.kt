package com.ethan.and.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.ethan.and.ui.main.MainActivity


class SplashActivity : AppCompatActivity() {

    var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(mainLooper)
    }

    override fun onResume() {
        super.onResume()
        handler?.postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }, 1500)
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            handler?.removeCallbacksAndMessages(null)
            handler = null
        }
    }
}
