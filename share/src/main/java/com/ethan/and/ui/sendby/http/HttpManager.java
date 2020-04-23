package com.ethan.and.ui.sendby.http;

import android.content.Context;

import com.ethan.and.ui.sendby.entity.CommonResponse;
import com.ethan.and.ui.sendby.entity.DownloadListResponse;
import com.ethan.and.ui.sendby.entity.HttpResponse;
import com.ethan.and.ui.sendby.entity.KeyEntity;
import com.flybd.sharebox.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpManager {

    private OkHttpClient okHttpClient;

    private Context context;

    private static HttpManager sHttpManager;

    private HttpManager(Context context) {
        this.context = context;

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(new DefaultHeaderAddInterceptor(context.getApplicationContext()));
//                .addInterceptor(new FirebaseHeaderInterceptor())
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClientBuilder.addInterceptor(logging);
        }
        okHttpClient = httpClientBuilder.build();
    }

    public static OkHttpClient getOkHttpClient() {
        return sHttpManager.okHttpClient;
    }

    public static HttpManager getInstance() {
        return sHttpManager;
    }

    public static HttpManager getInstance(Context context) {
        sHttpManager = new HttpManager(context);
        return sHttpManager;
    }

    //http://192.168.3.16:9998/spread/file/upload
    public CommonResponse getConfig(String[] remoteUrl) {
        OkHttpClient client = getOkHttpClient();
        String[] urls = new String[remoteUrl.length];
        for (int i = 0; i < remoteUrl.length; i++) {
            urls[i] = remoteUrl[i] + "/spread/file/config";
        }
        for (String url : urls) {
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    continue;
                }
                return new Gson().fromJson(response.body().string(), new TypeToken<CommonResponse>() {
                }.getType());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public HttpResponse<KeyEntity> getCode(String[] remoteUrl, List<String> fileNames) {
        OkHttpClient client = getOkHttpClient();
        String[] urls = new String[remoteUrl.length];
        for (int i = 0; i < remoteUrl.length; i++) {
            urls[i] = remoteUrl[i] + "/spread/file/code";
        }
        for (String url : urls) {
            try {
                RequestBody reqBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), new Gson().toJson(fileNames));
                Request request = new Request.Builder()
                        .url(url)
                        .post(reqBody)
                        .build();
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    continue;
                }
                return new Gson().fromJson(response.body().string(), new TypeToken<HttpResponse<KeyEntity>>() {
                }.getType());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public CommonResponse getFileSize(String[] remoteUrl, String fileName, int chunkIndex, String key) {
        OkHttpClient client = getOkHttpClient();
        String[] urls = new String[remoteUrl.length];
        for (int i = 0; i < remoteUrl.length; i++) {
            urls[i] = remoteUrl[i] + "/spread/file/file-size";
        }
        for (String url : urls) {
            try {
                Request request = new Request.Builder()
                        .header("fileName", fileName)
                        .header("chunkIndex", String.valueOf(chunkIndex))
                        .header("key", key)
                        .url(url)
                        .build();
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    continue;
                }
                return new Gson().fromJson(response.body().string(), new TypeToken<CommonResponse>() {
                }.getType());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public DownloadListResponse getDownloadList(String[] remoteUrl, String code) {
        OkHttpClient client = getOkHttpClient();
        String[] urls = new String[remoteUrl.length];
        for (int i = 0; i < remoteUrl.length; i++) {
            urls[i] = remoteUrl[i] + "/spread/file/file-urls";
        }
        for (String url : urls) {
            try {
                Request request = new Request.Builder()
                        .header("key", code)
                        .url(url)
                        .build();
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    continue;
                }
                return new Gson().fromJson(response.body().string(), new TypeToken<DownloadListResponse>() {
                }.getType());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
