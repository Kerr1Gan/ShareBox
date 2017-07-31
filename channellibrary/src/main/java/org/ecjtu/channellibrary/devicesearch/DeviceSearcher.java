package org.ecjtu.channellibrary.devicesearch;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by KerriGan on 2017/5/31.
 */

public abstract class DeviceSearcher extends Thread{
    private static final String TAG = "DeviceSearcher";

    private static final int DEVICE_FIND_PORT = 9000;
    private static final int RECEIVE_TIME_OUT = 1500; // 接收超时时间
    private static final int RESPONSE_DEVICE_MAX = 200; // 响应设备的最大个数，防止UDP广播攻击

    private static final byte PACKET_TYPE_FIND_DEVICE_REQ_10 = 0x10; // 搜索请求
    private static final byte PACKET_TYPE_FIND_DEVICE_RSP_11 = 0x11; // 搜索响应
    private static final byte PACKET_TYPE_FIND_DEVICE_CHK_12 = 0x12; // 搜索确认

    private static final byte PACKET_DATA_TYPE_DEVICE_NAME_20 = 0x20;
    private static final byte PACKET_DATA_TYPE_DEVICE_ROOM_21 = 0x21;

    private DatagramSocket mHostSocket;
    private Set<DeviceBean> mDeviceSet;

    private byte mPackType;
    private String mDeviceIP;

    private String mPort="";
    public DeviceSearcher() {
        mDeviceSet = new HashSet<>();
    }

    public DeviceSearcher(String port){
        this();
        mPort=port;
    }


