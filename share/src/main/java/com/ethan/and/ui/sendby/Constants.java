package com.ethan.and.ui.sendby;

import android.content.Context;
import android.content.SharedPreferences;

public class Constants {

    private static final String TAG = "Constants";

    private static final String KEY_CHUNK_SIZE = "key_chunk_size";
    private static final String KEY_CHUNKED_STREAMING_MODE = "key_chunked_streaming_mode";

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
        SharedPreferences pref = this.appContext.getSharedPreferences("constants", Context.MODE_PRIVATE);
        return pref.getLong(KEY_CHUNK_SIZE, 10 * 1024 * 1024);
    }

    public void setChunkSize(long size) {
        SharedPreferences pref = this.appContext.getSharedPreferences("constants", Context.MODE_PRIVATE);
        pref.edit().putLong(KEY_CHUNK_SIZE, size).apply();
    }

    public void setChunkedStreamingMode(long size) {
        SharedPreferences pref = this.appContext.getSharedPreferences("constants", Context.MODE_PRIVATE);
        pref.edit().putLong(KEY_CHUNKED_STREAMING_MODE, size).apply();
    }

    public long getChunkedStreamingMode() {
        SharedPreferences pref = this.appContext.getSharedPreferences("constants", Context.MODE_PRIVATE);
        return pref.getLong(KEY_CHUNKED_STREAMING_MODE, 10 * 1024 * 1024);
    }
}
