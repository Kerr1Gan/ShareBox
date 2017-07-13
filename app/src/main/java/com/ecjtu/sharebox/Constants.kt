package com.ecjtu.sharebox

/**
 * Created by KerriGan on 2017/6/16 0016.
 */
object Constants{

    //NetWorkState.NONE
    const val AP_STATE="com.ecjtu.sharebox.AP_STATE"

    const val ICON_HEAD="head.png"

    enum class NetWorkState{
        NONE,
        WIFI,
        AP,
        P2P,
        MOBILE
    }
}
