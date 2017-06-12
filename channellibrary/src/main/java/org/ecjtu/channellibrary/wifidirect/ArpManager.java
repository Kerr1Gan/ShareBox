package org.ecjtu.channellibrary.wifidirect;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
public class ArpManager {

	private static final int MSG_SUCCESS=1;
	private static final int MSG_FAILURE=2;
	
	
	/**
	 *  IResult will run on ui thread.
	 * 
	 * */
	public static void getP2pIpFromArp(final Looper looper,final WifiDirectManager manager,
			final IResult result){
		
		Runnable runnable=new Runnable() {
			@Override
			public void run() {
				final Thread currThread=Thread.currentThread();

				final Handler handler=new Handler(looper){
					@Override
					public void handleMessage(Message msg) {
						super.handleMessage(msg);
						switch(msg.what){
						case MSG_SUCCESS:
							result.onSuccess((String)msg.obj);
							break;
						case MSG_FAILURE:
							result.onFailure();
							break;
						}
					}
					
				};
				
				Runnable cancelRun=new Runnable() {
					@Override
					public void run() {
						currThread.interrupt();	
					}
				};
				
				handler.postDelayed(cancelRun, 1000*20);
				
				String p2pIp=null;
				
				while(!Thread.interrupted()){
					try {
						p2pIp=manager.getLocalP2PIP();
						if(p2pIp!=null){
							handler.obtainMessage(MSG_SUCCESS,p2pIp).sendToTarget();
							handler.removeCallbacks(cancelRun);
							return;
						}
						Thread.sleep(100);
					} catch (InterruptedException e) {
						break;
					}
				}
				handler.obtainMessage(MSG_FAILURE).sendToTarget();
			}
		};
		new Thread(runnable).start();
	}
	
	
	
	public interface IResult{	
		public void onSuccess(String ip);
		public void onFailure();
	}
}
