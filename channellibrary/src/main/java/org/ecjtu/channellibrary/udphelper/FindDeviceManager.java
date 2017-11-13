package org.ecjtu.channellibrary.udphelper;

import android.util.Log;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Ethan_Xiang on 2017/10/24.
 */

public class FindDeviceManager {

    private static final String TAG = "FindDeviceManager";

    private static final int UDP_PORT = 1111;

    private static final int DEFAULT_TIME_OUT = 2500;

    private volatile byte[] mBroadcastData;

    private boolean mHidden = false;

    private DatagramSocket mBroadcastSocket;

    private DatagramSocket mReceiveSocket;

    private int mPollingInterval = DEFAULT_TIME_OUT;

    private static AtomicInteger sBroadcastThreadsCount = new AtomicInteger();

    private static AtomicInteger sReceiveThreadsCount = new AtomicInteger();

    private Runnable mBroadcastWorker = new Runnable() {

        @Override
        public void run() {
            Log.i(TAG, "run: broadcast worker begin threads count " + sBroadcastThreadsCount.incrementAndGet());
            DatagramSocket localSocket = null;
            try {
                if (mBroadcastSocket == null) {
                    mBroadcastSocket = new DatagramSocket();
                    mBroadcastSocket.setSoTimeout(DEFAULT_TIME_OUT);
                }
                localSocket = mBroadcastSocket;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        byte[] sendData = new byte[1];
                        InetAddress broadIP = InetAddress.getByName("255.255.255.255");// 255.255.255.255 会发送给局域网内所有设备 https://segmentfault.com/q/1010000004918877
                        DatagramPacket sendPack = new DatagramPacket(sendData, sendData.length, broadIP, UDP_PORT);
                        if (mBroadcastData != null) {
                            byte[] local = new byte[mBroadcastData.length];
                            System.arraycopy(mBroadcastData, 0, local, 0, mBroadcastData.length);
                            sendPack.setData(local);
                            localSocket.send(sendPack);
                        }
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (e instanceof InterruptedIOException) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    try {
                        Thread.sleep(mPollingInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
                Log.i(TAG, "run: broadcast bind port failure");
            }
            if (localSocket != null) {
                localSocket.close();
            }
            Log.i(TAG, "run: broadcast worker end threads count " + sBroadcastThreadsCount.decrementAndGet());
        }

    };

    private Runnable mReceiveWorker = new Runnable() {

        @Override
        public void run() {
            Log.i(TAG, "run: receive worker begin threads count " + sReceiveThreadsCount.incrementAndGet());
            DatagramSocket localSocket = null;
            try {
                if (mReceiveSocket == null) {
                    mReceiveSocket = new DatagramSocket(UDP_PORT);
                }
                localSocket = mReceiveSocket;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        byte[] data = new byte[1024 * 10];
                        DatagramPacket pack = new DatagramPacket(data, data.length);
                        localSocket.receive(pack);
                        if (mReceiveListener != null) {
                            int offset = pack.getOffset();
                            int len = pack.getLength();
                            byte[] local = new byte[len];
                            System.arraycopy(pack.getData(), offset, local, 0, len);
                            IReceiveMsg listener = mReceiveListener.get();
                            if (listener != null) {
                                listener.onReceive(pack.getAddress().getHostAddress(), pack.getPort(), local);
                            }
                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (e instanceof InterruptedIOException) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
                Log.i(TAG, "run: receive bind port failure");
            }
            if (localSocket != null) {
                localSocket.close();
            }
            Log.i(TAG, "run: receive worker end threads count " + sReceiveThreadsCount.decrementAndGet());
        }
    };

    private Thread mBroadcastThread;

    private Thread mReceiveThread;

    private WeakReference<IReceiveMsg> mReceiveListener;

    public FindDeviceManager() {
    }

    public FindDeviceManager(byte[] data) {
        mBroadcastData = data;
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
        mBroadcastSocket = null;
        mReceiveSocket = null;
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
        mBroadcastSocket = null;
        mReceiveSocket = null;
    }

    public void setBrodcastData(byte[] data) {
        mBroadcastData = data;
    }

    public void hide(boolean hide) {
        mHidden = hide;
    }

    public boolean isHide() {
        return mHidden;
    }

    public void setReceiveListener(IReceiveMsg listener) {
        mReceiveListener = new WeakReference<>(listener);
    }

    public void setPollingInterval(int millis) {
        mPollingInterval = millis;
    }

    public interface IReceiveMsg {
        void onReceive(String ip, int port, byte[] msg);
    }
}
