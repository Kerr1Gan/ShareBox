package com.ecjtu.sharebox.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

import com.ecjtu.sharebox.ui.adapter.FileExpandableAdapter;
import com.ecjtu.sharebox.ui.dialog.FilePickDialog;

import java.io.File;
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

    private FileExpandableAdapter mAdapter=new FileExpandableAdapter(this);

    public void initData(FilePickDialog.TabItemHolder holder){
        mAdapter.initData(holder);
    }

    public void loadedData(){
        mAdapter.loadedData();
    }

}
