package org.ecjtu.channellibrary.wifiutils;


import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by KerriGan on 2016/4/12.
 */
public class WifiAutoConnect implements WifiReceiver.IReceiveNewNetWorks{

    private static final String TAG="WifiAutoConnect";

    private Context _context;

    private WifiManager _wifiManager;

    private WifiConfiguration _wifiConfiguration;

    private ArrayList<String> _wifiSSIDList;

    private int _connectState;

    public static final int STATE_DISABLE=0x10;

    public static final int STATE_ENABLE=0x11;

    public static final int STATE_TIME_OUT=0x12;

    public static final int STATE_CONNECTED=0x13;

    private WifiReceiver _wifiReceiver;

    private IConnect _connectListener;

    public static String BaseSSID ="SyncSync";

    public static String BaseCode ="SyncSync";

    private String _code="";

    public WifiAutoConnect(Context context)
    {
        _context=context;
        _connectState =STATE_DISABLE;
    }

    public void setConnectCode(String code)
    {
        _code=code;
    }

    public void startScan()
    {
        _wifiManager= (WifiManager) _context.getSystemService(Context.WIFI_SERVICE);
        _wifiReceiver=new WifiReceiver(_wifiManager,this);
        _wifiManager.disconnect();
        if(!_wifiManager.isWifiEnabled())
            _wifiManager.setWifiEnabled(true);


        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        IntentFilter filter=new IntentFilter("wifi_scan_available");
//        filter.addAction("wifi_scan_available");
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        _context.registerReceiver(_wifiReceiver,filter);

    }

    public void stopScan()
    {
        if(_wifiReceiver!=null)
        {
            try
            {
                _context.unregisterReceiver(_wifiReceiver);
                _wifiReceiver=null;
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void setConnectListener(IConnect listener)
    {
        _connectListener=listener;
    }

    public void onReceiveNewNetworks(ArrayList<ScanResult> wifiList)
    {
        if(wifiList==null)
        {
            if(_connectListener!=null)
                _connectListener.onConnect(STATE_TIME_OUT);
        }



        _wifiSSIDList =new ArrayList<>();

        for(ScanResult res:wifiList)
        {
            Log.i(TAG,"WifiAutoConnect 搜索到Wifi SSID:" + res.SSID);
            if(res.SSID.contains(WifiAutoConnect.BaseSSID))
                _wifiSSIDList.add(res.SSID);
        }
        synchronized (this)
        {
            if(_wifiSSIDList.size()<=0)
            {
//                if(connectToHotSpot(BaseSSID,BaseCode+_code))
//                    _connectState=STATE_ENABLE;

                if(_connectState==STATE_ENABLE &&
                        isWifiAvailable())
                {
                    Log.i(TAG, "Wifi 连接成功！");
                    _connectState=STATE_CONNECTED;
                }

            }
            else
            {
                for(int i=0;i<_wifiSSIDList.size();i++)
                {
                    if(connectToHotSpot(_wifiSSIDList.get(i), BaseCode +_code))
                    {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }

                        _connectState=STATE_ENABLE;
                        if(isWifiAvailable())
                        {
                            Log.i(TAG,"Wifi 连接成功！");
                            _connectState =STATE_CONNECTED;
                            break;
                        }
                        _connectState=STATE_DISABLE;
                    }
                }
            }

            Log.i(TAG,"Wifi State "+_connectState);
            if(_connectListener!=null)
                _connectListener.onConnect(_connectState);
        }
    }

    public boolean connectToHotSpot(String ssid, String pwd)
    {

        _wifiConfiguration=this.setWifiParams(ssid,pwd);

        int wcgID=_wifiManager.addNetwork(_wifiConfiguration);

        return _wifiManager.enableNetwork(wcgID,true);
    }

    public WifiConfiguration setWifiParams(String ssid,String password)
    {
        WifiConfiguration apConfig=new WifiConfiguration();
        apConfig.SSID="\""+ssid+"\"";
        apConfig.preSharedKey="\""+password+"\"";
//        apConfig.SSID=ssid;
//        apConfig.preSharedKey=password;

        apConfig.hiddenSSID=true;
        apConfig.status=WifiConfiguration.Status.ENABLED;

        apConfig.allowedAuthAlgorithms.clear();
        apConfig.allowedGroupCiphers.clear();
        apConfig.allowedKeyManagement.clear();
        apConfig.allowedPairwiseCiphers.clear();
        apConfig.allowedProtocols.clear();

        try {
            Class c= null;
            c = Class.forName("android.net.wifi.WifiConfiguration$KeyMgmt");
            Field f=c.getDeclaredField("WPA2_PSK");

            int n=f.getInt(f);

            apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//            apConfig.allowedKeyManagement.set(n);

            apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return apConfig;
    }


    @Override
    public void onReceive(ArrayList<ScanResult> list)
    {
        if(_connectState!=STATE_CONNECTED)
            onReceiveNewNetworks(list);
    }

    public boolean isWifiAvailable()
    {
//        return _wifiManager.getConnectionInfo().getSSID().startsWith("\""+BaseSSID+"\"");
        return _wifiManager.getConnectionInfo().getSSID().startsWith("\""+ BaseSSID);
    }

    public interface IConnect
    {
        void onConnect(int state);
    }
}
