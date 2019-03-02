package com.ethan.and.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.ecjtu.channellibrary.udphelper.FindDeviceManager;

/**
 * Created by KerriGan on 2017/6/18.
 */

public class MainService extends Service {

    private static final String TAG = "MainService";

    private FindDeviceManager mFindDeviceManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MainServiceBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class MainServiceBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    public void createHelper(String name, int port, String icon) {
        if (mFindDeviceManager != null) {
            stopSearch();
        }
        mFindDeviceManager = new FindDeviceManager((name + "," + port + "," + icon).getBytes());
    }

    public void startSearch() {
        startSearch(false);
    }

    public void startSearch(boolean hidden) {
        if (mFindDeviceManager != null) {
            mFindDeviceManager.hide(hidden);
            mFindDeviceManager.start();
        }
    }

    public void stopSearch() {
        if (mFindDeviceManager != null) {
            mFindDeviceManager.stop();
            setMessageListener(null);
        }
    }

    public void setMessageListener(FindDeviceManager.IReceiveMsg listener) {
        if (mFindDeviceManager != null)
            mFindDeviceManager.setReceiveListener(listener);
    }

}
