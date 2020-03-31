package com.ethan.and.ui.sendby;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.common.componentes.fragment.LazyInitFragment;
import com.common.utils.activity.ActivityUtil;
import com.flybd.sharebox.R;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class SendFileFragment extends LazyInitFragment {

    private static final String EXTRA = "extra";

    public static Bundle getBundle(List<File> selectedFiles) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA, (Serializable) selectedFiles);
        return bundle;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_send_file, container, false);
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
    }

}
