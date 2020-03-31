package com.ethan.and.ui.sendby;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import com.common.componentes.fragment.LazyInitFragment;
import com.common.utils.activity.ActivityUtil;
import com.flybd.sharebox.R;

public class BottomSheetFragment extends LazyInitFragment {

    private SimpleBottomSheetBehavior behavior;

//    View.OnLayoutChangeListener layoutListener = new View.OnLayoutChangeListener() {
//        @Override
//        public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
//            if (header.getHeight() > 0) {
//                behavior.setPeekHeight(header.getHeight() + HEADER_OFFSET_HEIGHT);
//                header.removeOnLayoutChangeListener(layoutListener);
//            }
////            if (header.getHeight() <= 0) {
////                header.post(() -> behavior.setPeekHeight(header.getHeight() + HEADER_OFFSET_HEIGHT));
////            } else {
////            }
//        }
//    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void wrapInBottomSheet(View view) {
        boolean cancelable = true;
        CoordinatorLayout coordinator = view.getRootView().findViewById(R.id.coordinator);
        FrameLayout bottomSheet = coordinator.findViewById(R.id.design_bottom_sheet);
        SimpleBottomSheetBehavior<FrameLayout> sheetBehavior = SimpleBottomSheetBehavior.from(bottomSheet);
        sheetBehavior.setBottomSheetCallback(bottomSheetCallback);
        sheetBehavior.setHideable(true);

        // Handle accessibility events
        ViewCompat.setAccessibilityDelegate(
                bottomSheet,
                new AccessibilityDelegateCompat() {
                    @Override
                    public void onInitializeAccessibilityNodeInfo(
                            View host, AccessibilityNodeInfoCompat info) {
                        super.onInitializeAccessibilityNodeInfo(host, info);
                        if (cancelable) {
                            info.addAction(AccessibilityNodeInfoCompat.ACTION_DISMISS);
                            info.setDismissable(true);
                        } else {
                            info.setDismissable(false);
                        }
                    }

                    @Override
                    public boolean performAccessibilityAction(View host, int action, Bundle args) {
                        if (action == AccessibilityNodeInfoCompat.ACTION_DISMISS && cancelable) {
                            //cancel();
                            return true;
                        }
                        return super.performAccessibilityAction(host, action, args);
                    }
                });
        bottomSheet.setOnTouchListener(
                (v, event) -> {
                    // Consume the event and prevent it from falling through
                    return true;
                });
    }

    private SimpleBottomSheetBehavior.BottomSheetCallback bottomSheetCallback =
            new SimpleBottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(
                        @NonNull View bottomSheet, @SimpleBottomSheetBehavior.State int newState) {
                    if (newState == SimpleBottomSheetBehavior.STATE_HIDDEN) {
                        //cancel();
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    //Log.i(TAG, "onSlide: " + slideOffset);
                }
            };

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View bottomSheet = view.getRootView().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        Window window = getActivity().getWindow();
        Display display = window.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight() + ActivityUtil.getStatusBarHeight(view.getContext());
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null) {
            params.width = width;
            params.height = height;
        } else {
            params = new ViewGroup.LayoutParams(width, height);
        }
        view.setLayoutParams(params);
        wrapInBottomSheet(view);
        if (bottomSheet != null) {
            behavior = SimpleBottomSheetBehavior.from(bottomSheet);
            behavior.setState(SimpleBottomSheetBehavior.STATE_COLLAPSED);
            behavior.setHideable(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public SimpleBottomSheetBehavior getBehavior() {
        return behavior;
    }
}
