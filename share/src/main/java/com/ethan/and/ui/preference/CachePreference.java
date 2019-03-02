package com.ethan.and.ui.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.preference.DialogPreference;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import com.bumptech.glide.Glide;
import com.ecjtu.sharebox.R;

/**
 * Created by Ethan_Xiang on 2017/8/11.
 */

public class CachePreference extends DialogPreference {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CachePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CachePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CachePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CachePreference(Context context) {
        super(context, null);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setTitle(R.string.do_you_want_to_clear_the_cache);
        builder.setMessage(getContext().getString(R.string.you_will_clear_cache, getSharedPreferences().getString(getKey(), "")));
        builder.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(getContext()).clearDiskCache();
                    }
                }).start();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
    }
}
