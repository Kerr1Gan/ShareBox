package org.ecjtu.channellibrary.wifiutils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by KerriGan on 2016/8/3.
 */
public class P2pManager {

    private WifiP2pManager _wifiP2pManager =null;

    private static P2pManager _p2pManagerInstance;

    private WifiP2pManager.Channel _channel;

    private P2pManager()
    {
    }


    public static P2pManager initialize(Context context)
    {
        P2pManager manager=getInstance();
        manager._wifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        manager._channel=manager._wifiP2pManager.initialize(context,context.getMainLooper(),null);
        return manager;
    }

    public synchronized static P2pManager getInstance()
    {
        if(_p2pManagerInstance==null)
            _p2pManagerInstance=new P2pManager();
        return _p2pManagerInstance;
    }

    public WifiP2pManager getWifiP2pManager()
    {
        return _wifiP2pManager;
    }

    public WifiP2pManager.Channel getChannel()
    {
        return _channel;
    }

    public IntentFilter getP2pManagerIntentFilter()
    {
        IntentFilter filter=new IntentFilter();

        //WIFI P2P 是否可用
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        //WIFI P2P PEERS列表发生变化
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        //WIFI P2P 连接发生变化
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        //WIFI P2P 设备信息发生变化(设备名更改)
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        return filter;
    }

    public void release()
    {
        _channel=null;
        _wifiP2pManager =null;
        _p2pManagerInstance=null;
    }

    public void discoverPeers(WifiP2pManager.ActionListener listener)
    {
        _wifiP2pManager.discoverPeers(_channel, listener);
    }

    public void connect(WifiP2pDevice device,WifiP2pManager.ActionListener listener)
    {
        WifiP2pConfig config=new WifiP2pConfig();

        config.deviceAddress=device.deviceAddress;
        config.wps.setup= WpsInfo.PBC;

        _wifiP2pManager.connect(_channel, config, listener);
    }

    public void getConnectionInfo(Intent intent,WifiP2pManager.ConnectionInfoListener listener)
    {
        NetworkInfo info=intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        if(info.isConnected())
        {
            _wifiP2pManager.requestConnectionInfo(_channel,listener);
//            _wifiP2pManager.requestConnectionInfo(_channel, new WifiP2pManager.ConnectionInfoListener() {
//                @Override
//                public void onConnectionInfoAvailable(WifiP2pInfo info) {
//                    // 这里可以查看变化后的网络信息
//
//                    // 通过传递进来的WifiP2pInfo参数获取变化后的地址信息
//                    InetAddress groupOwnerAddress = info.groupOwnerAddress;
//                    // 通过协商，决定一个小组的组长
//                    if (info.groupFormed && info.isGroupOwner) {
//                        // 这里执行P2P小组组长的任务。
//                        // 通常是创建一个服务线程来监听客户端的请求
//                    } else if (info.groupFormed) {
//                        // 这里执行普通组员的任务
//                        // 通常是创建一个客户端向组长的服务器发送请求
//                    }
//                }
//            });
        }
    }


}
