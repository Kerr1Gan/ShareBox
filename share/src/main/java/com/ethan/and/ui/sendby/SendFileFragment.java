package com.ethan.and.ui.sendby;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.common.componentes.fragment.LazyInitFragment;
import com.common.utils.activity.ActivityUtil;
import com.ethan.and.ui.sendby.http.HttpManager;
import com.ethan.and.ui.sendby.http.bean.CommonResponse;
import com.flybd.sharebox.AppExecutorManager;
import com.flybd.sharebox.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SendFileFragment extends LazyInitFragment {

    private static final String TAG = "SendFileFragment";

    private static final String EXTRA = "extra";

    private RecyclerView rvList;

    private List<File> selectedFiles;

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

        rvList = view.findViewById(R.id.rv_list);
        rvList.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        rvList.setAdapter(new Adapter());

        Bundle bundle = getArguments();
        if (bundle != null) {
            Serializable serializable = bundle.getSerializable(EXTRA);
            if (serializable instanceof List) {
                this.selectedFiles = (List<File>) serializable;
                if (selectedFiles.size() > 0) {
                    List<String> names = new ArrayList<>();
                    for (File f : selectedFiles) {
                        names.add(f.getName());
                    }

                    AppExecutorManager.INSTANCE.getInstance().networkIO().execute(() -> {
                        CommonResponse response = HttpManager.getInstance().getCode(Constants.get().getRestUrl(), names);
                        Log.i(TAG, "onViewCreated: " + new Gson().toJson(response));

                        getHandler().post(() -> {
                            try {
                                UploadTask task = new UploadTask();
                                task.setCtx(getContext());
                                if (response.getData() != null) {
                                    JsonObject data = new Gson().fromJson(new Gson().toJson(response.getData()), JsonObject.class);
                                    if (data != null) {
                                        String key = data.get("key").getAsString();
                                        String url = data.get("server").getAsString();
                                        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(url)) {
                                            task.setKey(key);
                                            task.setFile(selectedFiles.get(0));
                                            task.setName(selectedFiles.get(0).getName());
                                            task.setUrl(url);
                                            UploadManager.getInstance().pushTask(task);
                                            rvList.getAdapter().notifyDataSetChanged();
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    });
                }
            }
        } else {
            Activity activity = getActivity();
            if (activity != null) {
                activity.finish();
            }
        }
    }

    private static class Holder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvSize;
        TextView tvStatus;
        ContentLoadingProgressBar pbProgress;

        public Holder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSize = itemView.findViewById(R.id.tv_size);
            tvStatus = itemView.findViewById(R.id.tv_status);
            pbProgress = itemView.findViewById(R.id.pb_progress);
        }
    }

    class Adapter extends RecyclerView.Adapter<Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.layout_item_transfer, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.pbProgress.setProgress(new Random().nextInt(100), true);
            } else {
                holder.pbProgress.setProgress(new Random().nextInt(100));
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
