package com.ecjtu.sharebox.server.impl.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by KerriGan on 2016/4/24.
 */
public class EasyServerConnection implements ServiceConnection {

    private EasyServerService.EasyServerBinder mBinder;
    private Context mContext;

    public EasyServerConnection(Context context) {
        mContext = context;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        System.out.println("EasyServerConnection 成功连接");

        EasyServerService.EasyServerBinder binder = (EasyServerService.EasyServerBinder) service;
        binder.setHostContext(mContext);
        binder.setServerConnection(this);
        mBinder = binder;
        mBinder.getService().isBind = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        System.out.println("EasyServerConnection 断开连接");
        mBinder.getService().isBind = false;
        mBinder = null;
    }

    public EasyServerService.EasyServerBinder getBinder() {
        return mBinder;
    }

    public void unbindAndStopService() {
        try {
            mContext.unbindService(this);
        } catch (Exception e) {
        }

        mContext.stopService(new Intent(mContext, EasyServerService.class));
    }
}
