package org.ecjtu.easyserver.server.impl.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import org.ecjtu.easyserver.IAidlInterface;
import org.ecjtu.easyserver.R;
import org.ecjtu.easyserver.net.HostInterface;
import org.ecjtu.easyserver.server.Constants;
import org.ecjtu.easyserver.server.DeviceInfo;
import org.ecjtu.easyserver.server.ServerManager;
import org.ecjtu.easyserver.server.impl.server.EasyServer;
import org.ecjtu.easyserver.server.util.WifiUtil;
import org.ecjtu.easyserver.server.util.cache.ServerInfoParcelableHelper;
import org.ecjtu.easyserver.util.StatusBarUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ecjtu.easyserver.server.impl.server.EasyServer.TYPE_AP;
import static org.ecjtu.easyserver.server.impl.server.EasyServer.TYPE_NOTHING;
import static org.ecjtu.easyserver.server.impl.server.EasyServer.TYPE_P2P;


/**
 * Created by KerriGan on 2016/4/24.
 */
public class EasyServerService extends Service implements HostInterface.ICallback {

    private static final String TAG_NAME = "EasyServerService";

    private EasyServer mEasyServer = null;

    private Notification mNotification;

    public boolean isBind;

    private NotificationClickReceiver mReceiver;

    public static final String EXTRA_SERVER_PORT = "extra_server_port";

    public static final String EXTRA_SERVER_IP = "extra_server_ip";

    public static final String EXTRA_SERVER_TYPE = "extra_server_type";

    public static final String EXTRA_SETUP_SERVER = "extra_setup_server";

    public static final int SERVER_TYPE_AP = TYPE_AP;

    public static final int SERVER_TYPE_P2P = TYPE_P2P;

    public static final int SERVER_TYPE_NOT = TYPE_NOTHING;

