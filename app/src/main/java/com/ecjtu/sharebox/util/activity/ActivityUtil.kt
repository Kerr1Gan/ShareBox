package com.ecjtu.sharebox.util.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Created by Ethan_Xiang on 2017/8/10.
 */
object ActivityUtil{
    //Settings.ACTION_APPLICATION_DETAIL_SETTING
    fun getAppDetailSettingIntent(context: Context): Intent {
        var localIntent = Intent()
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null))
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW)
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails")
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName())
        }
        return localIntent
    }
}