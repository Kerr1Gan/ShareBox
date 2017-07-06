package org.ecjtu.channellibrary.wifiutil;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Process;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

/**
 * Created by KerriGan on 2016/4/12.
 */
public class WifiUtil {

    private static final String TAG="WifiUtil";
    public static ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    "/proc/net/arp"));
            String line;
            System.out.println("当前热点情况");
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        connectedIP.remove(0);
        return connectedIP;
    }

    public static final String DEVICE_P2P="p2p";
    public static final String DEVICE_WLAN="wlan";

    public static ArrayList<String> nativeGetConnectedIP() {
        return nativeGetConnectedIP(DEVICE_WLAN);
    }

    public static ArrayList<String> nativeGetConnectedIP(String deviceName) {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            Process local=Runtime.getRuntime().exec("cat /proc/net/arp");

            DataInputStream os=new DataInputStream(local.getInputStream());

            String line;
            Log.i(TAG,"arp begin");
            while ((line = os.readLine()) != null) {
                Log.i(TAG, line);
                String[] splited = line.split("\\s+");
                if (splited != null && splited.length > 5) {
                    if(!splited[3].equals("00:00:00:00:00:00")&&splited[5].contains(deviceName)){
                        String ip = splited[0];
                        connectedIP.add(ip);
                    }
                }
            }
            Log.i(TAG,"arp end");
        } catch (IOException e) {
            e.printStackTrace();
        }
//        connectedIP.remove(0);
        return connectedIP;
    }

