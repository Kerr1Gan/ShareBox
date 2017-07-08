//package org.ecjtu.easyserver.mobile.easyserver.service;
//
//
//import android.app.Service;
//import android.content.Intent;
//import android.os.IBinder;
//
//import org.ecjtu.easyserver.http.HTTPServerList;
//
//public class PlayFileService extends Service
//{
//
//	private FileServer fileServer = null;
//
//	@Override
//	public IBinder onBind(Intent intent)
//	{
//		return null;
//	}
//
//	@Override
//	public void onCreate()
//	{
//		super.onCreate();
//		fileServer = new FileServer();
//		fileServer.start();
//	}
//
//	@Override
//	public void onDestroy()
//	{
//		super.onDestroy();
//
//		HTTPServerList httpServerList = fileServer.getHttpServerList();
//		httpServerList.stop();
//		httpServerList.close();
//		httpServerList.clear();
//		fileServer.interrupt();
//
//	}
//
//}
