package com.sendby;

import android.os.SystemClock;
import android.util.Log;

import androidx.collection.SparseArrayCompat;

import com.flybd.sharebox.AppExecutorManager;
import com.flybd.sharebox.BuildConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.OkHttpClient;

public class UploadManager {

    private static final String TAG = "UploadManager";

    private SparseArrayCompat<UploadTask> uploadTask;

    private ThreadPoolExecutor executorService;

    private OkHttpClient client;

    private UploadManager() {
        uploadTask = new SparseArrayCompat<>();
        executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        client = new OkHttpClient();
    }

    public static UploadManager getInstance() {
        return Inner.sInstance;
    }

    public int pushTask(UploadTask task) {
        int hash = task.getKey().hashCode();
        while (uploadTask.get(hash) != null) {
            hash++;
        }

        AppExecutorManager.INSTANCE.getInstance().networkIO().execute(() -> {
            long fileLength = task.file.length();
            long chunkSize = Constants.get().getChunkSize();
            int chunk = (int) (fileLength / chunkSize);
            if (chunk == 0) {
                task.setUploadRunnable(new UploadTask.UploadRunnable(task, 0, task.file.length(), 1, 0));
                //task.getUploadRunnable().loadingData();
                executorService.execute(task.getUploadRunnable());
            } else {
                long extraSize = fileLength - (chunk * chunkSize);
                int index = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    UploadTask childTask = (UploadTask) task.clone();
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "pushTask: active count " + executorService.getActiveCount() + " max size " + executorService.getMaximumPoolSize());
                    }
                    if (executorService.getActiveCount() == executorService.getMaximumPoolSize()) {
                        SystemClock.sleep(10);
                        continue;
                    }
                    if (childTask != null) {
                        if (index >= chunk) {
                            childTask.setUploadRunnable(new UploadTask.UploadRunnable(childTask, index * chunkSize, index * chunkSize + extraSize, chunk + 1, index));
                            executorService.execute(childTask.getUploadRunnable());
                        } else {
                            childTask.setUploadRunnable(new UploadTask.UploadRunnable(childTask, index * chunkSize, index * chunkSize + chunkSize, chunk + 1, index));
                            executorService.execute(childTask.getUploadRunnable());
                        }
                    }
                    index++;
                    if (index >= chunk + 1) {
                        break;
                    }
                }
            }
        });
        uploadTask.put(hash, task);
        return hash;
    }

    public UploadTask getTask(int hash) {
        return uploadTask.get(hash);
    }

    public void clear() {
        uploadTask.clear();
    }

    private static class Inner {
        private static final UploadManager sInstance = new UploadManager();
    }
}
