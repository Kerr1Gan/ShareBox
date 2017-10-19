package com.ecjtu.sharebox.ui.adapter;

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
import com.ecjtu.sharebox.async.AppThumbTask;
import com.ecjtu.sharebox.ui.dialog.FilePickDialog;
import com.ecjtu.sharebox.ui.dialog.TextItemDialog;
import com.ecjtu.sharebox.ui.fragment.IjkVideoFragment;
import com.ecjtu.sharebox.ui.fragment.WebViewFragment;
import com.ecjtu.sharebox.ui.widget.FileExpandableListView;
import com.ecjtu.sharebox.util.cache.CacheUtil;
import com.ecjtu.sharebox.util.file.FileOpenIntentUtil;
import com.ecjtu.sharebox.util.file.FileUtil;
import com.ecjtu.sharebox.util.image.ImageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Created by Ethan_Xiang on 2017/7/10.
 */

public class FileExpandableAdapter extends BaseExpandableListAdapter implements View.OnClickListener,
        View.OnLongClickListener {

    protected FilePickDialog.TabItemHolder mTabHolder;

    protected List<String> mFileList;

    private static final int CACHE_SIZE = 5 * 1024 * 1024;

    private static LruCache<String, Bitmap> sLruCache = new LruCache<String, Bitmap>(CACHE_SIZE) {
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    private List<VH> mVHList = new ArrayList<>();

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

    public void initData(FilePickDialog.TabItemHolder holder, List<VH> oldCache) {
        mTabHolder = holder;
        mFileList = mTabHolder.getFileList();

        mExpandableListView.setGroupIndicator(null);
        mExpandableListView.setDivider(new ColorDrawable(Color.TRANSPARENT));
        mExpandableListView.setChildDivider(new ColorDrawable(Color.DKGRAY));
        mExpandableListView.setDividerHeight(1);

        if (oldCache != null) {
            mVHList = oldCache;
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
        if (!(v.getTag() instanceof VH)) return;
        VH vh = (VH) v.getTag();
        int position = mVHList.indexOf(vh);
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
        return true;
    }


    public void onFoldFiles(LinkedHashMap<String, List<String>> foldFiles, String[] names) {
        if (names == null) return;
        List<VH> newArr = new ArrayList<>();

        boolean selectAll = mSelectAll;
        mSelectAll = false;
        for (String name : names) {
            VH vh = new VH(name, foldFiles.get(name));
            if (selectAll) {
                vh.activate(true);
            }
            for (VH last : mVHList) {
                if (last.group.equals(vh.group)) {
                    vh.activate(last.isActivated());
                    vh.activatedList = last.activatedList;
                }
            }
            newArr.add(vh);
        }

        mVHList = newArr;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return mVHList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mVHList.get(groupPosition).childList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mVHList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mVHList.get(groupPosition).childList.get(childPosition);
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
        VH vh = (VH) getGroup(groupPosition);
        String f = vh.group;
        String thumb = null;
        if (vh.childList.size() != 0)
            thumb = vh.childList.get(0);
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
        fileCount.setText(String.valueOf(vh.childList.size()));

        convertView.setTag(getGroup(groupPosition));
        child.setTag(getGroup(groupPosition));
        convertView.setOnClickListener(this);
        child.setOnClickListener(this);
        convertView.setOnLongClickListener(this);

        int activeSize = vh.activatedList.size();
        if (activeSize != 0 && activeSize != vh.childList.size()) {
            child.setBackgroundResource(R.mipmap.check_normal_pure);
            child.setText(String.valueOf(vh.activatedList.size()));
        }
        if (isExpanded) mExpandableListView.expandGroup(groupPosition);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String f = (String) getChild(groupPosition, childPosition);
        VH vh = (VH) getGroup(groupPosition);
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

    public static class VH implements Cloneable {

        public List<String> childList;

        public String group;

        private boolean isActivated = false;

        private List<String> activatedList = new ArrayList<>();

        public VH(String group, List<String> childList) {
            this.group = group;
            this.childList = childList;
        }

        public boolean isActivated() {
            return isActivated;
        }

        public void setActivated(boolean activated) {
            isActivated = activated;
        }

        public void activate(boolean active) {
            isActivated = active;
            if (isActivated) {
                activatedList.clear();
                activatedList.addAll(childList);
            } else {
                activatedList.clear();
            }
        }

        public void activateItem(boolean active, String file) {
            if (active) {
                if (activatedList.indexOf(file) < 0) {
                    activatedList.add(file);
                }
                if (activatedList.size() == childList.size()) {
                    isActivated = true;
                }
            } else {
                activatedList.remove(file);
                if (activatedList.size() != childList.size()) {
                    isActivated = false;
                }
            }
        }

        public boolean isItemActivated(String file) {
            return activatedList.indexOf(file) >= 0;
        }

        public List<String> getActivatedList() {
            return activatedList;
        }

        public void setActivatedList(List<String> list) {
            activatedList = list;
        }

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }
    }

    class Worker extends Thread {

        @Override
        public void run() {

            final LinkedHashMap<String, List<String>> res = new LinkedHashMap<>();

            if (mTitle.equalsIgnoreCase("Apk")) {
                List<String> arrayList = new ArrayList<String>();
                List<PackageInfo> installedApps = FileUtil.INSTANCE.getInstalledApps(mContext, false);
                for (PackageInfo packageInfo : installedApps) {
                    arrayList.add(packageInfo.applicationInfo.sourceDir);
                }
                res.put("已安装", arrayList);
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
            VH vh = (VH) v.getTag(R.id.extra_tag);

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
        for (int i = 0; i < mVHList.size(); i++) {
            VH vh = mVHList.get(i);
            files.addAll(vh.getActivatedList());
        }
        return files;
    }

    public void setup(String title) {
        mTitle = title;
    }

    public void selectAll(boolean select) {
        mSelectAll = select;
        for (VH vh : mVHList) {
            vh.activate(select);
        }
        notifyDataSetChanged();
    }

    public void replaceVhList(List<VH> vhList) {
        mVHList = vhList;
    }

    public List<VH> getVhList() {
        return mVHList;
    }

    public String getTitle() {
        return mTitle;
    }

    public Context getContext() {
        return mContext;
    }
}
