package com.ecjtu.sharebox.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

import com.ecjtu.sharebox.ui.adapter.FileExpandableAdapter;
import com.ecjtu.sharebox.ui.holder.FileExpandableProperty;
import com.ecjtu.sharebox.ui.holder.TabItemHolder;

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

    public void initData(TabItemHolder holder, List<FileExpandableProperty> oldCache) {
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
