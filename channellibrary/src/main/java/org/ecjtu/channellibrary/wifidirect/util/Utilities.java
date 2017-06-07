package org.ecjtu.channellibrary.wifidirect.util;

import android.os.Build;
import android.os.Environment;


public class Utilities {
	private static final String TAG = "Utilities";
    
	public static final String FILE_STORED_PATH = 
			Environment.getExternalStorageDirectory() + "/Wifidirect";
	
    public static final String WIFIDIRECT_SERVICE_STARTED_ACTION = 
			"wifidirect.data.transport.action.SERVICE_STARTED";
    
    public static final String WIFIDIRECT_SERVICE_STOPED_ACTION = 
			"wifidirect.data.transport.action.SERVICE_STOPED";
    
    public static final String WIFIDIRECT_SERVICE_STARTCOMMAND_ACTION = 
			"wifidirect.data.transport.action.SERVICE_STARTCOMMAND";
    
    public static final String EXTRA_FILEPATH = "extra_filepath";
	
	public static final int BT_SOCKET_TIMEOUT = 30000;
	public static final int BT_SOCKET_CONNECT_TIMEOUT = 120000;
	
    public static final int HEADER_LEN = 144;
    public static final int BUFFER_MAX_SIZE = 4096;
    
    public static final int HEADER_SECTION_COUNT = 7;
    
    public static final boolean IS_MULTI_DEVICE_SUPPORTED = false;
    
    // Constants that indicate the current wifi connection type
    public static final int WIFI_CONNECTED_NONE 				= 0;       	  // nothing
    public static final int WIFI_CONNECTED_SERVER 				= 1;     	  // mobile.easyserver.server connections
    public static final int WIFI_CONNECTED_CLIENT 				= 2; 		  // client connection
	
    // Handler message types
    public static final int MSG_WIFI_DISCOVER_PEER_FAILED 		= 20;
    public static final int MSG_WIFI_DISCOVER_PEER_SUCCESS 		= 21;
    public static final int MSG_WIFI_DISCOVER_STOP_FAILED		= 22;
    public static final int MSG_WIFI_CONNECT_FAILED 			= 23;
    public static final int MSG_WIFI_CONNECT_SUCCESS 			= 24;
    public static final int MSG_WIFI_CONNECTED 					= 25;

	public static byte[] makeupInfos(String filename, String fileType, long size,
			int isFile, String action, String saveFolder, int notiState) {
		int headerLen = HEADER_LEN - 4;
		byte[] data = new byte[headerLen];
		if (action == null || action.length() == 0) {
			action = " ";
		}
		if (saveFolder == null || saveFolder.length() == 0) {
			saveFolder = " ";
		}
		String infos = filename + ":" + fileType + ":" + size + ":" + isFile
				+ ":" + action + ":" + saveFolder + ":" + notiState;
		int len = infos.getBytes().length;
		ALog.i(TAG, "makeupInfos infos:" + infos + ", len:" + len);
		if (len == headerLen) {
			data = infos.getBytes();
		} else if (len < headerLen) {
			int sub = headerLen - len;
			byte[] temp = infos.getBytes();
			for (int N = 0; N < headerLen; N++) {
				if (N < sub) {
					data[N] = (byte) '*';
				} else {
					data[N] = temp[N - sub];
				}
			}
		} else {
			int sub = len - headerLen;
			infos = infos.substring(sub);
			data = infos.getBytes();
		}
		
		byte[] head = new byte[HEADER_LEN];
		head[0] =  (byte) '*';
		head[1] =  (byte) '$';
		head[2] =  (byte) '*';
		head[3] =  (byte) '$';
		for (int i = 0; i < data.length; i++) {
			head[i + 4] = data[i];
		}
		return head;
	}
    
    // bytes to int
 	public static int bytesToInt(byte[] bytes) {
 		int addr = bytes[0] & 0xFF;
 		addr |= ((bytes[1] << 8) & 0xFF00);
 		addr |= ((bytes[2] << 16) & 0xFF0000);
 		addr |= ((bytes[3] << 24) & 0xFF000000);
 		return addr;
 	}

 	// int --> byte[]
 	public static byte[] int2Bytes(int n) {
 		byte[] targets = new byte[4];
 		targets[0] = (byte) (n & 0xff);				// 最低位
 		targets[1] = (byte) ((n >> 8) & 0xff);		// 次低位
 		targets[2] = (byte) ((n >> 16) & 0xff);		// 次高位
 		targets[3] = (byte) (n >>> 24);				// 最高位,无符号右移。
 		return targets;
 	}
 	
 	public static int getCurrentSDKVersion() {
 		return Build.VERSION.SDK_INT;
 	}
 	
 	public static boolean isSupportMultiDevice() {
    	return Utilities.IS_MULTI_DEVICE_SUPPORTED;
    }
 	
 	public static boolean isWifiP2pSupported() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public static String getFileSizeWithSuffix(long size){
 		String suffix=" B";
 		long value=size;
 		if(size>=1024&&size<=1024*1024){
 			suffix=" KB";
 			value=value/(1024);
 		}else if(size>=1024*1024&&size<=1024*1024*1024){
 			suffix=" MB";
 			value=value/(1024*1024);
 		}else if(size>=1024*1024*1024){
 			suffix=" G";
 			value=value/(1024*1024*1024);
 		}
 		
 		return String.valueOf(value)+suffix;
 	}
 	
 	
}
