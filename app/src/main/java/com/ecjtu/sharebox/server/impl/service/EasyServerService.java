package com.ecjtu.sharebox.server.impl.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.ecjtu.sharebox.R;
import com.ecjtu.sharebox.server.impl.server.EasyServer;

import org.ecjtu.channellibrary.wifiutil.WifiUtil;

import static com.ecjtu.sharebox.server.impl.server.EasyServer.TYPE_AP;
import static com.ecjtu.sharebox.server.impl.server.EasyServer.TYPE_P2P;


/**
 * Created by KerriGan on 2016/4/24.
 */
public class EasyServerService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private EasyServer mEasyServer = null;

    private Notification mNotification;

    public boolean isBind;

    private NotificationClickReceiver mReceiver;

    public static String APP_NAME;

    public static final String EXTRA_SERVER_PORT = "extra_server_port";

    public static final String EXTRA_SERVER_IP = "extra_server_ip";

    public static final String EXTRA_SERVER_TYPE = "extra_server_type";

    public static final int SERVER_TYPE_AP = TYPE_AP;

    public static final int SERVER_TYPE_P2P = TYPE_P2P;

    @Override
    public void onCreate() {
        super.onCreate();

        mBinder = new EasyServerBinder();
        isBind = false;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ico_wifi_open);
        builder.setContentTitle(APP_NAME);
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

//        remoteView.setOnClickPendingIntent(R.id.image_view, pi);
        remoteView.setOnClickPendingIntent(R.id.main_content, pi2);

        mNotification = builder.build();


        startForeground(101, mNotification);


        IntentFilter filter = new IntentFilter("action_onclick");
        mReceiver = new NotificationClickReceiver();
        this.registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int type = intent.getIntExtra(EXTRA_SERVER_TYPE, SERVER_TYPE_AP);

        if (type == SERVER_TYPE_AP) {
            if (mEasyServer != null) {
                mEasyServer.interrupt();
            }
            mEasyServer = new EasyServer();
            mEasyServer.setType(TYPE_AP);
            mEasyServer.start();
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
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        System.out.println("easyserver后台服务终止");
        isBind = false;
        super.onDestroy();

        stopForeground(true);
        this.unregisterReceiver(mReceiver);

        if (mEasyServer != null)
            mEasyServer.interrupt();

    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public EasyServerBinder mBinder;

    private Context mContext;

    private EasyServerConnection mServerConnection;

    public class EasyServerBinder extends Binder {
        private boolean mIsBind = false;

        public EasyServerService getService() {
            return EasyServerService.this;
        }

        public boolean isBind() {
            return mIsBind;
        }

        public void bind(boolean bind) {
            mIsBind = bind;
        }

        public void clearNotification() {
            NotificationManager manager = (NotificationManager) EasyServerService.
                    this.getSystemService(NOTIFICATION_SERVICE);
            manager.cancel(101);
        }

        public void setHostContext(Context context) {
            mContext = context;
        }

        public void setServerConnection(EasyServerConnection con) {
            mServerConnection = con;
        }

        public boolean isRunning() {
            return EasyServerService.this.isBind;
        }

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

                            if (mServerConnection != null && mContext != null && isBind)
                                mContext.unbindService(mServerConnection);
                            mBinder.getService().mContext.stopService
                                    (new Intent(EasyServerService.this, EasyServerService.class));
                            mServerConnection = null;

                            //finish app
//                            ((Activity) mContext).finish();
                            // TODO: 2017/7/8
//                            LocalBroadcastManager.getInstance(mContext)
//                                    .sendBroadcast(new Intent
//                                            (MainActivity.CloseBroadCastReceiver.ACTION_CLOSE_APP));

                            mContext = null;
                        } catch (IllegalArgumentException e) {
                            mBinder.getService().stopService
                                    (new Intent(EasyServerService.this, EasyServerService.class));
                            mServerConnection = null;
                            if (mContext != null) {
                                //finish app
//                                ((Activity) mContext).finish();
                                // TODO: 2017/7/8
//                                LocalBroadcastManager.getInstance(mContext)
//                                        .sendBroadcast(new Intent
//                                                (MainActivity.CloseBroadCastReceiver.ACTION_CLOSE_APP));
                            }
                            mContext = null;
                        }

                        break;
                    case 2:
                        // TODO: 2017/7/8
//                        Intent i = new Intent(EasyServerService.this, MainActivity.class);
//                        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                        i.putExtra("mode", 1);
//                        EasyServerService.this.mContext.startActivity(i);
                        break;
                }
            }
        }
    }

}
