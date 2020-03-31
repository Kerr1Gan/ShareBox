package com.ethan.and.ui.sendby;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.common.componentes.activity.ImmersiveFragmentActivity;
import com.common.utils.activity.ActivityUtil;
import com.flybd.sharebox.R;

import org.jetbrains.annotations.Nullable;

public class SendByActivity extends ImmersiveFragmentActivity {

    private View bottom;

    private FileChooseFragment fileChooseFragment;

    private Button btnSend;

    private Button btnReceive;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendby);

        View content = findViewById(R.id.content);
        content.setPadding(content.getPaddingLeft(), content.getPaddingTop() + ActivityUtil.getStatusBarHeight(this), content.getPaddingRight(), content.getPaddingBottom());

        bottom = findViewById(R.id.design_bottom_sheet);
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
    }

    @Override
    public void onBackPressed() {
        if (fileChooseFragment.isExpandable()) {
            fileChooseFragment.collapse();
        } else {
            super.onBackPressed();
        }
    }
}
