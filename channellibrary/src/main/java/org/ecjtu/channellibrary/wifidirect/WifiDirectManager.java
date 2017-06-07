package org.ecjtu.channellibrary.wifidirect;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Handler;
import android.os.Message;

import org.ecjtu.channellibrary.wifidirect.util.Utilities;
import org.ecjtu.channellibrary.wifidirect.util.ALog;

@SuppressLint("NewApi")
public class WifiDirectManager implements ConnectionInfoListener, PeerListListener {
	
	private static final String TAG = "WifiDirectManager";
	
	// singleton
    private static WifiDirectManager sInstance = null;
    
    private String mWifiP2pGroupOwnerAddress = null;
    private String mRemoteWifiP2pDeviceAddress = null;
//    private int mLatestConnectedWifiType = Utilities.WIFI_CONNECTED_NONE;
    
    private Context mContext;
	private Channel mWifiChannel;
	private WifiP2pManager mWifiP2pManager;
	private WifiP2pInfo mWifiP2pInfo;
	private WifiP2pDevice mWifiP2pDevice;
	private WifiManager mWifiManager;
	private boolean mIsWifiP2pConnected = false;
//	private boolean mIsWifiP2pSocketConnected = false;
	private boolean mIsWifiP2pEnabled = false;
	
	
	public static final String WIFI_DIRECT_DISCOVERED_PEER_ACTION="wifidirect.action.DISCOVERED_PEER_ACTION";
	public static final String WIFI_DIRECT_CONNECTED_ACTION="wifidirect.action.CONNECTED_ACTION";
	public static final String WIFI_DIRECT_CONNECTION_INFO_AVAILABLE_ACTION="wifidirect.action.CONNECTION_INFO_AVAILABLE_ACTION";
	public static final String WIFI_DIRECT_CONNECT_FAILED="wifidirect.action.CONNECT_FAILED";
	
	public static final String WIFI_DIRECT_BEGIN_SEARCHING_ACTION="wifidirect.action.BEGIN_SEARCHING_ACTION";
	public static final String WIFI_DIRECT_END_SEARCHING_ACTION="wifidirect.action.END_SEARCHING_ACTION";
	
	private List<WifiP2pDevice> mWifiP2pPeers = new ArrayList<WifiP2pDevice>();
    
	
    private final Handler mManagerHandler = new Handler() {
    	@Override
		public void handleMessage(Message msg) {
    		ALog.i(TAG, "mManagerHandler msg.what:" + msg.what);
    		switch (msg.what) {
    		case Utilities.MSG_WIFI_DISCOVER_PEER_FAILED:
    			ALog.i(TAG, "Wifi discover peer failed.now retry");
    			discover((ActionListener)msg.obj);
    			break;
    		case Utilities.MSG_WIFI_DISCOVER_STOP_FAILED:
    			ALog.i(TAG, "Wifi discover stop failed.now retry");
    			stopDiscover((ActionListener)msg.obj);
    			break;
    		case Utilities.MSG_WIFI_DISCOVER_PEER_SUCCESS:
    			ALog.i(TAG, "Wifi discover peer success.");
    			sendWifiDirectBroadcast(WIFI_DIRECT_DISCOVERED_PEER_ACTION);
    			break;
    		case Utilities.MSG_WIFI_CONNECT_FAILED:
    			ALog.i(TAG, "Wifi connect failed.");
    			break;
    		case Utilities.MSG_WIFI_CONNECT_SUCCESS:
    			ALog.i(TAG, "Wifi connect success.");
    			sendWifiDirectBroadcast(WIFI_DIRECT_CONNECT_FAILED);
    			break;
    		case Utilities.MSG_WIFI_CONNECTED:
    			mIsWifiP2pConnected = true;
    			ALog.i(TAG, "Wifi now p2pConnected.");
    			sendWifiDirectBroadcast(WIFI_DIRECT_CONNECTED_ACTION);
    			break;
    		}
    	}
    };
    
    
    private void sendWifiDirectBroadcast(String action) {
    	Intent intent = new Intent(action);
    	mContext.sendBroadcast(intent);
    }
    
