package org.ecjtu.channellibrary.wifiutil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import java.net.InetAddress;

/**
 * Created by KerriGan on 2016/8/16.
 */
public class P2pReceiver extends BroadcastReceiver{

    private WifiP2pManager _wifiP2pManager;

    private WifiP2pManager.Channel _channel;


    public P2pReceiver(WifiP2pManager wifiP2pManager,WifiP2pManager.Channel channel)
    {
        _wifiP2pManager=wifiP2pManager;
        _channel=channel;
    }


    private WifiP2pManager.PeerListListener _peerListener=new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {

        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        switch (action)
        {
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                int state=intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);

                if(state== WifiManager.WIFI_STATE_ENABLED)
                {
                    //p2p is enable
                }
                else
                {
                    //p2p is not useful
                }

                break;

            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                _wifiP2pManager.requestPeers(_channel,_peerListener);
                break;

            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:

                break;

            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                break;
        }
    }

    public void discoverPeers()
    {
        _wifiP2pManager.discoverPeers(_channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    public void connect(WifiP2pDevice device)
    {
        WifiP2pConfig config=new WifiP2pConfig();

        config.deviceAddress=device.deviceAddress;
        config.wps.setup= WpsInfo.PBC;

        _wifiP2pManager.connect(_channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    public void getConnectionInfo(Intent intent)
    {
        NetworkInfo info=intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        if(info.isConnected())
        {
            _wifiP2pManager.requestConnectionInfo(_channel, new WifiP2pManager.ConnectionInfoListener() {
                @Override
                public void onConnectionInfoAvailable(WifiP2pInfo info) {
                    // 这里可以查看变化后的网络信息

                    // 通过传递进来的WifiP2pInfo参数获取变化后的地址信息
                    InetAddress groupOwnerAddress = info.groupOwnerAddress;
                    // 通过协商，决定一个小组的组长
                    if (info.groupFormed && info.isGroupOwner) {
                        // 这里执行P2P小组组长的任务。
                        // 通常是创建一个服务线程来监听客户端的请求
                    } else if (info.groupFormed) {
                        // 这里执行普通组员的任务
                        // 通常是创建一个客户端向组长的服务器发送请求
                    }
                }
            });
        }
    }
}

