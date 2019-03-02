package com.flybd.sharebox.util;

import android.content.Context;
import android.os.Build;

import java.lang.reflect.Method;

/**
 * Created by Ethan_Xiang on 2017/7/20.
 */

public class StatusBarUtil {
    public static final String STATUS_BAR_SERVICE = "statusbar";

    /**
     * 收起通知栏
     * @param context
     */
    public static void collapseStatusBar(Context context) {
        try {
            Object statusBarManager = context.getSystemService(STATUS_BAR_SERVICE);
            Method collapse;
            if (Build.VERSION.SDK_INT <= 16) {
                collapse = statusBarManager.getClass().getMethod("collapse");
            } else {
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
            }
            collapse.invoke(statusBarManager);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

}