    public String getWifiMacAddress(Context context) {
    	WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
        ALog.i(TAG, "getWifiMacAddress macAddress:" + macAddress);
        return macAddress;
    }
    
	private void init(Context context) {
    	ALog.i(TAG, "init-----------");
    	if (mContext == null) {
    		mContext = context;
    	}
    	
//    	mLatestConnectedWifiType = Utilities.WIFI_CONNECTED_NONE;
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) context.getApplicationContext()
					.getSystemService(Context.WIFI_SERVICE);
		}
    	if (mWifiP2pManager == null) {
			mWifiP2pManager = (WifiP2pManager) context
					.getSystemService(Context.WIFI_P2P_SERVICE);
    	}
    	if (mWifiChannel == null && mWifiP2pManager != null) {
    		mWifiChannel = mWifiP2pManager.initialize(context, context.getMainLooper(), null);
    	}
    }
    
    public static synchronized WifiDirectManager getInstance(Context context) {
    	ALog.i(TAG, "getInstance sInstance:" + sInstance);
    	if (sInstance == null) {
    		sInstance = new WifiDirectManager();
    		sInstance.init(context);
    	}
    	return sInstance;
    }
    
    public static synchronized WifiDirectManager getInstance() {
    	ALog.i(TAG, "getInstance sInstance:" + sInstance);
    	return sInstance;
    }
    
    public Handler getManagerHandler() {
    	return mManagerHandler;
    }
    
	public void disconnectWifiDirect() {
    	if (mWifiP2pDevice != null) {
    		ALog.i(TAG, "disconnectWifiDirect mWifiP2pDevice.status:" + mWifiP2pDevice.status);
    		if (mWifiP2pDevice.status == WifiP2pDevice.CONNECTED) {
    			disconnect();
    		} else if (mWifiP2pDevice.status == WifiP2pDevice.AVAILABLE 
    				|| mWifiP2pDevice.status == WifiP2pDevice.INVITED) {
    			cancelConnect();
    		}
    	}
    }
    
    public void setWifiP2pInfo(WifiP2pInfo info) {
    	mWifiP2pInfo = info;
    	ALog.i(TAG, "setWifiP2pInfo mWifiP2pInfo:" + mWifiP2pInfo);
    }
    
    public void setWifiP2pDevice(WifiP2pDevice device) {
    	mWifiP2pDevice = device;
    	ALog.i(TAG, "setWifiP2pDevice device:" + device.deviceAddress);
    }
    
    
    public void setIsWifiP2pConnected(boolean isConnected) {
    	mIsWifiP2pConnected = isConnected;
    }

    
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
    	mIsWifiP2pEnabled = isWifiP2pEnabled;
    }
    
	public void setWifiEnabled() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}
	
	public void setWifiDisabled() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}
    
    public boolean isWifiP2pEnabled() {
    	return mIsWifiP2pEnabled;
    }
    
    
    public WifiP2pManager getWifiP2pManager() {
    	return mWifiP2pManager;
    }
    
    public WifiP2pDevice getWifiP2pDevice() {
    	return mWifiP2pDevice;
    }
    
    public Channel getChannel() {
    	return mWifiChannel;
    }
    
    public List<WifiP2pDevice> getWifiP2pPeers() {
    	return mWifiP2pPeers;
    }
	
	public void discover(ActionListener actionListener) {
		ALog.i(TAG, "discover mWifiP2pManager:" + mWifiP2pManager);
		
		if(!mWifiManager.isWifiEnabled())
			mWifiManager.setWifiEnabled(true);

		if (mWifiP2pManager != null) {
			
			if(actionListener==null){
				
				final ActionListener fActionListener=actionListener;
				actionListener=new ActionListener() {
					private int mRetryTimes=0;	
					@Override
					public void onSuccess() {
						ALog.i(TAG, "discover onSuccess!");
						mRetryTimes=3;
					}
					@Override
					public void onFailure(int reason) {
						ALog.i(TAG, "discover failed reason:" + errorCode2String(reason));
						if(mRetryTimes>=3)
							return;
//						Message msg = mManagerHandler.obtainMessage(Utilities.MSG_WIFI_DISCOVER_PEER_FAILED);
//						msg.obj=fActionListener;
//						mManagerHandler.sendMessageDelayed(msg, 500);
						mRetryTimes++;
					}
				};
				
			}
			mWifiP2pManager.discoverPeers(mWifiChannel, actionListener);
		}
	}
	
	public void stopDiscover(ActionListener actionListener){
		if(mWifiP2pManager!=null){
			final ActionListener fActionListener=actionListener;
			if(actionListener==null){
				
				actionListener=new ActionListener() {
					private int mRetryTimes=0;
					@Override
					public void onSuccess() {
						ALog.i(TAG, "stopDiscover onSuccess");
						mRetryTimes=3;
					}
					
					@Override
					public void onFailure(int reason) {
						ALog.i(TAG, "stopDiscover failed reason:" + errorCode2String(reason));
						if(mRetryTimes>=3)
							return;
//						Message msg = mManagerHandler.obtainMessage(Utilities.MSG_WIFI_DISCOVER_STOP_FAILED);
//						msg.obj=fActionListener;
//						mManagerHandler.sendMessageDelayed(msg, 500);
						mRetryTimes++;
					}
				};
			}
			mWifiP2pManager.stopPeerDiscovery(mWifiChannel, actionListener);
		}
	}
	
	
	public static final int ERROR               = 0;

    public static final int P2P_UNSUPPORTED     = 1;

    public static final int BUSY                = 2;
	
	public String errorCode2String(int code) {
		String res = null;
		switch (code) {
		case 0:
			res = "WifiDirect has an error.";
			break;
		case 1:
			res = "WifiDirect is unsupported.";
			break;
		case 2:
			res = "framework is busy.do wifi is open?";
			break;
		}
		return res;
	}
	
	public void connect(WifiP2pConfig config) {
		ALog.i(TAG, "connect mWifiP2pManager:" + mWifiP2pManager + ", config:" + config);
		if (mWifiP2pManager != null) {
			mWifiP2pManager.connect(mWifiChannel, config, new ActionListener() {

				@Override
				public void onSuccess() {
					ALog.v(TAG, "connect onSuccess!");
					Message msg = mManagerHandler.obtainMessage(Utilities.MSG_WIFI_CONNECT_SUCCESS);
					mManagerHandler.sendMessage(msg);
				}
				
				@Override
				public void onFailure(int reason) {
					ALog.v(TAG, "connect failed reason:" + reason);
//					Message msg = mManagerHandler.obtainMessage(Utilities.MSG_WIFI_CONNECT_FAILED);
//					if(mManagerHandler.hasMessages(Utilities.MSG_WIFI_CONNECT_FAILED))
//						return;
//					mManagerHandler.sendMessageDelayed(msg, 500);
				}
			});
		}
	}
	
	public void disconnect() {
		ALog.i(TAG, "disconnect mWifiP2pManager:" + mWifiP2pManager + ", mWifiChannel:" + mWifiChannel);
		if (mWifiP2pManager != null) {
			mWifiP2pManager.removeGroup(mWifiChannel, new ActionListener() {
				@Override
				public void onSuccess() {
					ALog.v(TAG, "disconnect onSuccess!");
				}
				@Override
				public void onFailure(int reason) {
					ALog.v(TAG, "disconnect failed reason:" + reason);
				}
			});
		}
	}
	
	public void cancelConnect() {
		ALog.i(TAG, "cancelDisconnect mWifiP2pManager:" + mWifiP2pManager + ", mWifiChannel:" + mWifiChannel);
		if (mWifiP2pManager != null) {
			mWifiP2pManager.cancelConnect(mWifiChannel, new ActionListener() {

				@Override
				public void onSuccess() {
					ALog.v(TAG, "cancelDisconnect onSuccess!");
				}

				@Override
				public void onFailure(int reason) {
					ALog.v(TAG, "cancelDisconnect failed reason:" + reason);
				}
			});
		}
	}
	
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		ALog.i(TAG, "onPeersAvailable mRemoteWifiP2pDeviceAddress:" + mRemoteWifiP2pDeviceAddress);
		mWifiP2pPeers.clear();
		mWifiP2pPeers.addAll(peers.getDeviceList());
		if (mRemoteWifiP2pDeviceAddress != null) {
			int size = mWifiP2pPeers.size();
			for (int i = 0; i < size; i++) {
				WifiP2pDevice device = mWifiP2pPeers.get(i);
				ALog.i(TAG, "onPeersAvailable i:" + i + ", address:"
						+ device.deviceAddress + ", name:" + device.deviceName);
				if (mRemoteWifiP2pDeviceAddress.substring(3).equals(device.deviceAddress.substring(3))) {
					mRemoteWifiP2pDeviceAddress = device.deviceAddress;
				}
			}
		} else {
			int size = mWifiP2pPeers.size();
			if (size > 0) {
				WifiP2pDevice device = mWifiP2pPeers.get(0);
				mRemoteWifiP2pDeviceAddress = device.deviceAddress;
			}
		}
		Message msg = mManagerHandler
				.obtainMessage(Utilities.MSG_WIFI_DISCOVER_PEER_SUCCESS);
		mManagerHandler.sendMessageDelayed(msg, 500);

	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		ALog.i(TAG, "onConnectionInfoAvailable info:" + info + ", mIsWifiP2pConnected:" + mIsWifiP2pConnected);
		doWifiDirectConnectionAvailable(info);
	}
	
	public void doWifiDirectConnectionAvailable(WifiP2pInfo info) {
		if (mIsWifiP2pConnected) {
			return;
		}
		
		if (info != null && info.groupOwnerAddress != null) {
			mWifiP2pGroupOwnerAddress = info.groupOwnerAddress.getHostAddress();
			ALog.i(TAG, "doWifiDerectConnectionAvailable mWifiP2pGroupOwnerAddress:" + mWifiP2pGroupOwnerAddress);
		}
		if(info==null){
			mWifiP2pManager.requestConnectionInfo(getChannel(), this);
			return;
		}
		setWifiP2pInfo(info);
		Message msg = mManagerHandler.obtainMessage(Utilities.MSG_WIFI_CONNECTED);
		mManagerHandler.sendMessage(msg);
		mIsWifiP2pConnected = true;
		
		mContext.sendBroadcast(new Intent(WIFI_DIRECT_CONNECTION_INFO_AVAILABLE_ACTION));
/*		if (info.groupFormed && info.isGroupOwner) {
			ALog.i(TAG, "doWifiDirectConnectionAvailable is GroupOwner!!");
		} else if (info.groupFormed) {
		}*/
	}
	
	public void onWifiEnabled() {
		// common connect
	}
	
	public void onClearData() {
		ALog.i(TAG, "onClearData");
		mIsWifiP2pConnected = false;
//		mIsWifiP2pSocketConnected = false;
//		mLatestConnectedWifiType = Utilities.WIFI_CONNECTED_NONE;
		mWifiP2pPeers.clear();
	}
	
	public WifiP2pInfo getWifiP2pInfo()
	{
		return mWifiP2pInfo;
	}
	
	public String getWifiP2pGroupOwnerAddress(){
		return mWifiP2pGroupOwnerAddress;
	}
	
	public String getRemoteP2pDeviceMacAddress(){
		return mRemoteWifiP2pDeviceAddress;
	}

	public void wifiP2pConnect() {
		ALog.i(TAG, "wifiP2pConnect mRemoteWifiP2pDeviceAddress:" + mRemoteWifiP2pDeviceAddress);
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = mRemoteWifiP2pDeviceAddress;
		config.wps.setup = WpsInfo.PBC;
	}
	
