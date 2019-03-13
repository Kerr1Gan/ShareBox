package com.common.utils;

import android.content.Context;

/**
 * Created by Ethan_Xiang on 2017/12/4.
 */

public class MetricUtil {

    public static float getWidthDP(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels / context.getResources().getDisplayMetrics().density;
    }

    public static float getHeightDP(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels / context.getResources().getDisplayMetrics().density;
    }
}
