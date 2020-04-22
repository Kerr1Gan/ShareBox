package com.ethan.and.ui.sendby;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.common.componentes.fragment.LazyInitFragment;
import com.common.utils.activity.ActivityUtil;
import com.common.utils.file.FileUtil;
import com.ethan.and.async.AppThumbTask;
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

public class SendFileFragment extends LazyInitFragment {

    private static final String TAG = "SendFileFragment";

    private static final String EXTRA = "extra";

    private RecyclerView rvList;

    private List<File> selectedFiles;

    private TextView tvKey;

    private int taskHash = 0;

    private long lastBytesTransfer = 0L;

    private static final LruCache<String, Bitmap> sLruCache = new LruCache<>(24);

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

        tvKey = view.findViewById(R.id.tv_key);

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
                                            taskHash = UploadManager.getInstance().pushTask(task);
                                            rvList.getAdapter().notifyDataSetChanged();
                                            tvKey.setText(key);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    });
                    getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rvList.getAdapter().notifyDataSetChanged();
                            getHandler().postDelayed(this, 1000);
                        }
                    }, 500);
                }
            }
        } else {
            Activity activity = getActivity();
            if (activity != null) {
                activity.finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        getHandler().removeCallbacksAndMessages(null);
        UploadTask tsk = UploadManager.getInstance().getTask(taskHash);
        if (tsk != null) {
            tsk.stop();
        }
        super.onDestroy();
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

    private class Adapter extends RecyclerView.Adapter<Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.layout_item_transfer, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            File file = selectedFiles.get(position);
            holder.tvTitle.setText(file.getName());
            Formatter.BytesResult result = Formatter.formatBytes(file.length());
            holder.tvSize.setText(result.value + " " + result.units);
            FileUtil.MediaFileType type = FileUtil.INSTANCE.getMediaFileTypeByName(file.getName());
            holder.ivIcon.setImageBitmap(null);
            setChildViewThumb(type, file.getAbsolutePath(), holder.ivIcon);

            holder.tvStatus.setText("等待中");
            UploadTask task = UploadManager.getInstance().getTask(taskHash);
            if (task != null) {
                if (task.getStatus() == UploadTask.Status.IDLE) {
                    holder.tvStatus.setText("等待中");
                } else if (task.getStatus() == UploadTask.Status.RUNNING) {
                    long transfer = task.getTransferBytes().get() - lastBytesTransfer;
                    lastBytesTransfer = task.getTransferBytes().get();
                    holder.tvStatus.setText("上传中 " + Formatter.formatBytes(transfer).value + " " + Formatter.formatBytes(transfer).units);
                } else if (task.getStatus() == UploadTask.Status.END) {
                    holder.tvStatus.setText("结束");
                }
                float process = ((task.getTransferBytes().get() * 1f) / (file.length() * 1f));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    holder.pbProgress.setProgress((int) (process * 100), false);
                } else {
                    holder.pbProgress.setProgress((int) (process * 100));
                }
            }
        }

        @Override
        public int getItemCount() {
            return selectedFiles == null ? 0 : selectedFiles.size();
        }
    }

    protected void setChildViewThumb(FileUtil.MediaFileType type, String f, ImageView icon) {
        setChildViewThumb(type, f, icon, null);
    }

    protected void setChildViewThumb(FileUtil.MediaFileType type, String f, ImageView icon, RequestOptions options) {
        Context ctx = getContext();
        if (ctx == null) {
            return;
        }
        if (type == FileUtil.MediaFileType.MOVIE ||
                type == FileUtil.MediaFileType.IMG) {
            if (options == null) {
                Glide.with(ctx).load(f).listener(null).into(icon);
            } else {
                Glide.with(ctx).load(f).listener(null).apply(options).into(icon);
            }
        } else if (type == FileUtil.MediaFileType.APP) {
            Bitmap b = sLruCache.get(f);
            if (b == null) {
                AppThumbTask task = new AppThumbTask(sLruCache, ctx, icon);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new File(f));
            } else
                icon.setImageBitmap(b);
        } else if (type == FileUtil.MediaFileType.MP3) {
            icon.setImageResource(R.mipmap.music);
        } else if (type == FileUtil.MediaFileType.DOC) {
            icon.setImageResource(R.mipmap.document);
        } else if (type == FileUtil.MediaFileType.RAR) {
            icon.setImageResource(R.mipmap.rar);
        }
    }
}