    @Override
    public void run() {
        try {
            onSearchStart();
            mHostSocket = new DatagramSocket();
            // 设置接收超时时间
            mHostSocket.setSoTimeout(RECEIVE_TIME_OUT);

            byte[] sendData = new byte[1024];
            InetAddress broadIP = InetAddress.getByName("255.255.255.255");//255.255.255.255 会发送给局域网内所有设备 https://segmentfault.com/q/1010000004918877
            DatagramPacket sendPack = new DatagramPacket(sendData, sendData.length, broadIP, DEVICE_FIND_PORT);

            for (int i = 0; i < 3; i++) {
                // 发送搜索广播
                mPackType = PACKET_TYPE_FIND_DEVICE_REQ_10;
                sendPack.setData(packData(i + 1));
                Log.i(TAG, String.format("%s: 发送搜索广播",TAG));
                mHostSocket.send(sendPack);

                // 监听来信
                byte[] receData = new byte[1024];
                DatagramPacket recePack = new DatagramPacket(receData, receData.length);
                try {
                    // 最多接收200个，或超时跳出循环
                    int rspCount = RESPONSE_DEVICE_MAX;
                    while (rspCount-- > 0) {
                        recePack.setData(receData);
                        mHostSocket.receive(recePack);
                        if (recePack.getLength() > 0) {
                            mDeviceIP = recePack.getAddress().getHostAddress();
                            if (parsePack(recePack)) {
                                Log.i(TAG, String.format("%s: 设备上线：%s",TAG,mDeviceIP));
                                // 发送一对一的确认信息。使用接收报，因为接收报中有对方的实际IP，发送报时广播IP
                                mPackType = PACKET_TYPE_FIND_DEVICE_CHK_12;
                                recePack.setData(packData(rspCount)); // 注意：设置数据的同时，把recePack.getLength()也改变了
                                mHostSocket.send(recePack);
                            }
                        }
                    }
                } catch (SocketTimeoutException e) {
                }
                Log.i(TAG, String.format("%s: 结束搜索 %d",TAG,i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            onSearchFinish(mDeviceSet);
            if (mHostSocket != null) {
                mHostSocket.close();
            }
        }

    }

    /**
     * 搜索开始时执行
     */
    public abstract void onSearchStart();

    /**
     * 搜索结束后执行
     * @param deviceSet 搜索到的设备集合
     */
    public abstract void onSearchFinish(Set<DeviceBean> deviceSet);

    /**
     * 解析报文
     * 协议：$ + packType(1) + data(n)
     *  data: 由n组数据，每组的组成结构type(1) + length(4) + data(length)
     *  type类型中包含name、room类型，但name必须在最前面
     */
    private boolean parsePack(DatagramPacket pack) {
        if (pack == null || pack.getAddress() == null) {
            return false;
        }

        String ip = pack.getAddress().getHostAddress();
        int port = pack.getPort();
        for (DeviceBean d : mDeviceSet) {
            if (d.getIp().equals(ip)) {
                return false;
            }
        }
        int dataLen = pack.getLength();
        int offset = 0;
        byte packType;
        byte type;
        int len;
        DeviceBean device = null;

        if (dataLen < 2) {
            return false;
        }
        byte[] data = new byte[dataLen];
        System.arraycopy(pack.getData(), pack.getOffset(), data, 0, dataLen);

        if (data[offset++] != '$') {
            return false;
        }

        packType = data[offset++];
        if (packType != PACKET_TYPE_FIND_DEVICE_RSP_11) {
            return false;
        }

        while (offset + 5 < dataLen) {
            type = data[offset++];
            len = data[offset++] & 0xFF;
            len |= (data[offset++] << 8);
            len |= (data[offset++] << 16);
            len |= (data[offset++] << 24);

            if (offset + len > dataLen) {
                break;
            }
            switch (type) {
                case PACKET_DATA_TYPE_DEVICE_NAME_20:
                    String name = new String(data, offset, len, Charset.forName("UTF-8"));
                    device = new DeviceBean();
                    device.setName(name);
                    device.setIp(ip);
                    device.setPort(port);
                    break;
                case PACKET_DATA_TYPE_DEVICE_ROOM_21:
                    String room = new String(data, offset, len, Charset.forName("UTF-8"));
                    if (device != null) {
                        device.setRoom(room);
                    }
                    break;
                default: break;
            }
            offset += len;
        }
        if (device != null) {
            mDeviceSet.add(device);
            return true;
        }
        return false;
    }

    /**
     * 打包搜索报文
     * 协议：$ + packType(1) + sendSeq(4) + [deviceIP(n<=15)]
     *  packType - 报文类型
     *  sendSeq - 发送序列
     *  deviceIP - 设备IP，仅确认时携带
     */
    private byte[] packData(int seq) {
        byte[] data = new byte[1024];
        int offset = 0;

        data[offset++] = '$';

        data[offset++] = mPackType;

        seq = seq == 3 ? 1 : ++seq; // can't use findSeq++
        data[offset++] = (byte) seq;
        data[offset++] = (byte) (seq >> 8 );
        data[offset++] = (byte) (seq >> 16);
        data[offset++] = (byte) (seq >> 24);

        if (mPackType == PACKET_TYPE_FIND_DEVICE_CHK_12) {
            String content=mDeviceIP+","+mPort;
            byte[] ips = content.getBytes(Charset.forName("UTF-8"));
            System.arraycopy(ips, 0, data, offset, ips.length);
            offset += ips.length;
        }

        byte[] result = new byte[offset];
        System.arraycopy(data, 0, result, 0, offset);
        return result;
    }

    @Override
    public void interrupt() {
        if(mHostSocket!=null) mHostSocket.close();
        super.interrupt();
    }

    /**
     * 设备Bean
     * 只要IP一样，则认为是同一个设备
     */
    public static class DeviceBean{
        String ip;      // IP地址
        int port;       // 端口
        String name;    // 设备名称
        String room;    // 设备所在房间

        @Override
        public int hashCode() {
            return ip.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof DeviceBean) {
                return this.ip.equals(((DeviceBean)o).getIp());
            }
            return super.equals(o);
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRoom() {
            return room;
        }

        public void setRoom(String room) {
            this.room = room;
        }


    }
}
