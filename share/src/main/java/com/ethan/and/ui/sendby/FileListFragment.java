package com.ethan.and.ui.sendby;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.common.componentes.fragment.LazyInitFragment;
import com.common.utils.file.FileOpenIntentUtil;
import com.common.utils.file.FileUtil;
import com.ethan.and.async.AppThumbTask;
import com.ethan.and.ui.dialog.TextItemDialog;
import com.flybd.sharebox.AppExecutorManager;
import com.flybd.sharebox.BuildConfig;
import com.flybd.sharebox.R;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileListFragment extends LazyInitFragment {

    private RecyclerView rvFileList;

    private List<File> fileList;

    private SparseArrayCompat<Boolean> viewState = new SparseArrayCompat<>();

    private FileUtil.MediaFileType type;

    private static final LruCache<String, Bitmap> sLruCache = new LruCache<>(24);

    public static FileListFragment newInstance(FileUtil.MediaFileType type) {
        FileListFragment ret = new FileListFragment();
        Bundle b = new Bundle();
        b.putString("extra", type.toString());
        ret.setArguments(b);
        return ret;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvFileList = view.findViewById(R.id.rv_file_list);
        rvFileList.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        rvFileList.setAdapter(new Adapter());
    }

    @Override
    public void onUserVisibleHintChanged(boolean isVisibleToUser) {
        super.onUserVisibleHintChanged(isVisibleToUser);
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        if (bundle != null) {
            String typeStr = bundle.getString("extra");
            this.type = FileUtil.MediaFileType.valueOf(typeStr);
            AppExecutorManager.INSTANCE.getInstance().networkIO().execute(() -> {
                Context ctx = getContext();
                if (ctx != null) {
                    fileList = findFilesWithType(ctx, type);
                    getHandler().post(() -> rvFileList.getAdapter().notifyDataSetChanged());
                    Log.i("FileListFragment", "onUserVisibleHintChanged: " + new Gson().toJson(fileList));
                }
            });
        }
    }

    public void clearSelected() {
        viewState.clear();
        rvFileList.getAdapter().notifyDataSetChanged();
    }

    private List<File> findFilesWithType(Context context, FileUtil.MediaFileType type) {
        List<File> list = null;
        List<String> strList;
        switch (type) {
            case MOVIE:
                list = FileUtil.INSTANCE.getAllMediaFile(context, null);
                strList = new ArrayList<>();
                for (File path : list) {
                    strList.add(path.getAbsolutePath());
                }
                break;
            case MP3:
                list = FileUtil.INSTANCE.getAllMusicFile(context, null);
                strList = new ArrayList<>();
                for (File path : list) {
                    strList.add(path.getAbsolutePath());
                }
                break;
            case IMG:
//                    list=FileUtil.getAllImageFile(mContext!!,null)
                list = FileUtil.INSTANCE.getImagesByDCIM(context);
                strList = new ArrayList<>();
                for (File path : list) {
                    strList.add(path.getAbsolutePath());
                }
                break;
            case DOC:
                list = FileUtil.INSTANCE.getAllDocFile(context, null);
                strList = new ArrayList<>();
                for (File path : list) {
                    strList.add(path.getAbsolutePath());
                }
                break;
            case APP:
                list = FileUtil.INSTANCE.getAllApkFile(context, null);
                List<PackageInfo> installAppInfo = FileUtil.INSTANCE.getInstalledApps(context, true);
                for (PackageInfo info : installAppInfo) {
                    File apkFile = new File(info.applicationInfo.sourceDir);
                    if (apkFile.exists()) {
                        list.add(apkFile);
                    }
                }
                strList = new ArrayList<>();
                for (File path : list) {
                    strList.add(path.getAbsolutePath());
                }
                break;
            case RAR:
                list = FileUtil.INSTANCE.getAllRarFile(context, null);
                strList = new ArrayList<>();
                for (File path : list) {
                    strList.add(path.getAbsolutePath());
                }
                break;
        }
        if (list != null) {
            try {
                Collections.sort(list, (o1, o2) -> {
                    long diff = o1.lastModified() - o2.lastModified();
                    if (diff > 0)
                        return -1;
                    else if (diff == 0)
                        return 0;
                    else
                        return 1;//如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
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

    private class Holder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView tvName;
        CheckBox checkBox;

        public Holder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            tvName = itemView.findViewById(R.id.text_name);
            checkBox = itemView.findViewById(R.id.check_box);
        }
    }

    private class Adapter extends RecyclerView.Adapter<Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.layout_file_item, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            File file = fileList.get(position);
            holder.tvName.setText(file.getName());
            boolean isChecked = false;
            Boolean cache = viewState.get(position);
            if (cache != null) {
                isChecked = cache;
            }
            holder.checkBox.setChecked(isChecked);

            String f = file.getAbsolutePath();

            FileUtil.MediaFileType type = FileListFragment.this.type;
            if (type != null) {
                holder.icon.setImageBitmap(null);
                setChildViewThumb(type, f, holder.icon);
            }
            holder.itemView.setOnClickListener(v -> {
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
                viewState.put(position, holder.checkBox.isChecked());
            });
            holder.checkBox.setOnClickListener(v -> {
                viewState.put(position, holder.checkBox.isChecked());
            });
            holder.itemView.setOnLongClickListener(v -> {
                Context ctx = getContext();
                if (ctx == null) {
                    return false;
                }
                final TextItemDialog dlg = new TextItemDialog(ctx);
                dlg.setupItem(new String[]{/*ctx.getString(R.string.open), */ctx.getString(R.string.open_by_others), ctx.getString(R.string.delete), ctx.getString(R.string.cancel)});
                dlg.setOnClickListener(index -> {
                    if (index == 0) {
                        openFile(file.getAbsolutePath());
                    } else if (index == 1) {
                        AlertDialog.Builder b = new AlertDialog.Builder(ctx);
                        b.setTitle("删除文件")
                                .setMessage("确认删除文件？")
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                    if (file.delete()) {
                                        fileList.remove(file);
                                        viewState.remove(position);
                                        rvFileList.getAdapter().notifyItemRemoved(position);
                                    }
                                }).create().show();
                    }
                    dlg.cancel();
                    return null;
                });
                dlg.show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return fileList == null ? 0 : fileList.size();
        }
    }

    protected boolean openFile(String path) {
        Context ctx = getContext();
        if (ctx == null) {
            return false;
        }
        Intent i = FileOpenIntentUtil.INSTANCE.openFile(path, BuildConfig.APPLICATION_ID + ".fileprovider", ctx);
        try {
            ctx.startActivity(i);
        } catch (Exception ignore) {
        }
        return true;
    }

    public List<File> getSelectedFiles() {
        if (fileList == null) {
            return null;
        }
        List<File> ret = new ArrayList<>();
        for (int i = 0; i < fileList.size(); i++) {
            File f = fileList.get(i);
            boolean selected = viewState.get(i) == null ? false : viewState.get(i);
            if (selected) {
                ret.add(f);
            }
        }
        return ret;
    }
}
