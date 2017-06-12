package org.ecjtu.channellibrary.wifidirect;

import java.util.Map;


import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.ecjtu.channellibrary.wifidirect.util.ALog;

@SuppressLint("NewApi")
public class WifiDirectGroupManager implements GroupInfoListener,DnsSdServiceResponseListener,
DnsSdTxtRecordListener{
	
	private static final String TAG="WifiDirectGroupManager";
	
	private static WifiDirectGroupManager sInstance;
	
	private WifiDirectManager mWifiDirectManager;
	
	private WifiP2pManager mWifiP2pManager;
	
	private Channel mChannel;
	
	private String mHotSpotName;
	
	private String mHotSpotPwd;
	
	private WifiP2pServiceRequest mServiceRequest;
	
	private WifiP2pDnsSdServiceInfo mServiceInfo;
	
	private boolean mIsRegisterRequest=false;
	
	private boolean mIsAddLocalService=false;
	
	public static class Event{
		
//		public static final int CREATE_GROUP_SUCCESS = 1;
//		public static final int CREATE_GROUP_FAILURE = 2;
//		public static final int REMOVE_GROUOP_SUCCESS= 3;
//		public static final int REMOVE_GROUPO_FAILURE= 4;
		public static final int GROUP_INFO_AVAILABLE= 5;
//		public static final int ADD_LOCAL_SERVICE_SUCCESS= 6;
//		public static final int ADD_LOCAL_SERVICE_FAILURE= 7;
//		public static final int ADD_SERVICE_REQUEST_SUCCESS= 8;
//		public static final int ADD_SERVICE_REQUEST_FAILURE= 9;
//		public static final int DISCOVER_SERVICE_SUCCESS= 10;
//		public static final int DISCOVER_SERVICE_FAILURE= 11;
//		public static final int REMOVE_SERVICE_REQUEST_SUCCESS= 12;
//		public static final int REMOVE_SERVICE_REQUEST_FAILURE= 13;
//		public static final int REMOVE_SERVICE_SUCCESS= 14;
//		public static final int REMOVE_SERVICE_FAILURE= 15;
		public static final int SERVICE_AVAILABLE= 16;
		public static final int SERVICE_INFO_AVAILABLE= 17;
		
		//for user
		public static final int START_GROUP_SUCCESS=100+1;
		public static final int START_GROUP_FAILURE=100+2;
		public static final int STOP_GROUP_SUCCESS=100+3;
		public static final int STOP_GROUP_FAILURE=100+4;
		public static final int OPEN_SERVICE_SUCCESS=100+6;
		public static final int OPEN_SERVICE_FAILURE=100+7;
		public static final int STOP_SERVICE_SUCCESS=100+8;
		public static final int STOP_SERVICE_FAILURE=100+9;
		public static final int DISCOVER_SUCCESS=100+10;
		public static final int DISCOVER_FAILURE=100+11;
	}
	
	private boolean mIsGroupStart=false;
	private boolean mIsServiceStart=false;
	private boolean mIsDiscovering=false;
	
	public boolean isGroupStart(){
		return mIsGroupStart;
	}
	
	public boolean isServiceStart(){
		return mIsServiceStart;
	}
	
	public boolean isDiscovering(){
		return mIsDiscovering;
	}
	
	public void resetState(){
		mIsGroupStart=false;
		mIsServiceStart=false;
		mIsDiscovering=false;
		mIsRegisterRequest=false;
		mIsAddLocalService=false;
	}
	
	private class MHandler extends Handler{
		public MHandler(Looper looper){
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch(msg.what){
			case Event.START_GROUP_SUCCESS:
				mIsGroupStart=true;
				if(mHandleEventListener!=null)
					mHandleEventListener.handle(msg.what, null);
				break;
			case Event.START_GROUP_FAILURE:
				if(mHandleEventListener!=null)
					mHandleEventListener.handle(msg.what, msg.obj);
				break;
			case Event.STOP_GROUP_SUCCESS:
				mIsGroupStart=false;
				if(mHandleEventListener!=null)
					mHandleEventListener.handle(msg.what, null);
				break;
			case Event.STOP_GROUP_FAILURE:
				if(mHandleEventListener!=null)
					mHandleEventListener.handle(msg.what, msg.obj);
				break;
			case Event.OPEN_SERVICE_SUCCESS:
				mIsServiceStart=true;
				if(mHandleEventListener!=null)
					mHandleEventListener.handle(msg.what, null);
				break;
			case Event.OPEN_SERVICE_FAILURE:
				if(mHandleEventListener!=null)
					mHandleEventListener.handle(msg.what, msg.obj);
				break;
			case Event.STOP_SERVICE_SUCCESS:
				mIsGroupStart=false;
				if(mHandleEventListener!=null)
					mHandleEventListener.handle(msg.what, null);
				break;
			case Event.STOP_SERVICE_FAILURE:
				if(mHandleEventListener!=null)
					mHandleEventListener.handle(msg.what, msg.obj);
				break;
			case Event.DISCOVER_SUCCESS:
				mIsDiscovering=true;
				if(mHandleEventListener!=null)
					mHandleEventListener.handle(msg.what, null);
				break;
			case Event.DISCOVER_FAILURE:
				mIsDiscovering=false;
				if(mHandleEventListener!=null)
					mHandleEventListener.handle(msg.what, msg.obj);
				break;
				
			case Event.SERVICE_AVAILABLE:
				mIsGroupStart=true;
				if(mHandleEventListener!=null)
					mHandleEventListener.handle(msg.what, msg.obj);
				break;

			case Event.SERVICE_INFO_AVAILABLE:
				if(mHandleEventListener!=null)
					mHandleEventListener.handle(msg.what, msg.obj);
				break;
				
			case Event.GROUP_INFO_AVAILABLE:
				if(mHandleEventListener!=null)
					mHandleEventListener.handle(msg.what, null);
				break;
			}
			
		}
	}
	
	private MHandler mHandler;
	
	private IHandleEvent mHandleEventListener;
	
	private WifiDirectGroupManager(WifiDirectManager manager,Context context){
		mWifiP2pManager=manager.getWifiP2pManager();
		mChannel=manager.getChannel();
		mWifiDirectManager=manager;
		
		mServiceRequest=WifiP2pServiceRequest.newInstance
				(WifiP2pServiceInfo.SERVICE_TYPE_ALL);
		
		mWifiP2pManager.setDnsSdResponseListeners(mChannel, this, this);
		
		mHandler=new MHandler(context.getMainLooper());
	}
	
	public static WifiDirectGroupManager getInstance(Context context){
		
		if(sInstance==null){
			sInstance=new WifiDirectGroupManager
					(WifiDirectManager.getInstance(),context);
		}
		return sInstance;
	}
	
	protected String errorCode2String(int errorCode){
		return mWifiDirectManager.errorCode2String(errorCode);
	}
	
	public void createGroup(){
		mWifiP2pManager.createGroup(mChannel, new ActionListener() {
			@Override
			public void onSuccess() {
				ALog.i(TAG, "create group success");
//				mHandler.obtainMessage(Event.CREATE_GROUP_SUCCESS).sendToTarget();
				mHandler.obtainMessage(Event.START_GROUP_SUCCESS).sendToTarget();
				mWifiP2pManager.requestGroupInfo(mChannel, WifiDirectGroupManager.this);
			}
			@Override
			public void onFailure(int reason) {
				ALog.i(TAG, "create group failure "+errorCode2String(reason));
//				mHandler.obtainMessage(Event.CREATE_GROUP_FAILURE,reason).sendToTarget();
				mHandler.obtainMessage(Event.START_GROUP_FAILURE).sendToTarget();
				//try to get data,when is start
				mWifiP2pManager.requestGroupInfo(mChannel, WifiDirectGroupManager.this);
			}
		});
	}
	
	public void removeGroup(){
		mWifiP2pManager.removeGroup(mChannel, new ActionListener() {
			@Override
			public void onSuccess() {
				ALog.i(TAG, "remove group success");
//				mHandler.obtainMessage(Event.REMOVE_GROUOP_SUCCESS).sendToTarget();
				mHandler.obtainMessage(Event.STOP_GROUP_SUCCESS).sendToTarget();
			}
			@Override
			public void onFailure(int reason) {
				ALog.i(TAG, "remove group failure "+errorCode2String(reason));
//				mHandler.obtainMessage(Event.REMOVE_GROUPO_FAILURE,reason).sendToTarget();
				mHandler.obtainMessage(Event.STOP_GROUP_FAILURE).sendToTarget();
			}
		});
	}

	@Override
	public void onGroupInfoAvailable(WifiP2pGroup group) {
		if(group!=null){
			mHotSpotName=group.getNetworkName();
			mHotSpotPwd=group.getPassphrase();
			mHandler.obtainMessage(Event.GROUP_INFO_AVAILABLE).sendToTarget();
		}else{
			mWifiP2pManager.requestGroupInfo(mChannel, WifiDirectGroupManager.this);
		}
	}
	
	/**
	 *  @return param[0] is name,param[1] is pwd
	 * */
	public String[] getHotSpotInfo(){
		return new String[]{mHotSpotName,mHotSpotPwd};
	}
	
	public void openLocalService(String serviceName,Map<String, String> valueMap){
		mServiceInfo=WifiP2pDnsSdServiceInfo.newInstance(serviceName,
				"_ipp._tcp", valueMap);
		
		mWifiP2pManager.addLocalService(mChannel,mServiceInfo ,new ActionListener() {
			@Override
			public void onSuccess() {
				ALog.i(TAG, "addLocalService success");
				mIsAddLocalService=true;
//				mHandler.obtainMessage(Event.ADD_LOCAL_SERVICE_SUCCESS).sendToTarget();
				if(mIsRegisterRequest){
					mWifiP2pManager.discoverServices(mChannel, new ActionListener() {
						@Override
						public void onSuccess() {
							ALog.i(TAG, "discoverServices success");
//							mHandler.obtainMessage(Event.DISCOVER_SERVICE_SUCCESS).sendToTarget();
							mHandler.obtainMessage(Event.OPEN_SERVICE_SUCCESS).sendToTarget();
						}
						
						@Override
						public void onFailure(int reason) {
							ALog.i(TAG, "discoverSerivces failure "+errorCode2String(reason));
//							mHandler.obtainMessage(Event.DISCOVER_SERVICE_FAILURE,reason).sendToTarget();
							if(!mHandler.hasMessages(Event.OPEN_SERVICE_FAILURE)){
								mHandler.obtainMessage(Event.OPEN_SERVICE_FAILURE).sendToTarget();
							}
						}
					});
					return;
				}
				
				mWifiP2pManager.addServiceRequest(mChannel, mServiceRequest, new ActionListener() {
					@Override
					public void onSuccess() {
						ALog.i(TAG, "addServiceRequest success");
						mIsRegisterRequest=true;
//						mHandler.obtainMessage(Event.ADD_SERVICE_REQUEST_SUCCESS).sendToTarget();
						mWifiP2pManager.discoverServices(mChannel, new ActionListener() {
							@Override
							public void onSuccess() {
								ALog.i(TAG, "discoverServices success");
//								mHandler.obtainMessage(Event.DISCOVER_SERVICE_SUCCESS)
//								.sendToTarget();
								mHandler.obtainMessage(Event.OPEN_SERVICE_SUCCESS).sendToTarget();
							}
							
							@Override
							public void onFailure(int reason) {
								ALog.i(TAG, "discoverSerivces failure "+errorCode2String(reason));
//								mHandler.obtainMessage(Event.DISCOVER_SERVICE_FAILURE,reason)
//								.sendToTarget();
								
								if(!mHandler.hasMessages(Event.OPEN_SERVICE_FAILURE)){
									mHandler.obtainMessage(Event.OPEN_SERVICE_FAILURE).sendToTarget();
								}
							}
						});
					}
					@Override
					public void onFailure(int reason) {
						ALog.i(TAG, "addServiceRequest failure "+errorCode2String(reason));
//						mHandler.obtainMessage(Event.ADD_SERVICE_REQUEST_FAILURE,reason).sendToTarget();
						if(!mHandler.hasMessages(Event.OPEN_SERVICE_FAILURE)){
							mHandler.obtainMessage(Event.OPEN_SERVICE_FAILURE).sendToTarget();
						}
					}
				});
			}
			@Override
			public void onFailure(int reason) {
				ALog.i(TAG, "addLocalService failure "+errorCode2String(reason));
				mIsAddLocalService=false;
//				mHandler.obtainMessage(Event.ADD_LOCAL_SERVICE_FAILURE,reason).sendToTarget();
				if(!mHandler.hasMessages(Event.OPEN_SERVICE_FAILURE)){
					mHandler.obtainMessage(Event.OPEN_SERVICE_FAILURE).sendToTarget();
				}
			}
		} );
	}
	
	public void stopLocalService(){
		mWifiP2pManager.removeServiceRequest(mChannel, mServiceRequest, new ActionListener() {
			@Override
			public void onSuccess() {
				ALog.i(TAG, "removeServiceRequest success");
				mIsRegisterRequest=false;
//				mHandler.obtainMessage(Event.REMOVE_SERVICE_REQUEST_SUCCESS).sendToTarget();
				mWifiP2pManager.removeLocalService(mChannel, mServiceInfo, new ActionListener() {
					@Override
					public void onSuccess() {
						ALog.i(TAG, "removeLocalService success");
						mIsAddLocalService=false;
//						mHandler.obtainMessage(Event.REMOVE_SERVICE_SUCCESS).sendToTarget();
						mHandler.obtainMessage(Event.STOP_SERVICE_SUCCESS).sendToTarget();
					}
					@Override
					public void onFailure(int reason) {
						ALog.i(TAG, "removeLocalService failure "+errorCode2String(reason));
//						mHandler.obtainMessage(Event.REMOVE_SERVICE_FAILURE,reason).sendToTarget();
						if(!mHandler.hasMessages(Event.STOP_SERVICE_FAILURE)){
							mHandler.obtainMessage(Event.STOP_SERVICE_FAILURE).sendToTarget();
						}
					}
				});
				
			}
			@Override
			public void onFailure(int reason) {
				ALog.i(TAG, "removeServiceRequest failure "+errorCode2String(reason));
//				mHandler.obtainMessage(Event.REMOVE_SERVICE_REQUEST_FAILURE,reason).sendToTarget();
				if(!mHandler.hasMessages(Event.STOP_SERVICE_FAILURE)){
					mHandler.obtainMessage(Event.STOP_SERVICE_FAILURE).sendToTarget();
				}
			}
		});
	}
	
	public void discoverService(){
		
		if(mIsRegisterRequest){
			mWifiP2pManager.discoverServices(mChannel, new ActionListener() {
				@Override
				public void onSuccess() {
					ALog.i(TAG, "discoverServices success");
//					mHandler.obtainMessage(Event.DISCOVER_SERVICE_SUCCESS).sendToTarget();
					mHandler.obtainMessage(Event.DISCOVER_SUCCESS).sendToTarget();
				}

				@Override
				public void onFailure(int reason) {
					ALog.i(TAG, "discoverServices failure "+errorCode2String(reason));
//					mHandler.obtainMessage(Event.DISCOVER_SERVICE_FAILURE,reason).sendToTarget();
					if(!mHandler.hasMessages(Event.DISCOVER_FAILURE)){
						mHandler.obtainMessage(Event.DISCOVER_FAILURE).sendToTarget();
					}
				}
			});
			return;
		}
		
		
		mWifiP2pManager.addServiceRequest(mChannel, mServiceRequest, new ActionListener() {
			@Override
			public void onSuccess() {
				ALog.i(TAG, "addServiceRequest success");
//				mHandler.obtainMessage(Event.ADD_SERVICE_REQUEST_SUCCESS).sendToTarget();
				mIsRegisterRequest=true;
				mWifiP2pManager.discoverServices(mChannel, new ActionListener() {
					@Override
					public void onSuccess() {
						ALog.i(TAG, "discoverServices success");
//						mHandler.obtainMessage(Event.DISCOVER_SERVICE_SUCCESS).sendToTarget();
						mHandler.obtainMessage(Event.DISCOVER_SUCCESS).sendToTarget();
					}

					@Override
					public void onFailure(int reason) {
						ALog.i(TAG, "discoverServices failure "+errorCode2String(reason));
//						mHandler.obtainMessage(Event.DISCOVER_SERVICE_FAILURE,reason).sendToTarget();
						if(!mHandler.hasMessages(Event.DISCOVER_FAILURE)){
							mHandler.obtainMessage(Event.DISCOVER_FAILURE).sendToTarget();
						}
					}
				});

			}
			@Override
			public void onFailure(int reason) {
				ALog.i(TAG, "addServiceRequest failure "+errorCode2String(reason));
//				mHandler.obtainMessage(Event.ADD_SERVICE_REQUEST_FAILURE,reason).sendToTarget();
				if(!mHandler.hasMessages(Event.DISCOVER_FAILURE)){
					mHandler.obtainMessage(Event.DISCOVER_FAILURE).sendToTarget();
				}
			}
		});
	}

	@Override
	public void onDnsSdTxtRecordAvailable(String fullDomainName,
			Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
		
		ALog.i(TAG, "onDnsSdTxtRecordAvailable");
		
		InfoHolder holder=new InfoHolder();
		holder.mFullDomainName=fullDomainName;
		holder.mValueMap=txtRecordMap;
		holder.mSrcDevice=srcDevice;
		
		mHandler.obtainMessage(Event.SERVICE_INFO_AVAILABLE,holder).sendToTarget();
/*		String res = "";
		for (Entry<String, String> ele : txtRecordMap.entrySet()) {
			res += ele.getKey();
			res += " " + ele.getValue() + " ";
		}*/

	}

	@Override
	public void onDnsSdServiceAvailable(String instanceName,
			String registrationType, WifiP2pDevice srcDevice) {
		ALog.i(TAG, "onDnsSdServiceAvailable name:"+instanceName);
		
		InfoHolder holder=new InfoHolder();
		
		holder.mInstanceName=instanceName;
		holder.mRegistrationType=registrationType;
		holder.mSrcDevice=srcDevice;
		
		mHandler.obtainMessage(Event.SERVICE_AVAILABLE,holder).sendToTarget();
		
	}
	
	public void setHandleEvnetListener(IHandleEvent listener){
		mHandleEventListener=listener;
	}
	
	public static class InfoHolder{
		//value is ready
		public String mFullDomainName;
		public Map<String, String> mValueMap;
		
		//common
		public WifiP2pDevice mSrcDevice;
		
		//mobile.easyserver.service is ready
		public String mInstanceName;
		public String mRegistrationType;
	}
	
	public interface IHandleEvent{		
		void handle(int event, Object result);
	}
	
}


