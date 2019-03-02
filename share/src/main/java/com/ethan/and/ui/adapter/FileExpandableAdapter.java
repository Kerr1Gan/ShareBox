package com.ethan.and.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.ecjtu.componentes.activity.ActionBarFragmentActivity;
import com.ecjtu.componentes.activity.RotateNoCreateActivity;
import com.ecjtu.sharebox.R;
import com.ethan.and.async.AppThumbTask;
import com.ethan.and.ui.dialog.TextItemDialog;
import com.ethan.and.ui.fragment.IjkVideoFragment;
import com.ethan.and.ui.fragment.WebViewFragment;
import com.ethan.and.ui.holder.FileExpandableInfo;
import com.ethan.and.ui.holder.TabItemInfo;
import com.ethan.and.ui.widget.FileExpandableListView;
import com.ecjtu.sharebox.util.cache.CacheUtil;
import com.ecjtu.sharebox.util.file.FileOpenIntentUtil;
import com.ecjtu.sharebox.util.file.FileUtil;
import com.ecjtu.sharebox.util.image.ImageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Created by Ethan_Xiang on 2017/7/10.
 */

public class FileExpandableAdapter extends BaseExpandableListAdapter implements View.OnClickListener,
        View.OnLongClickListener {

    protected TabItemInfo mTabHolder;

    private List<String> mFileList;

    private static final int CACHE_SIZE = 5 * 1024 * 1024;

    private static LruCache<String, Bitmap> sLruCache = new LruCache<String, Bitmap>(CACHE_SIZE) {
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    private List<FileExpandableInfo> mPropertyList = new ArrayList<>();

    private Worker mWorker = null;

    private FileExpandableListView mExpandableListView;

    private Context mContext;

    private String mTitle = "";

    private boolean mSelectAll = false;

    private String[] mInstalledAppNames;

    public FileExpandableAdapter(FileExpandableListView expandableListView) {
        mExpandableListView = expandableListView;
        mContext = expandableListView.getContext();
    }

    public void initData(TabItemInfo holder, List<FileExpandableInfo> oldCache) {
        mTabHolder = holder;
        mFileList = mTabHolder.getFileList();

        mExpandableListView.setGroupIndicator(null);
        mExpandableListView.setDivider(new ColorDrawable(Color.TRANSPARENT));
        mExpandableListView.setChildDivider(new ColorDrawable(Color.DKGRAY));
        mExpandableListView.setDividerHeight(1);

        if (oldCache != null) {
            mPropertyList = oldCache;
        }
        mExpandableListView.setAdapter(this);
    }

    public void loadedData() {
        mFileList = mTabHolder.getFileList();
        notifyDataSetChanged();
        if (mWorker == null && mFileList != null) {
            mWorker = new Worker();
            mWorker.start();
        }
    }

    @Override
    public void onClick(View v) {
        if (!(v.getTag() instanceof FileExpandableInfo)) return;
        FileExpandableInfo vh = (FileExpandableInfo) v.getTag();
        int position = mPropertyList.indexOf(vh);
        int id = v.getId();

        if (id == R.id.select_all) {
            if (vh.isActivated()) {
                vh.activate(false);
            } else {
                vh.activate(true);
            }
            notifyDataSetChanged();
        } else {
            boolean isExpand = mExpandableListView.isGroupExpanded(position);
            if (isExpand)
                mExpandableListView.collapseGroup(position);
            else
                mExpandableListView.expandGroup(position, true);
        }
    }

    @Override
    public boolean onLongClick(final View v) {
        final TextItemDialog dlg = new TextItemDialog(mExpandableListView.getContext());
        if (v.getTag() instanceof String) {
            final String path = (String) v.getTag();
            FileUtil.MediaFileType type = FileUtil.INSTANCE.getMediaFileTypeByName(path);
            if (type == FileUtil.MediaFileType.MOVIE) {
                dlg.setupItem(new String[]{mContext.getString(R.string.open), mContext.getString(R.string.cancel)});
                dlg.setOnClickListener(new Function1<Integer, Unit>() {
                    @Override
                    public Unit invoke(Integer integer) {
                        if (integer == 0) {
                            openFile(path);
                        } else if (integer == 1) {
                        }
                        dlg.cancel();
                        return null;
                    }
                });
            } else {
                dlg.setupItem(new String[]{mContext.getString(R.string.open), mContext.getString(R.string.open_by_others), mContext.getString(R.string.cancel)});
                dlg.setOnClickListener(new Function1<Integer, Unit>() {
                    @Override
                    public Unit invoke(Integer integer) {
                        if (integer == 0) {
                            Bundle bundle = WebViewFragment.openWithMIME(path);
                            Intent intent = ActionBarFragmentActivity.newInstance(mContext, WebViewFragment.class, bundle);
                            mContext.startActivity(intent);
                        } else if (integer == 1) {
                            openFile(path);
                        }
                        dlg.cancel();
                        return null;
                    }
                });
            }
            dlg.show();
        }
        return true;
    }


    public void onFoldFiles(LinkedHashMap<String, List<String>> foldFiles, String[] names) {
        if (names == null) return;
        List<FileExpandableInfo> newArr = new ArrayList<>();

        boolean selectAll = mSelectAll;
        mSelectAll = false;
        for (String name : names) {
            FileExpandableInfo vh = new FileExpandableInfo(name, foldFiles.get(name));
            if (selectAll) {
                vh.activate(true);
            }
            for (FileExpandableInfo last : mPropertyList) {
                if (last.getGroup().equals(vh.getGroup())) {
                    vh.activate(last.isActivated());
                    vh.setActivatedList(last.getActivatedList());
                }
            }
            newArr.add(vh);
        }

        mPropertyList = newArr;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return mPropertyList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mPropertyList.get(groupPosition).getChildList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mPropertyList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mPropertyList.get(groupPosition).getChildList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition; // getGroup(groupPosition).hashCode() 返回这个会导致，返回值改变后，notifyDataSetChanged会使Group折叠，内部因为对象改变了导致折叠
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getChild(groupPosition, childPosition).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        FileExpandableInfo vh = (FileExpandableInfo) getGroup(groupPosition);
        String f = vh.getGroup();
        String thumb = null;
        if (vh.getChildList().size() != 0)
            thumb = vh.getChildList().get(0);
        if (convertView == null)
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_file_group_item, parent, false);

        ((TextView) convertView.findViewById(R.id.text_name)).setText(FileUtil.INSTANCE.getFileName(f));
        FileUtil.MediaFileType type = mTabHolder.getType();

        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        icon.setImageDrawable(null);

        TextView text = (TextView) convertView.findViewById(R.id.text);
        text.setText("");

        setGroupViewThumb(type, thumb, icon, text);

        TextView child = (TextView) convertView.findViewById(R.id.select_all);
        child.setBackgroundResource(R.drawable.selector_file_group_item);
        child.setText("");
        child.setActivated(vh.isActivated());

        TextView fileCount = (TextView) convertView.findViewById(R.id.file_count);
        fileCount.setText(String.valueOf(vh.getChildList().size()));

        convertView.setTag(getGroup(groupPosition));
        child.setTag(getGroup(groupPosition));
        convertView.setOnClickListener(this);
        child.setOnClickListener(this);
        convertView.setOnLongClickListener(this);

        int activeSize = vh.getActivatedList().size();
        if (activeSize != 0 && activeSize != vh.getChildList().size()) {
            child.setBackgroundResource(R.mipmap.check_normal_pure);
            child.setText(String.valueOf(vh.getActivatedList().size()));
        }
        if (isExpanded) mExpandableListView.expandGroup(groupPosition);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String f = (String) getChild(groupPosition, childPosition);
        FileExpandableInfo vh = (FileExpandableInfo) getGroup(groupPosition);
        if (convertView == null)
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_file_item, parent, false);

        FileUtil.MediaFileType type = mTabHolder.getType();
        if (type == FileUtil.MediaFileType.APP && f.startsWith("/data/app") && mInstalledAppNames != null) {
            ((TextView) convertView.findViewById(R.id.text_name)).setText(mInstalledAppNames[childPosition]);
        } else {
            ((TextView) convertView.findViewById(R.id.text_name)).setText(FileUtil.INSTANCE.getFileName(f));
        }

        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        icon.setImageDrawable(null);

        setChildViewThumb(type, f, icon);

        CheckBox check = (CheckBox) convertView.findViewById(R.id.check_box);
        check.setChecked(vh.isItemActivated(f));
        check.setTag(f);
        check.setTag(R.id.extra_tag, vh);
        check.setOnClickListener(mCheckOnClickListener);

        convertView.setTag(f);
        convertView.setOnClickListener(this);
        convertView.setOnLongClickListener(this);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    protected void setGroupViewThumb(FileUtil.MediaFileType type, String thumb, ImageView icon, TextView text) {
        setGroupViewThumb(type, thumb, icon, text, null);
    }

    protected void setGroupViewThumb(FileUtil.MediaFileType type, String thumb, ImageView icon, TextView text, RequestOptions options) {
        if (type == FileUtil.MediaFileType.MOVIE ||
                type == FileUtil.MediaFileType.IMG) {
            if (options == null) {
                Glide.with(mContext).load(thumb).listener(mRequestListener).into(icon);
            } else {
                Glide.with(mContext).load(thumb).listener(mRequestListener).apply(options).into(icon);
            }
        } else if (type == FileUtil.MediaFileType.APP) {
            Bitmap b = sLruCache.get(thumb);
            if (b == null) {
                AppThumbTask task = new AppThumbTask(sLruCache, mContext, icon);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new File(thumb));
            } else
                icon.setImageBitmap(b);
        } else if (type == FileUtil.MediaFileType.MP3) {
            text.setText(R.string.music);
        } else if (type == FileUtil.MediaFileType.DOC) {
            text.setText(R.string.doc);
        } else if (type == FileUtil.MediaFileType.RAR) {
            text.setText(R.string.rar);
        }
    }

    protected void setChildViewThumb(FileUtil.MediaFileType type, String f, ImageView icon) {
        setChildViewThumb(type, f, icon, null);
    }

    protected void setChildViewThumb(FileUtil.MediaFileType type, String f, ImageView icon, RequestOptions options) {
        if (type == FileUtil.MediaFileType.MOVIE ||
                type == FileUtil.MediaFileType.IMG) {
            if (options == null) {
                Glide.with(mContext).load(f).listener(mRequestListener).into(icon);
            } else {
                Glide.with(mContext).load(f).listener(mRequestListener).apply(options).into(icon);
            }
        } else if (type == FileUtil.MediaFileType.APP) {
            Bitmap b = sLruCache.get(f);
            if (b == null) {
                AppThumbTask task = new AppThumbTask(sLruCache, mContext, icon);
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

    private RequestListener<Drawable> mRequestListener = new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            if (model instanceof String) {
                CacheUtil.makeCache((String) model, ImageUtil.drawable2Bitmap(resource), resource.getIntrinsicWidth(),
                        resource.getIntrinsicHeight(), mContext);
            }
            return false;
        }
    };

    class Worker extends Thread {

        @Override
        public void run() {

            final LinkedHashMap<String, List<String>> res = new LinkedHashMap<>();

            if (mTitle.equalsIgnoreCase("Apk")) {
                List<String> arrayList = new ArrayList<String>();
                List<PackageInfo> installedApps = FileUtil.INSTANCE.getInstalledApps(mContext, false);
                Collections.sort(installedApps, new Comparator<PackageInfo>() {
                    public int compare(PackageInfo lhs, PackageInfo rhs) {
                        if (lhs == null || rhs == null) {
                            return 0;
                        }
                        if (lhs.lastUpdateTime < rhs.lastUpdateTime) {
                            return 1;
                        } else if (lhs.lastUpdateTime > rhs.lastUpdateTime) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                for (PackageInfo packageInfo : installedApps) {
                    arrayList.add(packageInfo.applicationInfo.sourceDir);
                }
                res.put(mContext.getString(R.string.installed), arrayList);
                mInstalledAppNames = FileUtil.INSTANCE.getInstallAppsNameByPathArray(mContext, arrayList.toArray(new String[0]));
            }

            final String[] names = FileUtil.INSTANCE.foldFiles(mFileList, res);

            mExpandableListView.post(new Runnable() {
                @Override
                public void run() {
                    onFoldFiles(res, names);
                }
            });
            mWorker = null;
        }


    }

    protected void openFile(String path) {
        if (mTabHolder.getType() == FileUtil.MediaFileType.MOVIE) {
            Bundle bundle = new Bundle();
            bundle.putString(IjkVideoFragment.EXTRA_URI_PATH, path);
            Intent i = RotateNoCreateActivity.newInstance(mContext, IjkVideoFragment.class, bundle);
            mContext.startActivity(i);
        } else {
            Intent i = FileOpenIntentUtil.INSTANCE.openFile(path);
            try {
                mContext.startActivity(i);
            } catch (Exception ignore) {
            }
        }
    }

    private View.OnClickListener mCheckOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String file = (String) v.getTag();
            CheckBox checkBox = (CheckBox) v;
            FileExpandableInfo vh = (FileExpandableInfo) v.getTag(R.id.extra_tag);

            if (checkBox.isChecked()) {
                vh.activateItem(true, file);
            } else {
                vh.activateItem(false, file);
            }
            notifyDataSetChanged();
        }
    };

    public List<String> getSelectedFile() {
        List<String> files = new ArrayList<>();
        for (int i = 0; i < mPropertyList.size(); i++) {
            FileExpandableInfo vh = mPropertyList.get(i);
            files.addAll(vh.getActivatedList());
        }
        return files;
    }

    public void setup(String title) {
        mTitle = title;
    }

    public void selectAll(boolean select) {
        mSelectAll = select;
        for (FileExpandableInfo vh : mPropertyList) {
            vh.activate(select);
        }
        notifyDataSetChanged();
    }

    public void replaceVhList(List<FileExpandableInfo> vhList) {
        mPropertyList = vhList;
    }

    public List<FileExpandableInfo> getPropertyList() {
        return mPropertyList;
    }

    public String getTitle() {
        return mTitle;
    }

    public Context getContext() {
        return mContext;
    }
}
