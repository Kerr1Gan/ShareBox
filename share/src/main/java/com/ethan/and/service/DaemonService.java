package com.ethan.and.service;

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

public class DaemonService extends Service {

    private static final String TAG = "DaemonService";

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected restart MainService");
            bindService(new Intent(DaemonService.this, MainService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        startService(new Intent(this, MainService.class));
        bindService(new Intent(this, MainService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return (IBinder) mAidl;
    }

    private IAidlInterface mAidl = new IAidlInterface.Stub() {

        @Override
        public void startService() throws RemoteException {
            Intent i = new Intent(getBaseContext(), MainService.class);
            getBaseContext().startService(i);
        }

        @Override
        public void stopService() throws RemoteException {
            Intent i = new Intent(getBaseContext(), MainService.class);
            getBaseContext().stopService(i);
        }

        @Override
        public boolean isServerAlive() throws RemoteException {
            return false;
        }

        @Override
        public String getIp() throws RemoteException {
            return null;
        }

        @Override
        public int getPort() throws RemoteException {
            return 0;
        }
    };

    @Override
    public void onTrimMemory(int level) {
        if (isProcessRunning(getBaseContext(), "com.ecjtu.sharebox:daemon")) {
            Log.e(TAG, "onTrimMemory restart DaemonService");
            Intent i = new Intent(getBaseContext(), DaemonService.class);
            getBaseContext().startService(i);
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        try {
            Log.e(TAG, "onDestroy");
            unbindService(mServiceConnection);
        } catch (Exception e) {
        }
        super.onDestroy();
    }
}
