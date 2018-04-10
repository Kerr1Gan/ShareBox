package com.ecjtu.sharebox

/**
 * Created by KerriGan on 2017/6/16 0016.
 */
object Constants{

    //NetWorkState.NONE
    const val AP_STATE="com.ecjtu.sharebox.AP_STATE"

    const val ICON_HEAD="head.png"

    const val KEY_INFO_OBJECT="com.ecjtu.sharebox.Info"

    const val PREF_SERVER_PORT ="pref_server_port"

    enum class NetWorkState{
        NONE,
        WIFI,
        AP,
        P2P,
        MOBILE
    }
}
