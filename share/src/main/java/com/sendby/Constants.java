package com.sendby;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.sendby.entity.LoginEntity;

public class Constants {

    private static final String TAG = "Constants";

    private static final String FILE_NAME = "constants";
    private static final String KEY_CHUNK_SIZE = "key_chunk_size";
    private static final String KEY_CHUNKED_STREAMING_MODE = "key_chunked_streaming_mode";
    private static final String KEY_LOGIN_TOKEN = "key_login_token";
    private static final String KEY_LOGIN_ENTITY = "key_login_entity";
    private static final String KEY_REMOVE_AD = "key_remove_ad";

    private Context appContext;

    private Constants() {
    }

    private static class Inner {
        private static Constants sInstance = new Constants();
    }

    public static Constants get() {
        return Inner.sInstance;
    }

    public void init(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public String[] getRestUrl() {
        return new String[]{"http://54.169.98.71:9998"};
    }

    public long getChunkSize() {
        SharedPreferences pref = this.appContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getLong(KEY_CHUNK_SIZE, 10 * 1024 * 1024);
    }

    public void setChunkSize(long size) {
        SharedPreferences pref = this.appContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putLong(KEY_CHUNK_SIZE, size).apply();
    }

    public void setChunkedStreamingMode(long size) {
        SharedPreferences pref = this.appContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putLong(KEY_CHUNKED_STREAMING_MODE, size).apply();
    }

    public long getChunkedStreamingMode() {
        SharedPreferences pref = this.appContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getLong(KEY_CHUNKED_STREAMING_MODE, 10 * 1024 * 1024);
    }

    public String getToken() {
        SharedPreferences pref = this.appContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_LOGIN_TOKEN, "");
    }

    public void setToken(String token) {
        SharedPreferences pref = this.appContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_LOGIN_TOKEN, token).apply();
    }

    public void setLoginEntity(LoginEntity loginEntity) {
        SharedPreferences pref = this.appContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        if (loginEntity != null) {
            pref.edit().putString(KEY_LOGIN_ENTITY, new Gson().toJson(loginEntity)).apply();
        } else {
            pref.edit().putString(KEY_LOGIN_ENTITY, "").apply();
        }
    }

    public LoginEntity getLoginEntity() {
        SharedPreferences pref = this.appContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String body = pref.getString(KEY_LOGIN_ENTITY, "");
        if (!TextUtils.isEmpty(body)) {
            try {
                return new Gson().fromJson(body, LoginEntity.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setRemoveAd(boolean remove) {
        SharedPreferences pref = this.appContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_REMOVE_AD, remove).apply();

    }

    public boolean isRemoveAd() {
        SharedPreferences pref = this.appContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_REMOVE_AD, false);
    }
}
