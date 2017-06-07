package org.ecjtu.channellibrary.wifidirect;



import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import org.ecjtu.channellibrary.wifidirect.util.ALog;
import org.ecjtu.channellibrary.wifidirect.util.Utilities;


/**
 *  服务多次启动能获得最大的搜索成功率和广播接收率
 * */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class WifiDirectService extends Service {

	private static final String TAG = "WifiDirectService";
	
    private WakeLock mWakeLock = null;
	private PowerManager mPM = null;
    
	private boolean mIsServiceInit = false;
	
    private WifiDirectManager mWifiDirectManager;
    
    private WifiDirectBinder mBinder;
    
    private static final int MSG_DISCOVER_PEER=1;
    private static final int MSG_DISCOVER_STOP=2;
    
    private volatile boolean mStopDiscover=false;
    
    private boolean mIsWifiP2pConnected=false;
    
    private boolean mIsWifiP2pSearching=false;
    
	protected Handler mServiceHandler = new Handler() {
		
		@Override
        public void handleMessage(Message msg) {
//			ALog.i(TAG, "ServiceHandler msg what:" + msg.what); 
			switch (msg.what) {
			case MSG_DISCOVER_PEER:
				if(!mStopDiscover){
					ALog.i(TAG, "ServiceHandler 尝试探索");
					mIsWifiP2pSearching=true;
					discover();
				}else{
					mServiceHandler.removeMessages(MSG_DISCOVER_PEER);
					ALog.i(TAG, "ServiceHandler 被终止探索");
				}
					
				break;
			case MSG_DISCOVER_STOP:
				ALog.i(TAG, "ServiceHandler 尝试终止探索");
				stopPeer();
				break;
			}
		}
	};
	
	private boolean mIsRegistered = false;
	private IntentFilter mIntentFilter = new IntentFilter();
    private final BroadcastReceiver mWifiDirectReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
	        ALog.i(TAG, "onReceive action:" + action);
	        if (mWifiDirectManager == null) {
	        	return;
	        }
	        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
	        	if (mWifiDirectManager.getWifiP2pManager() == null) {
	                return;
	            }
	            NetworkInfo networkInfo = (NetworkInfo) intent
	                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
	            ALog.i(TAG, "onReceive networkInfo.isConnected():" + networkInfo.isConnected());
	            
	            if (networkInfo.isConnected()) {
	            	mIsWifiP2pConnected=true;
	            	mWifiDirectManager.getWifiP2pManager().requestConnectionInfo(mWifiDirectManager.getChannel(), mWifiDirectManager);
	            } else {
	            	mIsWifiP2pConnected=false;
	            	mWifiDirectManager.onClearData();
	            }
	            if(!mStopDiscover)
	            	discover();
	        } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
	        	int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
	        	ALog.i(TAG, "onReceive state:" + state);
	        	if (state == WifiManager.WIFI_STATE_ENABLED) {
	        		mWifiDirectManager.onWifiEnabled();
	        	} else if (state == WifiManager.WIFI_STATE_DISABLED) {
	        		mWifiDirectManager.onClearData();
	        	}
	        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
	        	mWifiDirectManager.getWifiP2pManager().requestPeers
	        			(mWifiDirectManager.getChannel(), mWifiDirectManager);
	        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
	        	WifiP2pDevice device = (WifiP2pDevice) intent.getParcelableExtra(
	                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
	        	mWifiDirectManager.setWifiP2pDevice(device);
	        }
		}
    };
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		ALog.i(TAG, "-----------onCreate-----------");
		mPM = (PowerManager)getSystemService(Context.POWER_SERVICE);
		if (mPM != null) {
			mWakeLock = mPM.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		}
		
		if (!mIsRegistered) {
			mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
			mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
			mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
			mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
			registerReceiver(mWifiDirectReceiver, mIntentFilter);
			mIsRegistered = true;
		}
		initService();
	}
	
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int retCode = super.onStartCommand(intent, flags, startId);
        if (retCode == START_STICKY) {
        	ALog.i(TAG, "onStartCommand mIsServiceInit:" + mIsServiceInit);
        	if (!mIsServiceInit) {
        		ALog.i(TAG, "onStartCommand init mobile.easyserver.server after 3 seconds!");
       /* 		mServiceHandler.postDelayed(new Runnable() {
        			@Override
        			public void run() {
        				initService();
        			}
        		}, 500);*/
        	} else {
        		sendBroadcast(new Intent(Utilities.WIFIDIRECT_SERVICE_STARTCOMMAND_ACTION));
        	}
        }
        return retCode;
	}

	@Override
	public void onDestroy() {
		
		ALog.i(TAG, "-----------onDestroy-----------");
		
		clearService();
		
		if (mIsRegistered) {
			unregisterReceiver(mWifiDirectReceiver);
			mIsRegistered = false;
		}
		
		if (mWifiDirectManager != null) {
			mWifiDirectManager.getWifiP2pManager();
		}
		
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		if(mBinder==null)
			mBinder=new WifiDirectBinder();

		return mBinder;
	}
	
	private void initService() {
		
		ALog.i(TAG, "initService mIsServiceInit:" + mIsServiceInit);
		
		mIsServiceInit = true;
		
		if (Utilities.isWifiP2pSupported()) {
    		if (mWifiDirectManager == null) {
	        	mWifiDirectManager = WifiDirectManager.getInstance(this);
	        }
   
    	}
		// send mobile.easyserver.service started broadcast
     	sendBroadcast(new Intent(Utilities.WIFIDIRECT_SERVICE_STARTED_ACTION));
	}
	
	private void clearService() {
		
		ALog.i(TAG, "clearService mIsServiceInit:" + mIsServiceInit);
		mIsServiceInit = false;
		
		if (Utilities.isWifiP2pSupported()) {
		}
		
		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
		}
		
		// send mobile.easyserver.service started broadcast
		sendBroadcast(new Intent(Utilities.WIFIDIRECT_SERVICE_STOPED_ACTION));
	}
	
	/**
	 *  startDiscover之前 请确保 stopDiscover 以保证效率和成功率
	 * 
	 * */
	public void startDiscover(){
		mStopDiscover=false;
//		stopDiscover(); 
		if(mIsWifiP2pSearching){
			if(!mServiceHandler.hasMessages(MSG_DISCOVER_PEER))
				discover();
			else{
				mServiceHandler.removeMessages(MSG_DISCOVER_PEER);
				mServiceHandler.obtainMessage(MSG_DISCOVER_PEER).sendToTarget();
			}
			return;	
		}
		sendBroadcast(new Intent(WifiDirectManager.WIFI_DIRECT_BEGIN_SEARCHING_ACTION));
		mIsWifiP2pSearching=true;
		discover();
	}
	
	public void startDiscoverAfterStopDiscover(){
		mServiceHandler.removeMessages(MSG_DISCOVER_STOP);
		mWifiDirectManager.stopDiscover(new ActionListener() {
			@Override
			public void onSuccess() {
				startDiscover();
			}

			@Override
			public void onFailure(int reason) {
				startDiscover();
			}
		});
	}
	
	protected void discover(){
		mWifiDirectManager.discover(null);
//		Message msg=mServiceHandler.obtainMessage(MSG_DISCOVER_PEER);
//		mServiceHandler.sendMessageDelayed(msg, 2000);
	}
	
	public void stopDiscover(){
		mStopDiscover=true;
		stopPeer();
	}
	
	protected void stopPeer(){
		mWifiDirectManager.stopDiscover(new ActionListener() {
			@Override
			public void onSuccess() {
				ALog.i(TAG, "WifiDirectService 停止探索");
				mIsWifiP2pSearching=false;
				sendBroadcast(new Intent(WifiDirectManager.WIFI_DIRECT_END_SEARCHING_ACTION));		
			}
			@Override
			public void onFailure(int reason) {
				Message msg=mServiceHandler.obtainMessage(MSG_DISCOVER_STOP);
//				mServiceHandler.sendMessageDelayed(msg, 2000);
			}
		});
		
	}
	
	public boolean isWifiP2pConnected(){
		return mIsWifiP2pConnected;
	}
	
	public boolean isWifiP2pSearching(){
		return mIsWifiP2pSearching;
	}
	
	public void resetState(){
		mIsWifiP2pConnected=false;
		mIsWifiP2pSearching=false;
		mStopDiscover=false;
	}
	
	public class WifiDirectBinder extends Binder{
		public WifiDirectService getService(){
			return WifiDirectService.this;
		}
	}
	
	
}
