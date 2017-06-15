package com.ecjtu.sharebox.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ecjtu.sharebox.R;
import com.ecjtu.sharebox.ui.dialog.FilePickDialog;
import com.ecjtu.sharebox.utils.fileutils.FileUtil;

import java.io.File;
import java.util.List;

/**
 * Created by KerriGan on 2017/6/13 0013.
 */

public class FileExpandableListView extends ExpandableListView implements View.OnClickListener{

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

    private LruCache<String, Bitmap> mLruCache = new LruCache<String, Bitmap>(5*1024*1024) {
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    public void initData(FilePickDialog.TabItemHolder holder){
        mTabHolder=holder;
        mFileList=mTabHolder.getFileList();
        setAdapter(mAdapter);
        setGroupIndicator(null);
    }

    public void loadedData(){
        mFileList=mTabHolder.getFileList();
        ((BaseExpandableListAdapter)getExpandableListAdapter()).notifyDataSetInvalidated();
    }

    @Override
    public void onClick(View v) {

    }


    public class ExpandableAdapter extends BaseExpandableListAdapter{

        @Override
        public int getGroupCount() {
            if(mFileList==null) return 0;
            return mFileList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mFileList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return getGroup(groupPosition).hashCode();
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            File f= (File) getGroup(groupPosition);

            if(convertView==null)
                convertView=LayoutInflater.from(getContext()).inflate(R.layout.layout_file_item,parent,false);

            ((TextView)convertView.findViewById(R.id.text_name)).setText(f.getName());
            FileUtil.MediaFileType type=mTabHolder.getType();

            ImageView icon=(ImageView) convertView.findViewById(R.id.icon);

            if(type == FileUtil.MediaFileType.MOVIE ||
                    type== FileUtil.MediaFileType.IMG){
                Glide.with(getContext()).load(f.getAbsolutePath()).into(icon);
            }else if(type== FileUtil.MediaFileType.APP){
                Bitmap b= mLruCache.get(f.getAbsolutePath());
                if(b==null){
                    b=FileUtil.INSTANCE.getAppThumbnail(getContext(),f);
                    mLruCache.put(f.getAbsolutePath(),b);
                }
                icon.setImageBitmap(b);
            }

            convertView.setOnClickListener(FileExpandableListView.this);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            return null;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }


    }
}
