package com.sendby;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import com.flybd.sharebox.BuildConfig;
import com.flybd.sharebox.R;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CoreService extends Service {

    private static final String TAG = "CoreService";

    private HandlerThread workThread;

    private Handler workHandler;

    private Map<String, LocalNotificationHelper> helpers = new ArrayMap<>();

    public static Intent getDownloadIntent(Context context, String url, String name) {
        Intent intent = new Intent(context, CoreService.class);
        intent.putExtra("url", url);
        intent.putExtra("name", name);
        intent.putExtra("type", 0);
        return intent;
    }

    public static Intent getUploadIntent(Context context, String url, String name) {
        Intent intent = new Intent(context, CoreService.class);
        intent.putExtra("url", url);
        intent.putExtra("name", name);
        intent.putExtra("type", 1);
        return intent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        workThread = new HandlerThread(TAG);
        workThread.start();
        workHandler = new Handler(workThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int type = intent.getIntExtra("type", -1);
            String url = intent.getStringExtra("url");
            String name = intent.getStringExtra("name");
            if (type == 0) {
                toDownload(url, name);
            } else if (type == 1) {
                toUpload(url, "key", name);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (workHandler != null) {
            workHandler.removeCallbacksAndMessages(null);
        }
        if (workThread != null) {
            workThread.quit();
        }
        workHandler = null;
        workThread = null;
    }

    private void toDownload(String url, String name) {
        LocalNotificationHelper helper = helpers.get(url);
        if (helper == null) {
            helper = new LocalNotificationHelper(this, url.hashCode());
        }
        helper.createDownloadNotification(false);

        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + name;
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        final String finalName = name;
        final LocalNotificationHelper finalHelper = helper;
        FileDownloader.getImpl().setMaxNetworkThreadCount(Runtime.getRuntime().availableProcessors() * 2);
        int downloadId = FileDownloader.getImpl().create(url)
                .setPath(path)
                .setCallbackProgressTimes(100)
                .setListener(new FileDownloadLargeFileListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        float percent = (soFarBytes * 1f) / (totalBytes * 1f);
                        finalHelper.updateDownloadNotification(finalName + " is downloading " + (int) (percent * 100) + "%", (int) (percent * 100));
                        Log.i(TAG, "progress: " + finalName + " is downloading..." + (int) (percent * 100));
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        Log.i(TAG, "completed: " + task.toString());

                        finalHelper.finishDownloadingNotification();
                        Context context = CoreService.this;
                        Intent intent = new Intent(context, SendByActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        PendingIntent pIntent = PendingIntent.getActivity(context, (int) (Math.random() * 1000), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        LocalNotificationHelper notify = new LocalNotificationHelper(context, (int) (Math.random() * 1000));
                        notify.send(pIntent, R.mipmap.ic_launcher, "Downloading finish", "Downloading finish", "Download " + finalName + " success.", false, false, false);
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        Log.i(TAG, "download task " + task);
                        Log.i(TAG, "download file error " + e);
                        finalHelper.finishDownloadingNotification();

                        LocalNotificationHelper notify = new LocalNotificationHelper(CoreService.this, (int) (Math.random() * 1000));
                        notify.send(null, R.mipmap.ic_launcher, "Downloading failed", "Downloading failed", "Download " + finalName + " failed.", false, false, false);
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                    }
                })
                .start();

    }

    private void toUpload(String url, String key, String name) {
        OkHttpClient client = new OkHttpClient();
        File file = new File("File path");
        long fileLen = file.length();
        ProgressRequestBody requestBody = new ProgressRequestBody(MediaType.parse("application/octet-stream"), file, totalBytesRead -> {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "toUpload: " + name + " fileLen " + fileLen + " uploaded " + totalBytesRead);
            }
        });
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key", key)
                .addFormDataPart("file", name, requestBody)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();

            }
        });
    }
}
