package com.ecjtu.sharebox

import android.app.Activity

/**
 * Created by KerriGan on 2017/6/16 0016.
 */

fun Activity.getMainApplication():MainApplication{
    return this.application as MainApplication
}