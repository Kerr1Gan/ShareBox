package org.ecjtu.channellibrary.wifiutil;

/**
 * Created by KerriGan on 2016/4/4.
 */

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 判断网络状态的工具类
 */
public class NetworkUtil {

    /* 代码IP */
    private static String PROXY_IP = null;
    /* 代理端口 */
    private static int PROXY_PORT = 0;

    private static String NetworkUtil = "NetworkUtil";

    /**
     * 判断当前是否有网络连接
     *
     * @param context
     * @return
     */
    public static boolean isNetwork(Context context) {
        boolean network = isWifi(context);
        boolean mobilework = isMobile(context);
        if (!network && !mobilework) { // 无网络连接
            Log.i(NetworkUtil, " 无网路链接！");
            return false;
        } else if (network == true && mobilework == false) { // wifi连接
            Log.i(NetworkUtil, "wifi连接");
        } else { // 网络连接
            Log.i(NetworkUtil, "手机网路连接，读取代理信息！");
            readProxy(context); // 读取代理信息
            return true;
        }
        return true;
    }

    /**
     * 读取网络代理
     *
     * @param context
     */
    private static void readProxy(Context context) {
        Uri uri = Uri.parse("content://telephony/carriers/preferapn");
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            PROXY_IP = cursor.getString(cursor.getColumnIndex("proxy"));
            PROXY_PORT = cursor.getInt(cursor.getColumnIndex("port"));
        }
        cursor.close();
    }

    /**
     * 判断当前网络是否是wifi局域网
     *
     * @param context
     * @return
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (info != null) {
            return info.isConnected(); // 返回网络连接状态
        }
        return false;
    }

    /**
     * 判断当前网络是否是手机网络
     *
     * @param context
     * @return
     */
    public static boolean isMobile(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (info != null) {
            return info.isConnected(); // 返回网络连接状态
        }
        return false;
    }

    public static boolean isHotSpot(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static WifiConfiguration getHotSpotConfiguration(Context context) {
        return WifiUtil.getWifiApConfiguration(context);
    }

    public static WifiInfo getConnectWifiInfo(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getConnectionInfo();
    }

    public static String getConnectWifiNameV2(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return "";
        }
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        String wifiName = wifiInfo.getExtraInfo();
        if (wifiName == null) {
            return "";
        }
        if (wifiName.startsWith("\"")) {
            wifiName = wifiName.substring(1, wifiName.length());
        }
        if (wifiName.endsWith("\"")) {
            wifiName = wifiName.substring(0, wifiName.length() - 1);
        }
        return wifiName;
    }


    /**
     * result[0] is self ip,result[1] is host ip,result[2] is isWifiEnable,true or false.
     */
    public static ArrayList<String> getWifiHostAndSelfIP(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String isWifiEnable;
        if (!wifiManager.isWifiEnabled()) {
            isWifiEnable = "false";
        } else
            isWifiEnable = "true";
        ArrayList<String> result = new ArrayList<>();
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String IPAddress = intToIp(wifiInfo.getIpAddress());
        result.add(IPAddress);

        DhcpInfo dhcpinfo = wifiManager.getDhcpInfo();
        String serverAddress = intToIp(dhcpinfo.serverAddress);
        result.add(serverAddress);

        result.add(isWifiEnable);
        return result;
    }

    /**
     * 通过左移位操作（<<）给每一段的数字加权
     * 第一段的权为2的24次方
     * 第二段的权为2的16次方
     * 第三段的权为2的8次方
     * 最后一段的权为1
     *
     * @param ip
     * @return int
     */
    public static int ipToInt(String ip) {
        String[] ips = ip.split("\\."); //正则表达式
        return (Integer.parseInt(ips[0]) << 24) + (Integer.parseInt(ips[1]) << 16)
                + (Integer.parseInt(ips[2]) << 8) + Integer.parseInt(ips[3]);
    }

    /**
     * 将整数值进行右移位操作（>>）
     * 右移24位，右移时高位补0，得到的数字即为第一段IP
     * 右移16位，右移时高位补0，得到的数字即为第二段IP
     * 右移8位，右移时高位补0，得到的数字即为第三段IP
     * 最后一段的为第四段IP
     *
     * @param i
     * @return String
     */
    public static String intToIp(int i) {
//        return ((i >> 24) & 0xFF) + "." + ((i >> 16) & 0xFF) + "."
//                + ((i >> 8) & 0xFF) + "." + (i & 0xFF);
        return ((i >> 0) & 0xFF) + "." + ((i >> 8) & 0xFF) + "."
                + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

    //byte 数组与 int 的相互转换
    public static int byteArrayToInt(byte[] b) {
        return b[0] & 0xFF |
                (b[1] & 0xFF) << 8 |
                (b[2] & 0xFF) << 16 |
                (b[3] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    public static String[] getLocalWLANIps() {
        return getIpFromInterface("wlan");
    }

    public static String[] getLocalApIps() {
        return getIpFromInterface("ap");
    }

    public static String[] getIpFromInterface(String name) {
        List<String> result = new ArrayList<>();
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();

                    String iface = intf.getName();
                    if (iface.contains(name)) {
                        if (inetAddress instanceof Inet4Address && isUsableAddress(inetAddress)) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
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
        for (int i = 0; i < ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i] & 0xFF;
        }
        return ipAddrStr;
    }

    private final static boolean isUsableAddress(InetAddress addr) {

        if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()) {
            return false;
        }

        return true;
    }
}
