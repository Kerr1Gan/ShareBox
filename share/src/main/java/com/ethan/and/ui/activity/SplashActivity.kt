package com.ethan.and.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.ethan.and.ui.main.MainActivity


class SplashActivity : AppCompatActivity() {

    companion object {
        private var sIsShow = false
    }

    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            handler = Handler(mainLooper)
        } catch (ex: Exception) {
            ex.printStackTrace()
            // NoSuchMethodError 部分机型这里会报异常？？？
            handler = Handler(Looper.getMainLooper())
        }
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