/*public static class Event{
	public static final int CREATE_GROUP_SUCCESS = 1;
	public static final int CREATE_GROUP_FAILURE = 2;
	public static final int REMOVE_GROUOP_SUCCESS= 3;
	public static final int REMOVE_GROUPO_FAILURE= 4;
	public static final int GROUP_INFO_AVAILABLE= 5;
	public static final int ADD_LOCAL_SERVICE_SUCCESS= 6;
	public static final int ADD_LOCAL_SERVICE_FAILURE= 7;
	public static final int ADD_SERVICE_REQUEST_SUCCESS= 8;
	public static final int ADD_SERVICE_REQUEST_FAILURE= 9;
	public static final int DISCOVER_SERVICE_SUCCESS= 10;
	public static final int DISCOVER_SERVICE_FAILURE= 11;
	public static final int REMOVE_SERVICE_REQUEST_SUCCESS= 12;
	public static final int REMOVE_SERVICE_REQUEST_FAILURE= 13;
	public static final int REMOVE_SERVICE_SUCCESS= 14;
	public static final int REMOVE_SERVICE_FAILURE= 15;
	public static final int SERVICE_AVAILABLE= 16;
	public static final int SERVICE_INFO_AVAILABLE= 17;
}

	private class MHandler extends Handler{
public MHandler(Looper looper){
	super(looper);
}

@Override
public void handleMessage(Message msg) {
	super.handleMessage(msg);
	
	switch(msg.what){
	case Event.CREATE_GROUP_SUCCESS:
		if(mHandleEventListener!=null)
			mHandleEventListener.handle(IHandleEvent.Event.START_GROUP_SUCCESS, null);
		break;
	case Event.CREATE_GROUP_FAILURE:
		if(mHandleEventListener!=null)
			mHandleEventListener.handle(IHandleEvent.Event.START_GROUP_FAILURE, msg.obj);
		break;
	case Event.REMOVE_GROUOP_SUCCESS:
		if(mHandleEventListener!=null)
			mHandleEventListener.handle(IHandleEvent.Event.STOP_GROUP_SUCCESS, null);
		break;
	case Event.REMOVE_GROUPO_FAILURE:
		if(mHandleEventListener!=null)
			mHandleEventListener.handle(IHandleEvent.Event.STOP_GROUP_SUCCESS, msg.obj);
		break;
	case Event.GROUP_INFO_AVAILABLE:
		if(mHandleEventListener!=null)
			mHandleEventListener.handle(IHandleEvent.Event.GROUP_INFO_AVAILABLE,(InfoHolder)msg.obj);
		break;
	case Event.ADD_LOCAL_SERVICE_SUCCESS:
		
		break;
	case Event.ADD_LOCAL_SERVICE_FAILURE:
		if(mHandleEventListener!=null)
			mHandleEventListener.handle(IHandleEvent.Event.GROUP_INFO_AVAILABLE,msg.what);
		break;
	case Event.ADD_SERVICE_REQUEST_SUCCESS:
		break;
	case Event.ADD_SERVICE_REQUEST_FAILURE:
		break;
	case Event.DISCOVER_SERVICE_SUCCESS:
		if(mIsAddLocalService&&mIsRegisterRequest){
			if(mHandleEventListener!=null)
				mHandleEventListener.handle(IHandleEvent.Event.GROUP_INFO_AVAILABLE,null);
		}
		
		if(mHandleEventListener!=null)
			mHandleEventListener.handle(IHandleEvent.Event.DISCOVER_SUCCESS,null);
		
		break;
	case Event.DISCOVER_SERVICE_FAILURE:
		if(mHandleEventListener!=null)
			mHandleEventListener.handle(IHandleEvent.Event.DISCOVER_FAILURE,null);
		break;
	case Event.REMOVE_SERVICE_REQUEST_SUCCESS:
		break;
	case Event.REMOVE_SERVICE_REQUEST_FAILURE:
		break;
	case Event.REMOVE_SERVICE_SUCCESS:
		break;
	case Event.REMOVE_SERVICE_FAILURE:
		break;
	case Event.SERVICE_AVAILABLE:
		if(mHandleEventListener!=null)
			mHandleEventListener.handle(IHandleEvent.Event.SERVICE_AVAILABLE,null);
		break;
	case Event.SERVICE_INFO_AVAILABLE:
		if(mHandleEventListener!=null)
			mHandleEventListener.handle(IHandleEvent.Event.SERVICE_INFO_AVAILABLE,null);
		break;
	}
}
}*/
