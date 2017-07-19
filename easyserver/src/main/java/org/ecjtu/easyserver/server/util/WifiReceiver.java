package org.ecjtu.easyserver.server.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;


/**
 * Created by KerriGan on 2016/4/12.
 */
public class WifiReceiver extends BroadcastReceiver{


    private static final String TAG="WifiReceiver";

    private ArrayList<ScanResult> _wifiList;

    private IReceiveNewNetWorks _listener;

    private WifiManager _wifiManager;

    public WifiReceiver(WifiManager manager, IReceiveNewNetWorks listener)
    {
        _wifiManager=manager;
        _listener=listener;
    }

    public WifiReceiver(WifiManager manager)
    {
        _wifiManager=manager;
    }

    public void setReceiveListener(IReceiveNewNetWorks listener)
    {
        _listener=listener;
    }


    private boolean _isConnecting=false;

    @Override
    public void onReceive(Context context, Intent intent) {

        if(_isConnecting)
            return;

        _isConnecting=true;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            if(_wifiManager.isScanAlwaysAvailable()&&_wifiManager.getScanResults().size()>0)
            {
                _wifiList= (ArrayList<ScanResult>) _wifiManager.getScanResults();

                if(_listener!=null)
                {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            _listener.onReceive(_wifiList);
                            _isConnecting=false;
                            abortBroadcast();
                        }
                    }).start();
                }
            }
            else
            {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long startTime= System.currentTimeMillis();
                        Log.i(TAG,"AutoConnect线程开始");
                        while(true)
                        {
                            if(System.currentTimeMillis()-startTime>8*1000)
                            {
                                Log.i(TAG,"Wifi 连接超时");
                                break;
                            }

                            _wifiList= (ArrayList<ScanResult>) _wifiManager.getScanResults();

                            if(_wifiList.size()>0)
                            {
                                if(_listener!=null)
                                    _listener.onReceive(_wifiList);
                                return;
                            }

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if(_listener!=null)
                            _listener.onReceive(null);
                        _isConnecting=false;
                        Log.i(TAG, "AutoConnect线程结束");
                    }
                }).start();
            }
        }
        else
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long startTime= System.currentTimeMillis();
                    Log.i(TAG, "AutoConnect线程开始");
                    while(true)
                    {
                        if(System.currentTimeMillis()-startTime>8*1000)
                        {
                            Log.i(TAG, "Wifi 连接超时");
                            break;
                        }

                        _wifiList= (ArrayList<ScanResult>) _wifiManager.getScanResults();

                        if(_wifiList.size()>0)
                        {
                            if(_listener!=null)
                                _listener.onReceive(_wifiList);
                            abortBroadcast();
                            return;
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if(_listener!=null)
                        _listener.onReceive(null);
                    _isConnecting=false;
                    Log.i(TAG, "AutoConnect线程结束");
                }
            }).start();
        }

    }

    public interface IReceiveNewNetWorks
    {
        void onReceive(ArrayList<ScanResult> list);
    }
}
