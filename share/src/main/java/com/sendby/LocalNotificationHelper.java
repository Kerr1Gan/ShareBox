package com.sendby;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.flybd.sharebox.BuildConfig;
import com.flybd.sharebox.R;


/**
 * Created by linchen on 2018/5/24.
 */
public class LocalNotificationHelper {
    private int notificationId;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private NotificationCompat.Builder uploadNotificationBuilder;
    private Context context;

    public static final int NOTIFICATION_ID_DEFAULT = 1;
    public static final int NOTIFICATION_ID_UPLOAD = 2;

    public LocalNotificationHelper(Context context, int notificationId) {
        this.context = context;
        this.notificationId = notificationId;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buildNotificationChannel(context);
            buildUploadChannel(context);
        }
        builder = new NotificationCompat.Builder(context, BuildConfig.APPLICATION_ID);
        uploadNotificationBuilder = new NotificationCompat.Builder(context, BuildConfig.APPLICATION_ID);
    }

    public static void cancelAll(Context context) {
        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancelAll();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void buildNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                BuildConfig.APPLICATION_ID,
                BuildConfig.APPLICATION_ID,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(context.getString(R.string.app_name));
        channel.enableLights(false);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationManager.createNotificationChannel(channel);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void buildUploadChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                BuildConfig.APPLICATION_ID,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("upload data");
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationManager.createNotificationChannel(channel);
    }

    private NotificationCompat.Builder configBuilder(PendingIntent pendingIntent, int icon, String ticker, String title, String body,
                                                     boolean shouldSound, boolean shouldVibrate, boolean shouldLight) {
        int notificationProperty = Notification.DEFAULT_SOUND;
        if (!shouldSound) {
            notificationProperty = 0;
        }
        if (shouldVibrate) {
            notificationProperty |= Notification.DEFAULT_VIBRATE;
        }
        if (shouldLight) {
            notificationProperty |= Notification.DEFAULT_LIGHTS;
        }

        builder.setContentIntent(pendingIntent)
                .setSmallIcon(icon)
                .setTicker(ticker)
                .setContentTitle(title)
                .setContentText(body)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(notificationProperty);
        return builder;
    }

    public void send(PendingIntent pendingIntent, int icon, String ticker, String title, String body, boolean shouldSound, boolean shouldVibrate, boolean shouldLight) {
        Notification notification = configBuilder(pendingIntent, icon, ticker, title, body, shouldSound, shouldVibrate, shouldLight)
                .build();
        try {
            // on some old Android system, it throws SecurityException: Requires VIBRATE permission
            notificationManager.notify(notificationId, notification);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // as per GP policy, uploading private info should show persistent notification
    public void createUploadNotification() {
        try {
            int icon = context.getApplicationInfo().icon;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                icon = R.mipmap.ic_launcher;
            }

            notificationManager.cancel(notificationId);

            Notification notification = uploadNotificationBuilder
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText("upload data")
                    .setSmallIcon(icon)
                    .setProgress(0, 0, true)
                    .build();

            // on some old Android system, it throws SecurityException: Requires VIBRATE permission
            notificationManager.notify(notificationId, notification);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // as per GP policy, uploading private info should show persistent notification
    public void createDownloadNotification(boolean sound) {
        try {
            int icon = context.getApplicationInfo().icon;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                icon = R.mipmap.ic_launcher;
            }

            notificationManager.cancel(notificationId);

            Notification notification = uploadNotificationBuilder
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText("download")
                    .setSmallIcon(icon)
                    .setProgress(0, 0, true)
                    .setDefaults(sound ? Notification.DEFAULT_SOUND : 0)
                    .setOnlyAlertOnce(true)
                    .build();

            // on some old Android system, it throws SecurityException: Requires VIBRATE permission
            notificationManager.notify(notificationId, notification);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateDownloadNotification(String contentText, int progress) {
        if (notificationManager != null && uploadNotificationBuilder != null) {
            uploadNotificationBuilder.setContentText(contentText);
            uploadNotificationBuilder.setProgress(100, progress, false);

            notificationManager.notify(notificationId, uploadNotificationBuilder.build());
        }
    }

    public void finishDownloadingNotification() {
        if (notificationManager != null && uploadNotificationBuilder != null) {
            uploadNotificationBuilder.setProgress(0, 0, false);
            notificationManager.notify(notificationId, uploadNotificationBuilder.build());
            notificationManager.cancel(notificationId);
        }
    }

    public void updateUploadNotification(String contentText) {
        if (notificationManager != null && uploadNotificationBuilder != null) {
            uploadNotificationBuilder.setContentText(contentText);

            notificationManager.notify(notificationId, uploadNotificationBuilder.build());
        }
    }

    public void finishUploadingNotification() {
        if (notificationManager != null && uploadNotificationBuilder != null) {
            uploadNotificationBuilder.setProgress(0, 0, false);
            notificationManager.notify(notificationId, uploadNotificationBuilder.build());
            notificationManager.cancel(NOTIFICATION_ID_UPLOAD);
        }
    }
}