    public IAidlInterface mBinder = new IAidlInterface.Stub() {

        @Override
        public void startService() throws RemoteException {
        }

        @Override
        public void stopService() throws RemoteException {
        }

        @Override
        public boolean isServerAlive() throws RemoteException {
            return EasyServerService.this.isServerAlive();
        }

        @Override
        public String getIp() throws RemoteException {
            return EasyServerService.this.getIp();
        }

        @Override
        public int getPort() throws RemoteException {
            return EasyServerService.this.getPort();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder.asBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG_NAME, "onCreate");
        isBind = false;
        initNotification();
        initEasyServer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return super.onStartCommand(intent, flags, startId);

        int type = intent.getIntExtra(EXTRA_SERVER_TYPE, SERVER_TYPE_NOT);

        if (type == SERVER_TYPE_AP) {
            if (mEasyServer != null) {
                mEasyServer.interrupt();
            }
            mEasyServer = new EasyServer();
            mEasyServer.setType(TYPE_AP);
            mEasyServer.start();
            mEasyServer.setServerListener(this);
        } else if (type == SERVER_TYPE_P2P) {
            String ip = intent.getStringExtra(EXTRA_SERVER_IP);
            int port = intent.getIntExtra(EXTRA_SERVER_PORT, -1);

            if (!TextUtils.isEmpty(ip) && port != -1) {
                if (mEasyServer != null) {
                    mEasyServer.interrupt();
                }
                mEasyServer = new EasyServer();
                mEasyServer.setType(TYPE_P2P);
                mEasyServer.setHTTPPort(port);
                mEasyServer.setBindIP(ip);
                mEasyServer.start();
                mEasyServer.setServerListener(this);
            }
        }

        String key = intent.getStringExtra(EXTRA_SETUP_SERVER);
        if (!TextUtils.isEmpty(key)) {
            //reload the server
            ServerInfoParcelableHelper helper = new ServerInfoParcelableHelper(getFilesDir().getAbsolutePath());
            DeviceInfo info = helper.get(Constants.PARAM_DEVICE_INFO);
            if (info != null) {
                ServerManager.getInstance().setDeviceInfo(info);
                ServerManager.getInstance().setContext(this);
                ServerManager.getInstance().setIp(info.getIp());
                ServerManager.getInstance().setIconPath(info.getIcon());
                List<File> fileList = new ArrayList<>();
                for (Map.Entry<String, List<String>> entry : info.getFileMap().entrySet()) {
                    for (String path : entry.getValue()) {
                        File file = new File(path);
                        if (fileList.indexOf(file) < 0) {
                            fileList.add(file);
                        }
                    }
                }
                ServerManager.getInstance().setSharedFileList(fileList);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        System.out.println("easyserver后台服务终止");
        Log.i(TAG_NAME, "onDestroy");
        isBind = false;
        super.onDestroy();

        stopForeground(true);
        this.unregisterReceiver(mReceiver);

        if (mEasyServer != null) {
            mEasyServer.interrupt();
            try {
                mEasyServer.join(10 * 1000);
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void initNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.notification_wifi);
        builder.setContentTitle("ShareBox");
        builder.setTicker("正在运行");
        builder.setContentText("正在运行");

        final Intent notificationIntent = new Intent("action_onclick");

        notificationIntent.putExtra("click", 1);
        final PendingIntent pi = PendingIntent.getBroadcast(this, 1, notificationIntent
                , PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteView = new RemoteViews(this.getPackageName(), R.layout.custom_notification_view);

        builder.setContent(remoteView);

        remoteView.setOnClickPendingIntent(R.id.img_view_exit, pi);
        notificationIntent.putExtra("click", 2);
        PendingIntent pi2 = PendingIntent.getBroadcast(this, 2, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        remoteView.setOnClickPendingIntent(R.id.img_view_exit, pi);
        remoteView.setOnClickPendingIntent(R.id.main_content, pi2);

        mNotification = builder.build();

        startForeground(101, mNotification);

        IntentFilter filter = new IntentFilter("action_onclick");
        mReceiver = new NotificationClickReceiver();
        this.registerReceiver(mReceiver, filter);
    }

    public void initEasyServer() {
    }

    @Override
    public void ready(Object server, String hostIP, int port) {
        Log.i(TAG_NAME, "server ready " + server.toString() + " host ip:" + hostIP + " port:" + port);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(Constants.PREF_KEY_HOST_IP, hostIP);
        editor.putInt(Constants.PREF_KEY_HOST_PORT, port);
        editor.putLong(Constants.PREF_OBSERVER_CHANGE, System.currentTimeMillis()).apply();
        editor.commit();
        Intent intent = new Intent(Constants.ACTION_UPDATE_SERVER);
        sendBroadcast(intent);
    }

    public class NotificationClickReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().compareTo("action_onclick") == 0) {
                switch (intent.getIntExtra("click", -1)) {
                    case 1:
                        try {
                            WifiManager manager = (WifiManager) EasyServerService.
                                    this.getSystemService(WIFI_SERVICE);
                            WifiUtil.openHotSpot(manager, false, "", "");
                            context.stopService(new Intent(context, EasyServerService.class));
                            //finish app
                            Intent closeIntent = new Intent(Constants.ACTION_CLOSE_SERVER);
                            context.sendBroadcast(closeIntent);

                        } catch (IllegalArgumentException e) {
                        } finally {
                            System.exit(0);
                        }
                        break;
                    case 2:
                        Intent i = context.getPackageManager().getLaunchIntentForPackage("com.ecjtu.sharebox");
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                        StatusBarUtil.collapseStatusBar(context);
                        break;
                }
            }
        }
    }

    public static Intent getApIntent(Context context) {
        Intent i = new Intent(context, EasyServerService.class);
        i.putExtra(EXTRA_SERVER_TYPE, TYPE_AP);
        return i;
    }

    public static Intent getP2PIntent(Context context, String ip, int port) {
        Intent i = new Intent(context, EasyServerService.class);
        i.putExtra(EXTRA_SERVER_TYPE, TYPE_P2P);
        i.putExtra(EXTRA_SERVER_IP, ip);
        i.putExtra(EXTRA_SERVER_PORT, port);
        return i;
    }

    public static Intent getSetupServerIntent(Context context, String key) {
        Intent i = new Intent(context, EasyServerService.class);
        i.putExtra(EXTRA_SETUP_SERVER, key);
        return i;
    }

    public boolean isServerAlive() {
        if (mEasyServer == null) return false;
        return mEasyServer.isRunning();
    }

    public String getIp() {
        if (mEasyServer == null) return null;
        return mEasyServer.getBindIP();
    }

    public int getPort() {
        if (mEasyServer == null) return 0;
        return mEasyServer.getHTTPPort();
    }
}
