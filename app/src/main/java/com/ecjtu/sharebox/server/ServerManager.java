package com.ecjtu.sharebox.server;

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

    private static class Inner{
        private static final ServerManager INSTANCE=new ServerManager();
    }

    public static ServerManager getInstance(){
        return Inner.INSTANCE;
    }

    public void setApplicationContext(Context context){
        mApplicationContext=context;
    }

    public void setSharedFileList(List<File> list){
        mSharedFileList=list;
    }

    public void setIp(String ip){
        mIp=ip;
    }

}
