package com.ethan.and.ui.sendby;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.common.componentes.activity.ImmersiveFragmentActivity;
import com.common.utils.activity.ActivityUtil;
import com.ethan.and.ui.fragment.LoginFragment;
import com.ethan.and.ui.fragment.PaymentFragment;
import com.ethan.and.ui.sendby.http.HttpManager;
import com.ethan.and.ui.sendby.http.bean.CommonResponse;
import com.flybd.sharebox.AppExecutorManager;
import com.flybd.sharebox.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

public class SendByActivity extends ImmersiveFragmentActivity {

    private static final String TAG = "SendByActivity";

    private FileChooseFragment fileChooseFragment;

    private Button btnSend;

    private Button btnReceive;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendby);

        View content = findViewById(R.id.content);
        content.setPadding(content.getPaddingLeft(), content.getPaddingTop() + ActivityUtil.getStatusBarHeight(this), content.getPaddingRight(), content.getPaddingBottom());

        btnSend = findViewById(R.id.btn_send);
        btnSend.setActivated(true);
        btnSend.setOnClickListener(v -> fileChooseFragment.sendFiles());

        btnReceive = findViewById(R.id.btn_receive);
        btnReceive.setOnClickListener(v -> {
            Intent intent = ImmersiveFragmentActivity.newInstance(this, ReceiveFileFragment.class);
            startActivity(intent);
        });

        fileChooseFragment = new FileChooseFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.design_bottom_sheet, fileChooseFragment, "SendByActivity").commitAllowingStateLoss();

        Intent intent = ImmersiveFragmentActivity.newInstance(this, PaymentFragment.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (fileChooseFragment.isExpandable()) {
            fileChooseFragment.collapse();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppExecutorManager.INSTANCE.getInstance().networkIO().execute(() -> {
            try {
                CommonResponse response = HttpManager.getInstance().getConfig(Constants.get().getRestUrl());
                if (response != null) {
                    JsonObject data = new Gson().fromJson(new Gson().toJson(response.getData()), JsonObject.class);
                    if (data != null) {
                        JsonElement element = data.get("chunkSize");
                        if (element != null) {
                            long chunkSize = element.getAsLong();
                            Constants.get().setChunkSize(chunkSize);
                        }
                        element = data.get("chunkedStreamingMode");
                        if (element != null) {
                            long chunkSize = element.getAsLong();
                            Constants.get().setChunkedStreamingMode(chunkSize);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


}
