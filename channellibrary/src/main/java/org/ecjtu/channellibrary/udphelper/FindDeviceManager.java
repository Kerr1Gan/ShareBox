package org.ecjtu.channellibrary.udphelper;

import android.util.Log;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by Ethan_Xiang on 2017/10/24.
 */

public class FindDeviceManager {

    private static final String TAG = "FindDeviceManager";

    private static final int UDP_PORT = 1111;

    private static final int RECEIVE_TIME_OUT = 2500;

    private volatile byte[] mBroadcastData;

    private boolean mHidden = false;

    private DatagramSocket mBroadcastSocket;

    private DatagramSocket mReceiveSocket;

    private Runnable mBroadcastWorker = new Runnable() {

        @Override
        public void run() {
            Log.i(TAG, "run: broadcast worker begin");
            while (!Thread.interrupted()) {
                try {
                    mBroadcastSocket = new DatagramSocket();
                    mBroadcastSocket.setSoTimeout(RECEIVE_TIME_OUT);

                    byte[] sendData = new byte[1];
                    InetAddress broadIP = InetAddress.getByName("255.255.255.255");// 255.255.255.255 会发送给局域网内所有设备 https://segmentfault.com/q/1010000004918877
                    DatagramPacket sendPack = new DatagramPacket(sendData, sendData.length, broadIP, UDP_PORT);
                    if (mBroadcastData != null) {
                        byte[] local = new byte[mBroadcastData.length];
                        System.arraycopy(mBroadcastData, 0, local, 0, mBroadcastData.length);
                        sendPack.setData(local);
                        mBroadcastSocket.send(sendPack);
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    if (e instanceof InterruptedIOException) {
                        Thread.currentThread().interrupt();
                    }
                }

                if (Thread.interrupted()) {
                    break;
                }

                try {
                    wait(RECEIVE_TIME_OUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
            Log.i(TAG, "run: broadcast worker end");
        }

    };

    private Runnable mReceiveWorker = new Runnable() {

        @Override
        public void run() {
            Log.i(TAG, "run: receive worker begin");
            while (!Thread.interrupted()) {
                try {
                    mReceiveSocket = new DatagramSocket(UDP_PORT);
                    byte[] data = new byte[1024 * 10];
                    DatagramPacket pack = new DatagramPacket(data, data.length);
                    mReceiveSocket.receive(pack);
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    if (e instanceof InterruptedIOException) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            Log.i(TAG, "run: receive worker end");
        }
    };

    private Thread mBroadcastThread;

    private Thread mReceiveThread;

    public FindDeviceManager() {
    }

    public void start() {
        if (mBroadcastThread == null && !isHide()) {
            mBroadcastThread = new Thread(mBroadcastWorker);
            mBroadcastThread.start();
        }
        if (mReceiveThread == null) {
            mReceiveThread = new Thread(mReceiveWorker);
            mReceiveThread.start();
        }
    }

    public void stop() {
        if (mBroadcastThread != null) {
            mBroadcastThread.interrupt();
            if (mBroadcastSocket != null) {
                mBroadcastSocket.close();
            }
            try {
                mBroadcastThread.join(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                mBroadcastThread.interrupt();
            }
        }
        if (mReceiveThread != null) {
            mReceiveThread.interrupt();
            if (mReceiveSocket != null) {
                mReceiveSocket.close();
            }
            try {
                mReceiveThread.join(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                mReceiveThread.interrupt();
            }
        }

        mBroadcastThread = null;
        mReceiveThread = null;
    }

    public void stopAsync() {
        if (mBroadcastThread != null) {
            mBroadcastThread.interrupt();
            if (mBroadcastSocket != null) {
                mBroadcastSocket.close();
            }
        }
        if (mReceiveThread != null) {
            mReceiveThread.interrupt();
            if (mReceiveSocket != null) {
                mReceiveSocket.close();
            }
        }
        mBroadcastThread = null;
        mReceiveThread = null;
    }

    public void setBrocastData(byte[] data) {
        mBroadcastData = data;
    }

    public void hide(boolean hide) {
        mHidden = hide;
    }

    public boolean isHide(){
        return mHidden;
    }
}
