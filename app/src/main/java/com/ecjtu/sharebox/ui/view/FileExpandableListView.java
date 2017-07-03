package com.ecjtu.sharebox.ui.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ecjtu.sharebox.R;
import com.ecjtu.sharebox.async.AppThumbTask;
import com.ecjtu.sharebox.ui.activity.BaseFragmentActivity;
import com.ecjtu.sharebox.ui.dialog.FileItemLongClickDialog;
import com.ecjtu.sharebox.ui.dialog.FilePickDialog;
import com.ecjtu.sharebox.ui.fragment.VideoPlayerFragment;
import com.ecjtu.sharebox.util.fileutils.FileOpenIntentUtil;
import com.ecjtu.sharebox.util.fileutils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import kotlin.jvm.functions.Function1;

/**
 * Created by KerriGan on 2017/6/13 0013.
 */

public class FileExpandableListView extends ExpandableListView implements View.OnClickListener,View.OnLongClickListener {

    public FileExpandableListView(Context context) {
        super(context);
    }

    public FileExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FileExpandableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private FilePickDialog.TabItemHolder mTabHolder;

    private List<File> mFileList;

    private ExpandableAdapter mAdapter=new ExpandableAdapter();

    private static final int CACHE_SIZE=5 * 1024 *1024;

    private static LruCache<String, Bitmap> sLruCache = new LruCache<String, Bitmap>(CACHE_SIZE) {
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    private List<VH> mVHList=new ArrayList<>();

    private Worker mWorker=null;

    public void initData(FilePickDialog.TabItemHolder holder){
        mTabHolder=holder;
        mFileList=mTabHolder.getFileList();

        setGroupIndicator(null);
        setDivider(new ColorDrawable(Color.TRANSPARENT));
        setChildDivider(new ColorDrawable(Color.DKGRAY));
        setDividerHeight(1);

        setAdapter(mAdapter);
    }

    public void loadedData(){
        mFileList=mTabHolder.getFileList();
        ((BaseExpandableListAdapter)getExpandableListAdapter()).notifyDataSetChanged();
        if(mWorker==null&&mFileList!=null){
            mWorker=new Worker();
            mWorker.start();
        }
    }

    @Override
    public void onClick(View v) {
        if (!(v.getTag() instanceof VH)) return;
        VH vh = (VH) v.getTag();
        int position = mVHList.indexOf(vh);
        int id=v.getId();

        if(id==R.id.select_all){
            vh.isActivated=!vh.isActivated;
            mAdapter.notifyDataSetChanged();
        }else{
            boolean isExpand = isGroupExpanded(position);
            if (isExpand)
                collapseGroup(position);
            else
                expandGroup(position, true);
        }
    }

    @Override
    public boolean onLongClick(final View v) {

        final FileItemLongClickDialog dlg=new FileItemLongClickDialog(getContext());
        dlg.setOnClickListener(new Function1<Integer, Void>() {
            @Override
            public Void invoke(Integer integer) {
                if(integer==R.id.open){
                    String path=((File)v.getTag()).getAbsolutePath();
                    if(mTabHolder.getType()== FileUtil.MediaFileType.MOVIE){
                        Bundle bundle=new Bundle();
                        bundle.putString(VideoPlayerFragment.Companion.getEXTRA_URI_PATH(),path);
                        Intent i=BaseFragmentActivity.Companion.newInstance(getContext(), VideoPlayerFragment.class,bundle);
                        getContext().startActivity(i);
                    }else{
                        Intent i=FileOpenIntentUtil.INSTANCE.openFile(path);
                        try {
                            getContext().startActivity(i);
                        }catch (Exception ignore){
                        }
                    }
                    dlg.cancel();
                }else{
                    dlg.cancel();
                }
                return null;
            }
        });
        dlg.show();
        return true;
    }


    public void onFoldFiles(LinkedHashMap<String,List<File>> foldFiles,String[] names){
        mVHList.clear();

        for(String name:names){
            VH vh=new VH(new File(name),foldFiles.get(name));
            mVHList.add(vh);
        }

        mAdapter.notifyDataSetChanged();
    }


    public class ExpandableAdapter extends BaseExpandableListAdapter{

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
            return getChild(groupPosition,childPosition).hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            VH vh= (VH) getGroup(groupPosition);
            File f= (File) vh.group;
            File thumb=null;
            if(vh.childList.size()!=0)
                thumb=vh.childList.get(0);
            if(convertView==null)
                convertView=LayoutInflater.from(getContext()).inflate(R.layout.layout_file_group_item,parent,false);

            ((TextView)convertView.findViewById(R.id.text_name)).setText(f.getName());
            FileUtil.MediaFileType type=mTabHolder.getType();

            ImageView icon=(ImageView) convertView.findViewById(R.id.icon);
            icon.setImageDrawable(null);
            if(type == FileUtil.MediaFileType.MOVIE ||
                    type== FileUtil.MediaFileType.IMG){
                Glide.with(getContext()).load(thumb.getAbsolutePath()).into(icon);
            }else if(type== FileUtil.MediaFileType.APP){
                Bitmap b= sLruCache.get(thumb.getAbsolutePath());
                if(b==null){
                    AppThumbTask task= new AppThumbTask(sLruCache,getContext(),icon);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,thumb);
                }else
                    icon.setImageBitmap(b);
            }else if(type== FileUtil.MediaFileType.MP3){
                icon.setImageResource(R.mipmap.music);
            }else if(type== FileUtil.MediaFileType.DOC){
                icon.setImageResource(R.mipmap.document);
            }else if(type== FileUtil.MediaFileType.RAR){
                icon.setImageResource(R.mipmap.rar);
            }

            View child=convertView.findViewById(R.id.select_all);
            child.setActivated(vh.isActivated);

            TextView fileCount= (TextView) convertView.findViewById(R.id.file_count);
            fileCount.setText(""+vh.childList.size());

            convertView.setTag(getGroup(groupPosition));
            child.setTag(getGroup(groupPosition));
            convertView.setOnClickListener(FileExpandableListView.this);
            child.setOnClickListener(FileExpandableListView.this);
            convertView.setOnLongClickListener(FileExpandableListView.this);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            File f= (File) getChild(groupPosition,childPosition);
            VH vh= (VH) getGroup(groupPosition);
            if(convertView==null)
                convertView=LayoutInflater.from(getContext()).inflate(R.layout.layout_file_item,parent,false);

            ((TextView)convertView.findViewById(R.id.text_name)).setText(f.getName());
            FileUtil.MediaFileType type=mTabHolder.getType();

            ImageView icon=(ImageView) convertView.findViewById(R.id.icon);
            icon.setImageDrawable(null);
            if(type == FileUtil.MediaFileType.MOVIE ||
                    type== FileUtil.MediaFileType.IMG){
                Glide.with(getContext()).load(f.getAbsolutePath()).into(icon);
            }else if(type== FileUtil.MediaFileType.APP){
                Bitmap b= sLruCache.get(f.getAbsolutePath());
                if(b==null){
                    AppThumbTask task= new AppThumbTask(sLruCache,getContext(),icon);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,f);
                }else
                    icon.setImageBitmap(b);
            }else if(type== FileUtil.MediaFileType.MP3){
                icon.setImageResource(R.mipmap.music);
            }else if(type== FileUtil.MediaFileType.DOC){
                icon.setImageResource(R.mipmap.document);
            }else if(type== FileUtil.MediaFileType.RAR){
                icon.setImageResource(R.mipmap.rar);
            }


            CheckBox check= (CheckBox) convertView.findViewById(R.id.check_box);
            check.setChecked(vh.isActivated);

            convertView.setTag(f);
            convertView.setOnClickListener(FileExpandableListView.this);
            convertView.setOnLongClickListener(FileExpandableListView.this);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }


    public static class VH{

        public List<File> childList;

        public File group;

        public boolean isActivated=false;

        public VH(File group,List<File> childList){
            this.group=group;
            this.childList=childList;
        }
    }

    class Worker extends Thread{

        @Override
        public void run() {

            final LinkedHashMap<String,List<File>> res=new LinkedHashMap<>();
            boolean isFast=false;
            if(mTabHolder.getType()== FileUtil.MediaFileType.IMG){
                isFast=true;
            }
            final String[] names=FileUtil.INSTANCE.foldFiles(mFileList,res,isFast);


            FileExpandableListView.this.post(new Runnable() {
                @Override
                public void run() {
                    onFoldFiles(res,names);
                }
            });
            mWorker=null;
        }
    }

}
