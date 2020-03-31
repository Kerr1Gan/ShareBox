package com.ethan.and.ui.activity;

import android.content.Context;
import android.os.Bundle;

import com.common.componentes.activity.ImmersiveFragmentActivity;

import org.jetbrains.annotations.Nullable;

public class SimpleImmersiveFragmentActivity extends ImmersiveFragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }
}