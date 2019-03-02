package com.ethan.and

import android.app.Activity
import com.ethan.and.MainApplication

/**
 * Created by KerriGan on 2017/6/16 0016.
 */

fun Activity.getMainApplication(): MainApplication {
    return this.application as MainApplication
}