package com.sendby;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.common.componentes.activity.ImmersiveFragmentActivity;
import com.flybd.sharebox.R;
import com.sendby.fragment.BackPressListener;

import org.jetbrains.annotations.Nullable;

public class SendFileActivity extends ImmersiveFragmentActivity {

    private static final String TAG = "SendFileActivity";

    public static Intent getIntent(Context context, Bundle extra) {
        Intent intent = new Intent(context, SendFileActivity.class);
        intent.putExtra("extra", extra);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG);
        if (fragment == null) {
            Bundle bundle = null;
            Intent intent = getIntent();
            if (intent != null) {
                bundle = intent.getBundleExtra("extra");
            }
            fragment = new SendFileFragment();
            fragment.setArguments(bundle);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment, TAG).commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG);
        if (fragment instanceof BackPressListener) {
            if (!((BackPressListener) fragment).onBackPress()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}
