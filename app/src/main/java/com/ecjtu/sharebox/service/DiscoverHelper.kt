package com.ecjtu.sharebox.service

import android.os.Handler
import android.os.Message

/**
 * Created by KerriGan on 2017/6/19.
 */

class DiscoverHelper{

    companion object{
        val MSG_START_SEARCH_DEVICE= 0x1001
        val MSG_STOP_SEARCH_DEVICE = 0x1002
        val MSG_START_WAITING= 0x1003
        val MSG_STOP_WAITING= 0x1004
    }


    private var mHandler:Handler? =SimpleHandler()










    class SimpleHandler:Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
        }
    }

}
