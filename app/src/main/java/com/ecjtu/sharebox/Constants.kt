package com.ecjtu.sharebox

/**
 * Created by KerriGan on 2017/6/16 0016.
 */
object Constants{

    //NetWorkState.NONE
    val AP_STATE="com.ecjtu.sharebox.AP_STATE"



    enum class NetWorkState{
        NONE,
        WIFI,
        AP,
        P2P,
        MOBILE
    }
}
