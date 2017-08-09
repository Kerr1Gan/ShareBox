package com.ecjtu.sharebox.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

/**
 * Created by KerriGan on 2017/6/18.
 */

public class MainService extends Service {

    private static final String TAG = "MainService";

    private boolean mDaemon = false;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected restart DaemonService");
            bindService(new Intent(MainService.this, DaemonService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return (IBinder) mAidl;
    }

    private IAidlInterface mAidl = new IAidlInterface.Stub() {

        @Override
        public void startService() throws RemoteException {
            Intent i = new Intent(getBaseContext(), DaemonService.class);
            getBaseContext().startService(i);
        }

        @Override
        public void stopService() throws RemoteException {
            Intent i = new Intent(getBaseContext(), DaemonService.class);
            getBaseContext().stopService(i);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        if (mDaemon) {
            startService(new Intent(this, DaemonService.class));
            bindService(new Intent(this, DaemonService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * 判断进程是否运行
     *
     * @return
     */
    public static boolean isProcessRunning(Context context, String processName) {

        boolean isRunning = false;
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> lists = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : lists) {
            if (info.processName.equals(processName)) {
                isRunning = true;
            }
        }

        return isRunning;
    }

    @Override
    public void onTrimMemory(int level) {
        if (isProcessRunning(getBaseContext(), "com.ecjtu.sharebox")) {
            Log.e(TAG, "onTrimMemory restart MainService");
            Intent i = new Intent(getBaseContext(), MainService.class);
            getBaseContext().startService(i);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            if (mDaemon)
                unbindService(mServiceConnection);
        } catch (Exception e) {
        }
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }
}
