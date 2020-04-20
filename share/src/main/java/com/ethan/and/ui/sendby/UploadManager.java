package com.ethan.and.ui.sendby;

import android.os.SystemClock;
import android.util.Log;

import androidx.collection.SparseArrayCompat;

import com.flybd.sharebox.AppExecutorManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.OkHttpClient;
import okio.BufferedSource;
import okio.Okio;

public class UploadManager {

    private static final String TAG = "UploadManager";

    private SparseArrayCompat<UploadTask> uploadTask;

    private ThreadPoolExecutor executorService;

    private OkHttpClient client;

    private BufferedSource bufferedSource;

    private ByteArrayOutputStream bos = new ByteArrayOutputStream();

    byte[] sink = new byte[8196];

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
                executorService.execute(task.uploadRunnable);
            } else {
                long extraSize = fileLength - (chunk * chunkSize);
                int index = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    UploadTask childTask = (UploadTask) task.clone();
                    Log.i(TAG, "pushTask: active count " + executorService.getActiveCount() + " max size " + executorService.getMaximumPoolSize());
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
                if (bufferedSource != null) {
                    try {
                        bufferedSource.close();
                    } catch (Exception e) {
                    }
                }
            }
        });
        return hash;
    }

    public byte[] loadingData(File file, long offset, long chunkSize) {
        BufferedSource buf = bufferedSource;
        try {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(Okio.source(file));
                buf = bufferedSource;
            }
            //buf = Okio.buffer(Okio.source(file));
            //buf.skip(offset);
            int len;
            bos.reset();
            int total = 0;
            int readBytes = 8196;
            while ((len = buf.read(sink, 0, readBytes)) >= 0) {
                bos.write(sink, 0, len);
                total += len;
                if (chunkSize - total < 8196) {
                    readBytes = (int) (chunkSize - total);
                }
                if (total >= chunkSize) {
                    break;
                }
            }
            //buf.close();
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            if (buf != null) {
//                try {
//                    buf.close();
//                } catch (IOException e) {
//                }
//            }
        }
        return null;
    }

    public String getMd5Hash(byte[] bytes) throws NoSuchAlgorithmException {
        // 生成一个MD5加密计算摘要
        MessageDigest md = MessageDigest.getInstance("MD5");
        // 计算md5函数
        md.update(bytes);
        // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
        // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
        //Log.i(TAG, "position: " + new BigInteger(1, md.digest()).toString(16));
        String md5Hash = new BigInteger(1, md.digest()).toString(16);
        Log.i(TAG, "hash: " + md5Hash);
        return md5Hash;
    }

    private static class Inner {
        private static final UploadManager sInstance = new UploadManager();
    }
}
