package org.ecjtu.easyserver.server;

import android.content.Context;


import java.io.File;
import java.util.List;

/**
 * Created by KerriGan on 2017/7/15.
 */

public class ServerManager {

    private ServerManager(){
    }

    private static ServerManager sInstance=null;

    private Context mApplicationContext;

    private List<File> mSharedFileList;

    private String mIp;

    private DeviceInfo mDeviceInfo;

    private String mIconPath;

    private static class Inner{
        private static final ServerManager INSTANCE=new ServerManager();
    }

    public static ServerManager getInstance(){
        return Inner.INSTANCE;
    }

    public ServerManager setApplicationContext(Context context){
        mApplicationContext=context;
        return this;
    }

    public ServerManager setSharedFileList(List<File> list){
        mSharedFileList=list;
        return this;
    }

    public ServerManager setIp(String ip){
        mIp=ip;
        return this;
    }

    public ServerManager setIconPath(String path){
        mIconPath=path;
        return this;
    }

    public ServerManager setDeviceInfo(DeviceInfo info){
        mDeviceInfo=info;
        return this;
    }

    public Context getApplicationContext(){
        return mApplicationContext;
    }

    public List<File> getSharedFileList(){
        return mSharedFileList;
    }

    public String getIp(){
        return mIp;
    }

    public String getIconPath(){
        return mIconPath;
    }

    public DeviceInfo getDeviceInfo(){
        return mDeviceInfo;
    }
}
