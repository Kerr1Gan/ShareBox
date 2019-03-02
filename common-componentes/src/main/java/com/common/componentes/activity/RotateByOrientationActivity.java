package com.common.componentes.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.OrientationEventListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.jvm.internal.Intrinsics;

/**
 * Created by Ethan_Xiang on 2017/10/16.
 */

public class RotateByOrientationActivity extends BaseFragmentActivity {

    private OrientationEventListener mOrientationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initOrientationListener();
    }

    private void initOrientationListener() {
        mOrientationListener = new OrientationEventListener(this) {
            public void onOrientationChanged(int rotation) {
                // 设置竖屏
                if (rotation >= 0 && rotation <= 45 || rotation >= 315 || rotation >= 135 && rotation <= 225) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else if (rotation > 45 && rotation < 135 || rotation > 225 && rotation < 315) {
                    // 设置横屏
                    if (rotation > 225 && rotation < 315) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    } else {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    }
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOrientationListener.enable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOrientationListener.disable();
    }

    @NotNull
    public static Intent newInstance(@NotNull Context context, @NotNull Class fragment, @Nullable Bundle bundle, @NotNull Class clazz) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(fragment, "fragment");
        Intrinsics.checkParameterIsNotNull(clazz, "clazz");
        return newInstance(context, fragment, bundle, clazz);
    }

    @NotNull
    public static Intent newInstance(@NotNull Context context, @NotNull Class fragment, @Nullable Bundle bundle) {
        return RotateNoCreateActivity.newInstance(context, fragment, bundle, getActivityClazz());
    }

    @NotNull
    public static Intent newInstance(@NotNull Context context, @NotNull Class fragment) {
        return RotateNoCreateActivity.newInstance(context, fragment, null);
    }

    protected static Class<? extends Activity> getActivityClazz() {
        return RotateByOrientationActivity.class;
    }
}
