package com.ethan.and.ui.sendby;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.common.componentes.fragment.LazyInitFragment;
import com.common.utils.activity.ActivityUtil;
import com.flybd.sharebox.R;

public class ReceiveFileFragment extends LazyInitFragment {

    private Button btnReceive;

    private EditText etKey;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receive_file, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View content = view.findViewById(R.id.content);
        content.setPadding(content.getPaddingLeft(), content.getPaddingTop() + ActivityUtil.getStatusBarHeight(view.getContext()), content.getPaddingRight(), content.getPaddingBottom());

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Activity activity = getActivity();
            if (activity != null) {
                activity.finish();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("小技巧")
                .setMessage("观看一段广告加速传输效果")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                }).setNegativeButton(android.R.string.cancel, (dialog, which) -> {

        }).setCancelable(false).create().show();

        btnReceive = view.findViewById(R.id.btn_receive);
        btnReceive.setActivated(true);
        btnReceive.setOnClickListener(v -> {

        });

        etKey = view.findViewById(R.id.et_key);
        etKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s.toString())) {
                    btnReceive.setVisibility(View.VISIBLE);
                } else {
                    btnReceive.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
