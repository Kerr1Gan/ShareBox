package com.ecjtu.sharebox.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ecjtu.sharebox.MainApplication;
import com.ecjtu.sharebox.R;
import com.ecjtu.sharebox.async.AppThumbTask;
import com.ecjtu.sharebox.ui.activity.BaseFragmentActivity;
import com.ecjtu.sharebox.ui.activity.ImmersiveFragmentActivity;
import com.ecjtu.sharebox.ui.dialog.TextItemDialog;
import com.ecjtu.sharebox.ui.dialog.FilePickDialog;
import com.ecjtu.sharebox.ui.fragment.VideoPlayerFragment;
import com.ecjtu.sharebox.ui.view.FileExpandableListView;
import com.ecjtu.sharebox.util.file.FileOpenIntentUtil;
import com.ecjtu.sharebox.util.file.FileUtil;

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

    protected List<File> mFileList;

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

    private static final String EXTRA_VH_LIST = "FileExpandableAdapter_extra_vh_list";

    private Activity mActivity;

    private String mTitle = "";

    private boolean mSelectAll = false;

    public FileExpandableAdapter(FileExpandableListView expandableListView) {
        mExpandableListView = expandableListView;
        mContext = expandableListView.getContext();
    }

    public void initData(FilePickDialog.TabItemHolder holder) {
        mTabHolder = holder;
        mFileList = mTabHolder.getFileList();

        mExpandableListView.setGroupIndicator(null);
        mExpandableListView.setDivider(new ColorDrawable(Color.TRANSPARENT));
        mExpandableListView.setChildDivider(new ColorDrawable(Color.DKGRAY));
        mExpandableListView.setDividerHeight(1);

        if (mActivity != null) {
            List<VH> vhList = readCache((MainApplication) mActivity.getApplication());
            if (vhList != null) {
                mVHList = vhList;
            }
        }
        mExpandableListView.setAdapter(this);
    }

    public List<VH> readCache(MainApplication application){
        List<VH> vhList = (List<VH>) application.getSavedInstance().get(EXTRA_VH_LIST + mTitle);
        return vhList;
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
        dlg.setOnClickListener(new Function1<Integer, Unit>() {
            @Override
            public Unit invoke(Integer integer) {
                if (integer == 0) {
                    String path = ((File) v.getTag()).getAbsolutePath();
                    openFile(path);
                    dlg.cancel();
                } else {
                    dlg.cancel();
                }
                return null;
            }
        });
        dlg.setupItem(new String[]{"打开", "取消"});
        dlg.show();
        return true;
    }


    public void onFoldFiles(LinkedHashMap<String, List<File>> foldFiles, String[] names) {
        if (names == null) return;
        List<VH> newArr = new ArrayList<>();

        boolean selectAll=mSelectAll;
        mSelectAll=false;
        for (String name : names) {
            VH vh = new VH(new File(name), foldFiles.get(name));
            if(selectAll){
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
        if (mActivity != null) {
            MainApplication application = (MainApplication) mActivity.getApplication();
            application.getSavedInstance().put(EXTRA_VH_LIST + mTitle, newArr);
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
        return getGroup(groupPosition).hashCode();
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
        File f = (File) vh.group;
        File thumb = null;
        if (vh.childList.size() != 0)
            thumb = vh.childList.get(0);
        if (convertView == null)
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_file_group_item, parent, false);

        ((TextView) convertView.findViewById(R.id.text_name)).setText(f.getName());
        FileUtil.MediaFileType type = mTabHolder.getType();

        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        icon.setImageDrawable(null);

        TextView text = (TextView) convertView.findViewById(R.id.text);
        text.setText("");
        if (type == FileUtil.MediaFileType.MOVIE ||
                type == FileUtil.MediaFileType.IMG) {
            Glide.with(mContext).load(thumb.getAbsolutePath()).into(icon);
        } else if (type == FileUtil.MediaFileType.APP) {
            Bitmap b = sLruCache.get(thumb.getAbsolutePath());
            if (b == null) {
                AppThumbTask task = new AppThumbTask(sLruCache, mContext, icon);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, thumb);
            } else
                icon.setImageBitmap(b);
        } else if (type == FileUtil.MediaFileType.MP3) {
            text.setText(R.string.music);
        } else if (type == FileUtil.MediaFileType.DOC) {
            text.setText(R.string.doc);
        } else if (type == FileUtil.MediaFileType.RAR) {
            text.setText(R.string.rar);
        }

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
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        File f = (File) getChild(groupPosition, childPosition);
        VH vh = (VH) getGroup(groupPosition);
        if (convertView == null)
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_file_item, parent, false);

        ((TextView) convertView.findViewById(R.id.text_name)).setText(f.getName());
        FileUtil.MediaFileType type = mTabHolder.getType();

        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        icon.setImageDrawable(null);
        if (type == FileUtil.MediaFileType.MOVIE ||
                type == FileUtil.MediaFileType.IMG) {
            Glide.with(mContext).load(f.getAbsolutePath()).into(icon);
        } else if (type == FileUtil.MediaFileType.APP) {
            Bitmap b = sLruCache.get(f.getAbsolutePath());
            if (b == null) {
                AppThumbTask task = new AppThumbTask(sLruCache, mContext, icon);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, f);
            } else
                icon.setImageBitmap(b);
        } else if (type == FileUtil.MediaFileType.MP3) {
            icon.setImageResource(R.mipmap.music);
        } else if (type == FileUtil.MediaFileType.DOC) {
            icon.setImageResource(R.mipmap.document);
        } else if (type == FileUtil.MediaFileType.RAR) {
            icon.setImageResource(R.mipmap.rar);
        }

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

    private static class VH {

        public List<File> childList;

        public File group;

        private boolean isActivated = false;

        private List<File> activatedList = new ArrayList<>();

        public VH(File group, List<File> childList) {
            this.group = group;
            this.childList = childList;
        }

        public boolean isActivated() {
            return isActivated;
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

        public void activateItem(boolean active, File file) {
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

        public boolean isItemActivated(File file) {
            return activatedList.indexOf(file) >= 0;
        }

        public List<File> getActivatedList() {
            return activatedList;
        }
    }

    class Worker extends Thread {

        @Override
        public void run() {

            final LinkedHashMap<String, List<File>> res = new LinkedHashMap<>();
            boolean isFast = false;
            if (mTabHolder.getType() == FileUtil.MediaFileType.IMG) {
                isFast = true;
            }
            final String[] names = FileUtil.INSTANCE.foldFiles(mFileList, res, isFast);


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
            bundle.putString(VideoPlayerFragment.Companion.getEXTRA_URI_PATH(), path);
            Intent i = ImmersiveFragmentActivity.Companion.newInstance(mContext, VideoPlayerFragment.class, bundle);
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
            File file = (File) v.getTag();
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

    public List<File> getSelectedFile() {
        List<File> files = new ArrayList<>();
        for (int i = 0; i < mVHList.size(); i++) {
            VH vh = mVHList.get(i);
            files.addAll(vh.getActivatedList());
        }
        return files;
    }

    public void setup(Activity activity, String title) {
        mActivity = activity;
        mTitle = title;
    }

    public void selectAll(boolean select) {
        mSelectAll=select;
        for (VH vh : mVHList) {
            vh.activate(select);
        }
        notifyDataSetChanged();
    }
}
