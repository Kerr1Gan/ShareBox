package org.ecjtu.channellibrary.wifiutils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KerriGan on 2016/8/17.
 */
public class P2pConnect {

    private static final String TAG=P2pConnect.class.getSimpleName();

    private Context _context;

    private WifiP2pManager _p2pManager;

    private WifiP2pManager.Channel _channel;

    private P2pReceiver _p2pReceiver;

    private List<WifiP2pDevice> _p2pDeviceList;

    private IReceive _receiveListener;

    public P2pConnect(Context context,WifiP2pManager manager,WifiP2pManager.Channel channel,IReceive listener)
    {
        _context=context;
        _p2pManager=manager;
        _channel=channel;
        _p2pDeviceList=new ArrayList<>();
        _receiveListener=listener;
    }

    public void registerReceiver()
    {
        IntentFilter filter=P2pManager.getInstance().getP2pManagerIntentFilter();
        _context.registerReceiver(_p2pReceiver,filter);
    }

    public void unregisterReceiver()
    {
        _context.unregisterReceiver(_p2pReceiver);
    }


    public P2pReceiver getP2pReceiver()
    {
        return _p2pReceiver;
    }

    public List<WifiP2pDevice> getP2pDeviceList()
    {
        return _p2pDeviceList;
    }


    private WifiP2pManager.PeerListListener _peerListener=new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            for(WifiP2pDevice device:peers.getDeviceList())
            {
                Log.i(TAG,device.toString());
            }
            _p2pDeviceList.clear();
            _p2pDeviceList.addAll(peers.getDeviceList());
        }
    };


    private class P2pReceiver extends BroadcastReceiver
    {
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
                        Log.i(TAG,"WIFI_P2P is enable");
                    }
                    else
                    {
                        //p2p is not useful
                        Log.i(TAG,"WIFI_P2P is not supported");
                    }
                    break;

                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                    _p2pManager.requestPeers(_channel,_peerListener);
                    break;

                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                    break;

                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                    break;
            }

            if(_receiveListener!=null)
                _receiveListener.onReceive(context,intent);
        }
    }

    public interface IReceive
    {
        void onReceive(Context context, Intent intent);
    }
}
