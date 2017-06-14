package com.newindia.sharebox.ui.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.newindia.sharebox.R;
import com.newindia.sharebox.ui.dialog.FilePickDialog;

import java.io.File;
import java.util.List;

/**
 * Created by KerriGan on 2017/6/13 0013.
 */

public class FileExpandableListView extends ExpandableListView{

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

    public void initData(FilePickDialog.TabItemHolder holder){
        mTabHolder=holder;
        mFileList=mTabHolder.getFileList();
        setAdapter(mAdapter);
    }

    public void loadedData(){
        mFileList=mTabHolder.getFileList();
        ((BaseExpandableListAdapter)getExpandableListAdapter()).notifyDataSetInvalidated();
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
            View v=LayoutInflater.from(getContext()).inflate(R.layout.layout_file_item,parent,false);
            ((TextView)v.findViewById(R.id.text_name)).setText(f.getName());
            return v;
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
