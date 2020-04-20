package com.ethan.and.ui.sendby;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.common.componentes.activity.ImmersiveFragmentActivity;
import com.common.utils.file.FileUtil;
import com.ethan.and.ui.holder.TabItemInfo;
import com.flybd.sharebox.R;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileChooseFragment extends BottomSheetFragment {

    private TabLayout tabLayout;

    private ViewPager viewPager;

    private Map<String, TabItemInfo> tabItemHolders = new LinkedHashMap<>();

    private SparseArrayCompat<FileListFragment> cachedFragment = new SparseArrayCompat<>();

    private TextView tvSend;

    private TextView tvClear;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file_choose, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        tvSend = view.findViewById(R.id.tv_send);
        tvClear = view.findViewById(R.id.tv_clear);
        tvClear.setVisibility(View.VISIBLE);

        tvClear.setOnClickListener(v -> {
            for (int i = 0; i < cachedFragment.size(); i++) {
                cachedFragment.valueAt(i).clearSelected();
            }
        });

        tvSend.setOnClickListener(v -> sendFiles());


        initData();
        viewPager.setAdapter(new SimplePagerAdapter(getChildFragmentManager()));
        viewPager.setOnPageChangeListener(new SimplePageListener());
        tabLayout.setupWithViewPager(viewPager);
    }

    private void initData() {
        TabItemInfo info = new TabItemInfo(getString(R.string.apk), FileUtil.INSTANCE.string2MediaFileType("Apk"), null, null);
        tabItemHolders.put("Apk", info);

        info = new TabItemInfo(getString(R.string.movie), FileUtil.INSTANCE.string2MediaFileType("Movie"), null, null);
        tabItemHolders.put("Movie", info);

        info = new TabItemInfo(getString(R.string.music), FileUtil.INSTANCE.string2MediaFileType("Music"), null, null);
        tabItemHolders.put("Music", info);

        info = new TabItemInfo(getString(R.string.photo), FileUtil.INSTANCE.string2MediaFileType("Photo"), null, null);
        tabItemHolders.put("Photo", info);

        info = new TabItemInfo(getString(R.string.doc), FileUtil.INSTANCE.string2MediaFileType("Doc"), null, null);
        tabItemHolders.put("Doc", info);

        info = new TabItemInfo(getString(R.string.archive), FileUtil.INSTANCE.string2MediaFileType("Rar"), null, null);
        tabItemHolders.put("Archive", info);
    }

    public void sendFiles() {
        List<File> selectedFiles = new ArrayList<>();
        for (int i = 0; i < cachedFragment.size(); i++) {
            FileListFragment fragment = cachedFragment.get(i);
            if (fragment != null) {
                List<File> select = fragment.getSelectedFiles();
                if (select != null) {
                    selectedFiles.addAll(select);
                }
            }
        }
        Context ctx = getContext();
        if (ctx != null) {
            Intent i = ImmersiveFragmentActivity.newInstance(getContext(), SendFileFragment.class, SendFileFragment.getBundle(selectedFiles));
            startActivity(i);
        }
    }

    @Override
    public void onUserVisibleHintChanged(boolean isVisibleToUser) {
        super.onUserVisibleHintChanged(isVisibleToUser);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public boolean isExpandable() {
        return getBehavior().getState() == SimpleBottomSheetBehavior.STATE_EXPANDED;
    }

    public void collapse() {
        getBehavior().setState(SimpleBottomSheetBehavior.STATE_COLLAPSED);
    }

    class SimplePagerAdapter extends FragmentPagerAdapter {

        public SimplePagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return tabItemHolders.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            String[] keySet = tabItemHolders.keySet().toArray(new String[0]);
            return keySet[position];
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            String title = String.valueOf(getPageTitle(position));
            TabItemInfo info = tabItemHolders.get(title);
            FileListFragment cache = cachedFragment.get(position);
            if (cache == null) {
                FileListFragment newObj = FileListFragment.newInstance(info.getType());
                cachedFragment.put(position, newObj);
                return newObj;
            }
            return cache;
        }
    }

    class SimplePageListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}
