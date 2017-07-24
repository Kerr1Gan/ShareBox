package org.ecjtu.easyserver.server.impl.service;

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
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import org.ecjtu.easyserver.R;
import org.ecjtu.easyserver.net.HostInterface;
import org.ecjtu.easyserver.server.ServerInfoCarrier;
import org.ecjtu.easyserver.server.ServerManager;
import org.ecjtu.easyserver.server.impl.server.EasyServer;
import org.ecjtu.easyserver.server.util.WifiUtil;
import org.ecjtu.easyserver.util.StatusBarUtil;

import static org.ecjtu.easyserver.server.impl.server.EasyServer.TYPE_AP;
import static org.ecjtu.easyserver.server.impl.server.EasyServer.TYPE_NOTHING;
import static org.ecjtu.easyserver.server.impl.server.EasyServer.TYPE_P2P;


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

    public static final String EXTRA_SETUP_SERVER= "extra_setup_server";

    public static final int SERVER_TYPE_AP = TYPE_AP;

    public static final int SERVER_TYPE_P2P = TYPE_P2P;

    public static final int SERVER_TYPE_NOT= TYPE_NOTHING;

    @Override
    public void onCreate() {
        super.onCreate();

        mBinder = new EasyServerBinder();
        isBind = false;

        initNotification();
        initEasyServer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null)
            return super.onStartCommand(intent,flags,startId);

        int type = intent.getIntExtra(EXTRA_SERVER_TYPE, SERVER_TYPE_NOT);

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

        Object param=intent.getSerializableExtra(EXTRA_SETUP_SERVER);
        if(param!=null){
            ServerInfoCarrier carrier=(ServerInfoCarrier)param;
            ServerManager manager=ServerManager.getInstance();
            manager.setDeviceInfo(carrier.deviceInfo);
            manager.setIconPath(carrier.iconPath);
            manager.setIp(carrier.ip);
            manager.setSharedFileList(carrier.sharedFileList);
            manager.setContext(this);
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

    public void initNotification(){
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

    public void initEasyServer(){
        EasyServer.setServerListener(new HostInterface.ICallback() {
            @Override
            public void ready(Object server, String hostIP, int port) {

            }
        });
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
                            try {
                                context.unbindService(mServerConnection);
                            }catch (Exception e){
                            }
                            context.stopService
                                    (new Intent(context, EasyServerService.class));
                            mServerConnection = null;

                            //finish app
                            // TODO: 2017/7/8
//                            LocalBroadcastManager.getInstance(mContext)
//                                    .sendBroadcast(new Intent
//                                            (MainActivity.CloseBroadCastReceiver.ACTION_CLOSE_APP));

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
                        }finally {
                            System.exit(0);
                        }
                        break;
                    case 2:
                        // TODO: 2017/7/8
                        Intent i = context.getPackageManager().getLaunchIntentForPackage("com.ecjtu.sharebox");
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("mode", 1);
                        context.startActivity(i);
                        StatusBarUtil.collapseStatusBar(context);
                        break;
                }
            }
        }
    }

    public static Intent getApIntent(Context context){
        Intent i=new Intent(context,EasyServerService.class);
        i.putExtra(EXTRA_SERVER_TYPE,TYPE_AP);
        return i;
    }

    public static Intent getP2PIntent(Context context,String ip,int port){
        Intent i=new Intent(context,EasyServerService.class);
        i.putExtra(EXTRA_SERVER_TYPE,TYPE_P2P);
        i.putExtra(EXTRA_SERVER_IP,ip);
        i.putExtra(EXTRA_SERVER_PORT, port);
        return i;
    }

    public boolean isServerAlive(){
        if(mEasyServer==null) return false;
        return mEasyServer.isRunning();
    }

    public String getIp(){
        return mEasyServer.getBindIP();
    }

    public int getPort(){
        return mEasyServer.getHTTPPort();
    }
}
