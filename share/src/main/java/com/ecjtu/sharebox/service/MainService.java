package com.ecjtu.sharebox.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.ecjtu.channellibrary.devicesearch.DiscoverHelper;

/**
 * Created by KerriGan on 2017/6/18.
 */

public class MainService extends Service {

    private static final String TAG = "MainService";

    private DiscoverHelper mDiscoverHelper;

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
        if (mDiscoverHelper != null) {
            stopHelper(true, true);
        }
        mDiscoverHelper = new DiscoverHelper(this, name, String.valueOf(port), icon);
        mDiscoverHelper.updateTime(System.currentTimeMillis());
    }

    public void prepareAndStartHelper(boolean waiting, boolean search) {
        DiscoverHelper.IMessageListener listener = mDiscoverHelper.getMessageListener();
        stopHelper(waiting, search);
        setMessageListener(listener);
        prepareHelper(waiting, search);
        startHelper(waiting, search);
    }

    public void prepareHelper(boolean waiting, boolean search) {
        mDiscoverHelper.prepare(this, waiting, search);
    }

    public void startHelper(boolean waiting, boolean search) {
        mDiscoverHelper.start(waiting, search);
    }

    public void stopHelper(boolean waiting, boolean search) {
        mDiscoverHelper.stop(waiting, search);
        setMessageListener(null);
    }

    public void setMessageListener(DiscoverHelper.IMessageListener listener) {
        mDiscoverHelper.setMessageListener(listener);
    }

}