//	@SuppressWarnings("resource")
//	public List<String> getLocalP2pIPFromArp()
//	{
//		BufferedReader reader=null;
//		String line;
//		List<String> list=new ArrayList<>();
//		try {
//			reader=new BufferedReader(new FileReader("/proc/net/arp"));
//			while((line=reader.readLine())!=null){
//				String[] params=line.split("\\s+");
//				
//				int size=params.length-1;
//				
//				if (params[size].contains("p2p")) {
//					list.add(params[0]);
//				}
//			}
//			reader.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}		
//		return list;
//	}
	
	public String getLocalP2PIP(){
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();

					String iface = intf.getName();
					if(iface.contains("p2p")){
						if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
							return getDottedDecimalIP(inetAddress.getAddress());
						}
					}
				}
			}
		} catch (SocketException ex) {
//			Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
		} catch (NullPointerException ex) {
//			Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
		}
		return null;
	}

	public String[] getLocalWLANIps(){
		List<String> result=new ArrayList<>();
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();

					String iface = intf.getName();
					if(iface.contains("wlan")){
						if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
							result.add(getDottedDecimalIP(inetAddress.getAddress()));
						}
					}
				}
			}
		} catch (SocketException ex) {
		} catch (NullPointerException ex) {
		}
		return result.toArray(new String[result.size()]);
	}
	
	private static String getDottedDecimalIP(byte[] ipAddr) {
		String ipAddrStr = "";
		for (int i=0; i<ipAddr.length; i++) {
			if (i > 0) {
				ipAddrStr += ".";
			}
			ipAddrStr += ipAddr[i]&0xFF;
		}
		return ipAddrStr;
	}
	
	/**
	 *  ARP表中没有本机IP信息?
	 * */
	@SuppressWarnings("resource")
	public String getIPFromMac(String MAC) {
	
		BufferedReader reader=null;
		String line;
		try {
			reader=new BufferedReader(new FileReader("/proc/net/arp"));
			while((line=reader.readLine())!=null){
				String[] params=line.split("\\s+");
				
				if(params[3].equals(MAC))
					return params[0];
			}
			reader.close();
		} catch (IOException e) {
		}		
		return null;
	}
	
	public void setLatestConnectedWifiDevice(WifiP2pDevice device, int type) {
//		mLatestConnectedWifiType = type;
	}
	
	public WifiManager getWifiManager(){
		return mWifiManager;
	}
	
	public void setDeviceName(String name){
//		setDeviceName(Channel c, String devName, ActionListener listener)
		Class<WifiP2pManager> clzz = WifiP2pManager.class;
		
		ActionListener listener=new ActionListener() {
			@Override
			public void onSuccess() {
				ALog.i(TAG, "setDeviceName succees");
			}
			@Override
			public void onFailure(int reason) {
				ALog.i(TAG, "setDeviceName failure");
			}
		};
		
		Method m=null;
		try {
			m=clzz.getDeclaredMethod("setDeviceName", 
					new Class[]{Channel.class,String.class,ActionListener.class});
			m.setAccessible(true);
			m.invoke(mWifiP2pManager, getChannel(),name,listener);	
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(m!=null)
				m.setAccessible(false);
		}
	}
	
	public static String getDeviceStatus(int deviceStatus) {
		switch (deviceStatus) {
		case WifiP2pDevice.AVAILABLE:
			return "Available";
		case WifiP2pDevice.INVITED:
			return "Invited";
		case WifiP2pDevice.CONNECTED:
			return "Connected";
		case WifiP2pDevice.FAILED:
			return "Failed";
		case WifiP2pDevice.UNAVAILABLE:
			return "Unavailable";
		default:
			return "Unknown error";
		}
	}
	
}
