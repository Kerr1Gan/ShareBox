package com.ethan.and.ui.sendby;

import android.content.Context;
import android.util.Log;

import com.ethan.and.ui.sendby.http.HttpManager;
import com.ethan.and.ui.sendby.http.TraceSender;
import com.ethan.and.ui.sendby.http.bean.CommonResponse;
import com.flybd.sharebox.BuildConfig;
import com.flybd.sharebox.R;

import org.ecjtu.channellibrary.wifiutil.NetworkUtil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import okio.BufferedSource;
import okio.Okio;

public class UploadTask implements Cloneable {

    private static final String TAG = "UploadTask";

    private static final int TIME_OUT = 8 * 1000;                          //超时时间
    private static final String CHARSET = "utf-8";                         //编码格式
    private static final String PREFIX = "--";                            //前缀
    private static final String BOUNDARY = UUID.randomUUID().toString();  //边界标识 随机生成
    private static final String CONTENT_TYPE = "multipart/form-data";     //内容类型
    private static final String LINE_END = "\r\n";


    int taskId;
    String name;
    String key;
    File file;
    Context ctx;
    String url;
    AtomicLong transferBytes = new AtomicLong();
    volatile int[] status = new int[]{Status.IDLE};
    UploadRunnable uploadRunnable;
    volatile boolean[] stop = new boolean[]{false};
    Thread workThread;

    public static class Status {
        public static final int IDLE = 1;
        public static final int RUNNING = 2;
        public static final int END = 3;
    }

    public static class UploadRunnable implements Runnable {

        private static final long sChunkSize = 1024 * 1024L;

        UploadTask task;
        long startPosition;
        long endPosition;
        long totalSize;
        int chunk;
        int chunkIndex;
        byte[] data;

        public UploadRunnable(UploadTask task, long startPosition, long endPosition, int chunk, int chunkIndex) {
            this.task = task;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
            this.totalSize = endPosition - startPosition;
            this.chunk = chunk;
            this.chunkIndex = chunkIndex;
        }

