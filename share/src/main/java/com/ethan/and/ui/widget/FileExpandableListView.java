package com.ethan.and.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

import com.ethan.and.ui.adapter.FileExpandableAdapter;
import com.ethan.and.ui.holder.FileExpandableInfo;
import com.ethan.and.ui.holder.TabItemInfo;

import java.util.List;

/**
 * Created by KerriGan on 2017/6/13 0013.
 */

public class FileExpandableListView extends ExpandableListView {

    public FileExpandableListView(Context context) {
        super(context);
    }

    public FileExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FileExpandableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private FileExpandableAdapter mAdapter = getFileExpandableAdapter();

    public void initData(TabItemInfo holder, List<FileExpandableInfo> oldCache) {
        mAdapter.initData(holder, oldCache);
    }

    public void loadedData() {
        mAdapter.loadedData();
    }

    public void setFileExpandableAdapter(FileExpandableAdapter adapter) {
        mAdapter = adapter;
    }

    public FileExpandableAdapter getFileExpandableAdapter() {
        if (mAdapter == null) mAdapter = new FileExpandableAdapter(this);
        return mAdapter;
    }
}