//    public static ArrayList<String> nativeGetConnectedIPDynamic() {
//        ArrayList<String> connectedIP = new ArrayList<String>();
//        try {
//            Process local=Runtime.getRuntime().exec("arp -v");
//
//            DataInputStream os=new DataInputStream(local.getInputStream());
//
//            String line;
//            while ((line = os.readLine()) != null) {
//                System.out.println(line);
//                String[] splitted = line.split(" +");
//                if (splitted != null && splitted.length >= 4) {
//                    String ip = splitted[0];
//                    connectedIP.add(ip);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return connectedIP;
//    }



    /**
     *  must not be used in org.ecjtu.share.ui thread.
     */
    public static boolean nativeIsApAlive(String ip)
    {
        try {
            Process local=Runtime.getRuntime().exec(("/system/bin/ping "+ip));

            DataInputStream os=new DataInputStream(local.getInputStream());

            int index=0;
            String line;
            while ((line = os.readLine()) != null) {
                System.out.println(line);
                if(line.contains("Unreachable"))
                {
                    os.close();
                    return false;
                }
                if(index>=2)
                {
                    os.close();
                    return true;
                }
                index++;
            }
            os.close();


            /**
             *  samsung device remove ping functions .2016/4/17
             */
//            Socket socket=new Socket();
//            SocketAddress addr=new InetSocketAddress(ip,12500);
//            socket.connect(addr,400);
//            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }




    public static boolean openHotSpot(WifiManager wifiManager, boolean isOpen, String hotName, String password)
    {
        if(wifiManager.isWifiEnabled()&&isOpen)
            wifiManager.setWifiEnabled(false);

        if(hotName.length()<=0 || password.length()<=0)
        {
            hotName=WifiAutoConnect.BaseSSID;
            password=WifiAutoConnect.BaseCode;
        }


        try {
            WifiConfiguration apConfig=new WifiConfiguration();

//            WifiConfiguration apConfig=getWifiApConfiguration(wifiManager);

            //wifi name
//            apConfig.SSID="\""+hotName+"\"";
            apConfig.SSID=hotName;

            //password
//            apConfig.preSharedKey="\""+password+"\"";
            apConfig.preSharedKey=password;

            //allowed password
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

            Class c=Class.forName("android.net.wifi.WifiConfiguration$KeyMgmt");

            Field f=c.getDeclaredField("WPA2_PSK");

            int n=f.getInt(f);

            apConfig.allowedAuthAlgorithms.clear();
            apConfig.allowedGroupCiphers.clear();
            apConfig.allowedKeyManagement.clear();
            apConfig.allowedPairwiseCiphers.clear();
            apConfig.allowedProtocols.clear();
            apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            apConfig.allowedKeyManagement.set(n);
            apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);


            Method method=wifiManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class,Boolean.TYPE);

            return (Boolean)method.invoke(wifiManager,apConfig,isOpen);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean openHotSpot(Context context, boolean isOpen, String hotName, String password)
    {
        WifiManager wifiManager= (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return openHotSpot(wifiManager,isOpen,hotName,password);
    }


    public static void disConnectWifi(Context context)
    {
        WifiManager manager= (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        manager.setWifiEnabled(false);
    }


    private static ServerSocket _aliveServerSocket=null;

    @Deprecated
    public static synchronized void startAliveServer(String ip)
    {
        if(_aliveServerSocket!=null)
            return;
        final String fIp=ip;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    try {
                        _aliveServerSocket=new ServerSocket();
                        SocketAddress addr=new InetSocketAddress(fIp,12500);
                        _aliveServerSocket.bind(addr, 5);
                        Socket socket=_aliveServerSocket.accept();

                        socket.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Deprecated
    public static synchronized void stopAliveServer()
    {
        if(_aliveServerSocket!=null)
        {
            try {
                _aliveServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    /**
     * Return whether Wi-Fi AP is enabled or disabled.
     * @return {@code true} if Wi-Fi AP is enabled
     *
     * @hide Dont open yet
     */
    public static boolean isWifiApEnabled(Context context) {
        Method method = null;
        WifiManager manager= (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try {
            method = manager.getClass().getMethod("getWifiApState");

            int tmp = ((Integer) method.invoke(manager));

            int res=manager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED").getInt(manager);

            return tmp==res;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String getWifiSSID(Context context)
    {
        WifiManager manager= (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if(!manager.isWifiEnabled())
            return null;

        return manager.getConnectionInfo().getSSID();
    }

    /**
     * Gets the Wi-Fi AP Configuration.
     * @return AP details in {@link WifiConfiguration}
     */
    public static WifiConfiguration getWifiApConfiguration(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");

            return (WifiConfiguration) method.invoke(wifiManager);
        } catch (Exception e) {
//            Log.e(this.getClass().toString(), "", e);
            return null;
        }
    }

    public static WifiConfiguration getWifiApConfiguration(Context context) {
        WifiManager manager= (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return getWifiApConfiguration(manager);
    }
    /**
     * Sets the Wi-Fi AP Configuration.
     * @return {@code true} if the operation succeeded, {@code false} otherwise
     */
    public static boolean setWifiApConfiguration(WifiManager wifiManager,WifiConfiguration wifiConfig) {
        try {
            Method method = wifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            return (Boolean) method.invoke(wifiManager, wifiConfig);
        } catch (Exception e) {
//            Log.e(this.getClass().toString(), "", e);
            return false;
        }
    }

    public static void getNearWifiList(final Context context, final WifiReceiver.IReceiveNewNetWorks listener)
    {
        WifiManager manager= (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        final WifiReceiver receiver=new WifiReceiver(manager);

        WifiReceiver.IReceiveNewNetWorks wListener=new WifiReceiver.IReceiveNewNetWorks() {

            WifiReceiver _receiver=receiver;

            @Override
            public void onReceive(ArrayList<ScanResult> list) {

                listener.onReceive(list);

                if(_receiver!=null)
                {
                    try
                    {
                        context.unregisterReceiver(_receiver);
                        _receiver=null;
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        };

        receiver.setReceiveListener(wListener);

        manager.disconnect();

        if(!manager.isWifiEnabled())
          manager.setWifiEnabled(true);

        IntentFilter filter=new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);

        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        context.registerReceiver(receiver,filter);
    }

    public static boolean connectWifi(Context context,String ssid,String pwd)
    {
        WifiManager manager= (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiConfiguration wifiConfiguration=new WifiConfiguration();
        wifiConfiguration.SSID="\""+ssid+"\"";
        wifiConfiguration.preSharedKey="\""+pwd+"\"";

        wifiConfiguration.hiddenSSID=true;
        wifiConfiguration.status=WifiConfiguration.Status.ENABLED;

        wifiConfiguration.allowedAuthAlgorithms.clear();
        wifiConfiguration.allowedGroupCiphers.clear();
        wifiConfiguration.allowedKeyManagement.clear();
        wifiConfiguration.allowedPairwiseCiphers.clear();
        wifiConfiguration.allowedProtocols.clear();

        try {
            Class c= null;
            c = Class.forName("android.net.wifi.WifiConfiguration$KeyMgmt");
            Field f=c.getDeclaredField("WPA2_PSK");

            wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

//            wifiConfiguration.allowedKeyManagement.set(f.getInt(wifiConfiguration));
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

            int wcgId=manager.addNetwork(wifiConfiguration);

            return manager.enableNetwork(wcgId, true);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String setupWifiDataProtocol(String apName,String pwd){
        return String.format("WIFI:T:WPA;P:\"%s\";S:%s;",pwd,apName);
    }
}
