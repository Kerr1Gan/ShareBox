//package org.ecjtu.easyserver.mobile.easyserver.service;
//
//import android.app.Service;
//import android.content.Intent;
//import android.os.IBinder;
//
//import org.ecjtu.easyserver.http.HTTPServerList;
//
///**
// * Created by KerriGan on 2016/4/ico_setting_normal.
// */
//public class HttpFileService extends Service {
//    private HttpFileServer _httpFileServer = null;
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        _httpFileServer = new HttpFileServer();
//        _httpFileServer.start();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//
//        HTTPServerList httpServerList = _httpFileServer.getHttpServerList();
//        httpServerList.stop();
//        httpServerList.close();
//        httpServerList.clear();
//        _httpFileServer.interrupt();
//
//    }
//}