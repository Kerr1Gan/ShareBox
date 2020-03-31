package com.ethan.and.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.PermissionChecker;

import com.common.componentes.fragment.LazyInitFragment;
import com.common.utils.activity.ActivityUtil;
import com.ethan.and.ui.sendby.SendByActivity;
import com.flybd.sharebox.R;

public class PermissionRequestFragment extends LazyInitFragment {

    Button btnNext;
    Button btnQuit;

    private String[] requestPermission = new String[]{
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_permission_request, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnNext = view.findViewById(R.id.btn_next);
        btnQuit = view.findViewById(R.id.btn_quit);
        btnNext.setActivated(true);
        View content = view.findViewById(R.id.content);
        content.setPadding(content.getPaddingLeft(), content.getPaddingTop() + ActivityUtil.getStatusBarHeight(view.getContext()), content.getPaddingRight(), content.getPaddingBottom());
        View body = view.findViewById(R.id.body);
        body.setPadding(body.getPaddingLeft(), body.getPaddingTop(), body.getPaddingRight(), body.getPaddingBottom() + ActivityUtil.getNavigationBarHeight(getActivity()));

        btnNext.setOnClickListener(v -> requestPermission());

        btnQuit.setOnClickListener(v -> {
            Activity activity = getActivity();
            if (activity != null) {
                activity.finish();
            }
        });

        for (String permission : requestPermission) {
            if (PermissionChecker.checkSelfPermission(view.getContext(), permission) != PermissionChecker.PERMISSION_GRANTED) {
                return;
            }
        }
        Activity activity = getActivity();
        if (activity != null) {
            startActivity(new Intent(activity, SendByActivity.class));
            activity.finish();
        }
    }

    private boolean requestPermission() {
        Activity ctx = getActivity();
        if (ctx == null) {
            return false;
        }
        for (String permission : requestPermission) {
            if (PermissionChecker.checkSelfPermission(ctx, permission) != PermissionChecker.PERMISSION_GRANTED) {
                requestPermissions(requestPermission, 101);
                return false;
            }
        }
        startActivity(new Intent(ctx, SendByActivity.class));
        ctx.finish();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            boolean hasPermission = true;
            for (int grant : grantResults) {
                if (grant == PackageManager.PERMISSION_DENIED) {
                    hasPermission = false;
                }
            }
            if (hasPermission) {
                Context ctx = getContext();
                if (ctx != null) {
                    startActivity(new Intent(ctx, SendByActivity.class));
                }
            }
        }
    }
}
