package com.ethan.and.ui.sendby;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.SparseArrayCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.common.componentes.fragment.LazyInitFragment;
import com.common.utils.activity.ActivityUtil;
import com.common.utils.file.FileUtil;
import com.ethan.and.ui.sendby.http.HttpManager;
import com.ethan.and.ui.sendby.entity.DownloadListResponse;
import com.flybd.sharebox.AppExecutorManager;
import com.flybd.sharebox.R;
import com.google.gson.Gson;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReceiveFileFragment extends LazyInitFragment {

    private static final String TAG = "ReceiveFileFragment";

    private Button btnReceive;

    private EditText etKey;

    private RecyclerView rvList;

    private List<DownloadListResponse.DownloadItem> downloadItems;

    private SparseArrayCompat<ViewState> viewState;

    class ViewState {
        int downloadId = 0;
        DownloadListResponse.DownloadItem downloadItem;
        long transferBytes = 0L;
        long totalBytes = 0L;
        long lastTransferBytes = 0L;
        Throwable error;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        downloadItems = new ArrayList<>();
        viewState = new SparseArrayCompat<>();
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
            AppExecutorManager.INSTANCE.getInstance().networkIO().execute(() -> {
                DownloadListResponse response = HttpManager.getInstance().getDownloadList(Constants.get().getRestUrl(), etKey.getText().toString());
                if (response != null) {
                    Log.i(TAG, "onViewCreated: " + new Gson().toJson(response));
                    getHandler().post(() -> {
                        if (response.getData() != null) {
                            downloadItems.clear();
                            downloadItems.addAll(response.getData());
                            rvList.getAdapter().notifyDataSetChanged();
                            btnReceive.setVisibility(View.INVISIBLE);
                            getHandler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Context ctx = getContext();
                                    if (ctx != null) {
                                        rvList.getAdapter().notifyDataSetChanged();
                                    }
                                    getHandler().postDelayed(this, 1000);
                                }
                            }, 1000);
                        }
                    });
                } else {
                    getHandler().post(() -> {
                        Context ctx = getContext();
                        if (ctx != null) {
                            Toast.makeText(ctx, "network error try again later", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
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

        rvList = view.findViewById(R.id.rv_list);
        rvList.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        rvList.setAdapter(new Adapter());
    }

    @Override
    public void onDestroy() {
        FileDownloader.getImpl().pauseAll();
        for (int i = 0; i < viewState.size(); i++) {
            int key = viewState.keyAt(i);
            ViewState state = viewState.get(key);
            if (state != null) {
                int id = state.downloadId;
                FileDownloader.getImpl().clear(id, state.downloadItem.getUrl());
            }
        }
        super.onDestroy();
        getHandler().removeCallbacksAndMessages(null);
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
            View v = getLayoutInflater().inflate(R.layout.layout_item_transfer, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            DownloadListResponse.DownloadItem item = downloadItems.get(position);

            ViewState state = viewState.get(position);
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String savedPath = downloadDir.getAbsolutePath() + File.separator + item.getFileName();
            if (state == null) {
                state = new ViewState();
                state.downloadItem = item;
                final ViewState finalViewState = state;
                File cache = new File(downloadDir.getAbsolutePath() + File.separator + item.getFileName());
                if (cache.exists()) {
                    cache.delete();
                }
                state.downloadId = FileDownloader.getImpl().create(state.downloadItem.getUrl())
                        .setPath(downloadDir.getAbsolutePath() + File.separator + item.getFileName())
                        .setCallbackProgressTimes(100)
                        .setListener(new FileDownloadLargeFileListener() {
                            @Override
                            protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                                finalViewState.transferBytes = soFarBytes;
                                finalViewState.totalBytes = totalBytes;
                            }

                            @Override
                            protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                                finalViewState.transferBytes = soFarBytes;
                                finalViewState.totalBytes = totalBytes;
                            }

                            @Override
                            protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                                finalViewState.transferBytes = soFarBytes;
                                finalViewState.totalBytes = totalBytes;
                            }

                            @Override
                            protected void completed(BaseDownloadTask task) {
                                if (task.isLargeFile()) {
                                    finalViewState.transferBytes = task.getLargeFileSoFarBytes();
                                    finalViewState.totalBytes = task.getLargeFileTotalBytes();
                                } else {
                                    finalViewState.transferBytes = task.getSmallFileSoFarBytes();
                                    finalViewState.totalBytes = task.getSmallFileTotalBytes();
                                }
                            }

                            @Override
                            protected void error(BaseDownloadTask task, Throwable e) {
                                finalViewState.error = e;
                            }

                            @Override
                            protected void warn(BaseDownloadTask task) {
                            }
                        })
                        .start();
                viewState.put(position, state);
            }

            holder.tvStatus.setText("等待中");
            holder.tvSize.setText("");
            byte status = FileDownloader.getImpl().getStatus(item.getUrl(), savedPath);
            if (status == FileDownloadStatus.progress) {
                long lastTransferBytes = state.lastTransferBytes;
                long speed = state.transferBytes - lastTransferBytes;
                state.lastTransferBytes = state.transferBytes;

                Formatter.BytesResult speedResult = Formatter.formatBytes(speed);
                holder.tvStatus.setText("下载中 " + speedResult.value + " " + speedResult.units);
                Formatter.BytesResult result = Formatter.formatBytes(state.totalBytes);
                holder.tvSize.setText(result.value + " " + result.units);
            } else if (status == FileDownloadStatus.completed) {
                holder.tvStatus.setText("下载完成");
                Formatter.BytesResult result = Formatter.formatBytes(state.totalBytes);
                holder.tvSize.setText(result.value + " " + result.units);
            } else if (status == FileDownloadStatus.INVALID_STATUS) {
                if (state.totalBytes == state.transferBytes && state.transferBytes > 0) {
                    holder.tvStatus.setText("下载完成");
                } else {
                    holder.tvStatus.setText("下载中");
                }
                Formatter.BytesResult result = Formatter.formatBytes(state.totalBytes);
                holder.tvSize.setText(result.value + " " + result.units);
            }

            holder.tvTitle.setText(item.getFileName());
            FileUtil.MediaFileType type = FileUtil.INSTANCE.getMediaFileTypeByName(item.getFileName());
            holder.ivIcon.setImageBitmap(null);
            setChildViewThumb(type, item.getUrl(), holder.ivIcon);
            float process = ((state.transferBytes * 1f) / (state.totalBytes * 1f));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.pbProgress.setProgress((int) (process * 100), false);
            } else {
                holder.pbProgress.setProgress((int) (process * 100));
            }
        }

        @Override
        public int getItemCount() {
            return downloadItems.size();
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
            //icon.setImageResource(R.mipmap.apk);
        } else if (type == FileUtil.MediaFileType.MP3) {
            icon.setImageResource(R.mipmap.music);
        } else if (type == FileUtil.MediaFileType.DOC) {
            icon.setImageResource(R.mipmap.document);
        } else if (type == FileUtil.MediaFileType.RAR) {
            icon.setImageResource(R.mipmap.rar);
        }
    }

    private boolean checkTaskFinished() {
        for (int i = 0; i < viewState.size(); i++) {
            int key = viewState.keyAt(i);
            ViewState state = viewState.get(key);
            if (state != null) {
                int id = state.downloadId;
                byte status = FileDownloader.getImpl().getStatus(id, state.downloadItem.getUrl());
                if (status == FileDownloadStatus.pending || status == FileDownloadStatus.progress || status == FileDownloadStatus.connected || status == FileDownloadStatus.started) {
                    return false;
                }
            }
        }
        return true;
    }
}