        public void loadingData() {
            BufferedSource buf = null;
            try {
                buf = Okio.buffer(Okio.source(task.file));
                buf.skip(startPosition);
                byte[] sink = new byte[8196];
                int len;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int total = 0;
                int readBytes = 8196;
                while ((len = buf.read(sink, 0, readBytes)) >= 0) {
                    bos.write(sink, 0, len);
                    total += len;
                    if (sChunkSize - total < 8196) {
                        readBytes = (int) (sChunkSize - total);
                    }
                    if (total >= sChunkSize) {
                        break;
                    }
                }
                buf.close();
                data = bos.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (buf != null) {
                    try {
                        buf.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            long total = 0;
            long uploadedSize = 0L;
            InputStream in = null;
            task.setWorkThread(Thread.currentThread());
            try {
                task.setStatus(Status.RUNNING);
                CommonResponse res = HttpManager.getInstance().getFileSize(Constants.get().getRestUrl(), task.name, chunkIndex, task.key);
                if (isStop()) {
                    return;
                }
                if (res != null) {
                    try {
                        if ("success".equalsIgnoreCase(res.getInfo())) {
                            Log.i(TAG, "run: 分片" + chunkIndex + "已上传成功");
                            task.getTransferBytes().addAndGet(totalSize);
                            return;
                        }
                        if (res.getData() instanceof Number) {
                            uploadedSize = (long) (double) res.getData();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (isStop()) {
                    return;
                }
                String md5Hash = getMd5Hash(task.file, startPosition, endPosition);
                if (isStop()) {
                    return;
                }
                startPosition += uploadedSize;
                task.getTransferBytes().addAndGet(uploadedSize);

                URL url = new URL(task.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setReadTimeout(TIME_OUT);
                conn.setChunkedStreamingMode((int) Constants.get().getChunkedStreamingMode());
                conn.setConnectTimeout(TIME_OUT);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);//Post 请求不能使用缓存
                conn.setRequestProperty("key", task.getKey());
                conn.setRequestProperty("startPosition", String.valueOf(startPosition));
                conn.setRequestProperty("endPosition", String.valueOf(endPosition));
                conn.setRequestProperty("totalSize", String.valueOf(totalSize));
                conn.setRequestProperty("md5", md5Hash);
                conn.setRequestProperty("chunk", String.valueOf(this.chunk));
                conn.setRequestProperty("chunkIndex", String.valueOf(this.chunkIndex));
                conn.setRequestProperty("Fast-Pass", "ApFRA8rMc57pOlDg");
                conn.setRequestProperty("File-Size", String.valueOf(uploadedSize));
                conn.setRequestProperty("fileName", task.file.getName());
                //设置请求头参数
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

                Locale locale = task.ctx.getResources().getConfiguration().locale;
                String lang = locale.getLanguage();
                String country = locale.getCountry();
                conn.setRequestProperty("X-APP-TYPE", "ANDROID");
                conn.setRequestProperty("X-APP-VERSION", String.valueOf(BuildConfig.VERSION_CODE));
                conn.setRequestProperty("X-APP-VERSION-NAME", String.valueOf(BuildConfig.VERSION_NAME));
                conn.setRequestProperty("X-APP-PACKAGE-NAME", BuildConfig.APPLICATION_ID);
                conn.setRequestProperty("X-APP-NAME", task.ctx.getString(R.string.app_name));
                conn.setRequestProperty("X-LANGUAGE", lang);
                conn.setRequestProperty("X-COUNTRY", country);
                conn.setRequestProperty("X-APP-TYPE-V2", "ANDROID");
                conn.setRequestProperty("X-ANDROID-ID", TraceSender.getAndroidID(task.ctx));
                conn.setRequestProperty("X-SIM-OPERATOR", NetworkUtil.getCurrentSimOperator(task.ctx));
                conn.setRequestProperty("X-NETWORK-STATE", String.valueOf(NetworkUtil.getNetworkState(task.ctx)));
                conn.connect();
                /**
                 * 请求体
                 */
                //上传参数
                OutputStream dos = conn.getOutputStream();
                //getStrParams()为一个
                dos.write(getStrParams(new HashMap<>()).toString().getBytes());
                dos.flush();

                //文件上传
                StringBuilder fileSb = new StringBuilder();
                Map<String, File> fileParams = new HashMap<>();
                fileParams.put(task.file.getName(), task.file);
                for (Map.Entry<String, File> fileEntry : fileParams.entrySet()) {
                    if (isStop()) {
                        return;
                    }
                    fileSb.append(PREFIX)
                            .append(BOUNDARY)
                            .append(LINE_END)
                            /**
                             * 这里重点注意： name里面的值为服务端需要的key 只有这个key 才可以得到对应的文件
                             * filename是文件的名字，包含后缀名的 比如:abc.png
                             */
                            .append("Content-Disposition: form-data; name=\"file\"; filename=\""
                                    + fileEntry.getKey() + "\"" + LINE_END)
                            .append("Content-Type: application/octet-stream" + LINE_END) //此处的ContentType不同于 请求头 中Content-Type
                            .append("Content-Transfer-Encoding: 8bit" + LINE_END)
                            .append(LINE_END);// 参数头设置完以后需要两个换行，然后才是参数内容
                    dos.write(fileSb.toString().getBytes());
                    dos.flush();
                    InputStream is = new FileInputStream(fileEntry.getValue());
                    byte[] buffer = new byte[8196];
                    long len;
                    is.skip(startPosition);
                    while ((len = is.read(buffer)) != -1) {
                        if (isStop()) {
                            return;
                        }
                        if (startPosition + total + len > endPosition) {
                            len = endPosition - startPosition - total;
                        }
                        total += len;
                        task.getTransferBytes().addAndGet(len);
                        byte[] bytes = new byte[(int) len];
                        System.arraycopy(buffer, 0, bytes, 0, (int) len);
                        dos.write(bytes, 0, (int) len);
                    }
                    is.close();
                    dos.write(LINE_END.getBytes());
                }
                //请求结束标志
                dos.write((PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes());
                dos.flush();
                dos.close();
                Log.e(TAG, "postResponseCode() = " + conn.getResponseCode());
                //读取服务器返回信息
                if (conn.getResponseCode() == 200) {
                    if (isStop()) {
                        return;
                    }
                    in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    Log.e(TAG, "run: " + response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                task.getTransferBytes().addAndGet(-total);
                task.getTransferBytes().addAndGet(-uploadedSize);
                Log.i(TAG, "run: startPosition " + startPosition + " endPosition " + endPosition);
                try {
                    Thread.sleep(50);
                    run();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    return;
                }
            } finally {
                AtomicLong bytesTransfer = task.getTransferBytes();
                //bytesTransfer.addAndGet(total);
                Log.i(TAG, "run: total " + bytesTransfer.get() + " real " + task.file.length());
                if (bytesTransfer.get() == task.file.length()) {
                    task.setStatus(Status.END);
                }
                if (conn != null) {
                    conn.disconnect();
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        public String getMd5Hash(File file, long startPosition, long endPosition) throws Exception {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream is = new FileInputStream(file);
            byte[] buffer = new byte[8196];
            long len;
            is.skip(startPosition);
            long total = 0;
            while ((len = is.read(buffer)) != -1) {
                if (startPosition + total + len > endPosition) {
                    len = endPosition - startPosition - total;
                }
                total += len;
                byte[] bytes = new byte[(int) len];
                System.arraycopy(buffer, 0, bytes, 0, (int) len);
                md.update(bytes);
            }
            is.close();
            String md5Hash = new BigInteger(1, md.digest()).toString(16);
            Log.i(TAG, "hash: " + md5Hash);
            return md5Hash;
        }

        public boolean isStop() {
            if (task.isStop() || (task.getWorkThread() != null && task.getWorkThread().isInterrupted())) {
                task.getWorkThread().interrupt();
                return true;
            }
            return false;
        }

        /**
         * 对post参数进行编码处理
         */
        private static StringBuilder getStrParams(Map<String, String> strParams) {
            StringBuilder strSb = new StringBuilder();
            for (Map.Entry<String, String> entry : strParams.entrySet()) {
                strSb.append(PREFIX)
                        .append(BOUNDARY)
                        .append(LINE_END)
                        .append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_END)
                        .append("Content-Type: text/plain; charset=" + CHARSET + LINE_END)
                        .append("Content-Transfer-Encoding: 8bit" + LINE_END)
                        .append(LINE_END)// 参数头设置完以后需要两个换行，然后才是参数内容
                        .append(entry.getValue())
                        .append(LINE_END);
            }
            return strSb;
        }
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public UploadRunnable getUploadRunnable() {
        return uploadRunnable;
    }

    public void setUploadRunnable(UploadRunnable uploadRunnable) {
        this.uploadRunnable = uploadRunnable;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public AtomicLong getTransferBytes() {
        return transferBytes;
    }

    public void setTransferBytes(AtomicLong transferBytes) {
        this.transferBytes = transferBytes;
    }

    @Override
    public Object clone() {
        try {
            UploadTask task = (UploadTask) super.clone();
            task.setStatus(getStatus());
            task.setTaskId(getTaskId());
            task.setFile(new File(this.file.getAbsolutePath()));
            task.setWorkThread(getWorkThread());
            task.setStop(isStop());
            task.ctx = this.ctx;
            return task;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Context getCtx() {
        return ctx;
    }

    public void setCtx(Context ctx) {
        this.ctx = ctx.getApplicationContext();
    }

    public int getStatus() {
        return status[0];
    }

    public void setStatus(int status) {
        this.status[0] = status;
    }

    public boolean isStop() {
        return stop[0];
    }

    public void setStop(boolean stop) {
        this.stop[0] = stop;
    }

    public void stop() {
        setStop(true);
        if (getWorkThread() != null) {
            getWorkThread().interrupt();
        }
    }

    public Thread getWorkThread() {
        return workThread;
    }

    public void setWorkThread(Thread workThread) {
        this.workThread = workThread;
    }
}
