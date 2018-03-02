package com.ecjtu.componentes.util

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager

/**
 * Created by KerriGan on 2017/11/18.
 */
object ImmersiveUtil {
    fun immersiveActivity(activity: Activity) {
        activity.apply {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)   //去除半透明状态栏
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) //一般配合fitsSystemWindows()使用, 或者在根部局加上属性android:fitsSystemWindows="true", 使根部局全屏显示
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.TRANSPARENT)
            }

            if (Build.VERSION.SDK_INT >= 24/*Build.VERSION_CODES.N*/) {
                try {
                    val decorViewClazz = Class.forName("com.android.internal.policy.DecorView")
                    val field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor")
                    field.isAccessible = true
                    field.setInt(window.decorView, Color.TRANSPARENT)  //改为透明
                } catch (e: Exception) {
                }
            }
        }
    }
}