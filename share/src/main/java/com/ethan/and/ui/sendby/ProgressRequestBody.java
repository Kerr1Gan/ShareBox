package com.ethan.and.ui.sendby;

import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;

public class ProgressRequestBody extends RequestBody {
    private static final String TAG = "ProgressRequestBody";
    private MediaType contentType;
    private File file;
    private ProgressCallback callback;
    private long startPosition;
    private long endPosition;

    public ProgressRequestBody(MediaType contentType, File file, ProgressCallback callback) {
        this.contentType = contentType;
        this.file = file;
        this.callback = callback;
        this.startPosition = 0;
        this.endPosition = file.length();
    }

    public ProgressRequestBody(MediaType contentType, File file, long startPosition, long endPosition, ProgressCallback callback) {
        this.contentType = contentType;
        this.file = file;
        this.callback = callback;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    @Override
    public @Nullable
    MediaType contentType() {
        return contentType;
    }

    @Override
    public long contentLength() {
        return endPosition - startPosition;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        Source source = null;
        try {
            source = Okio.source(file);
            BufferedSource bufferedSource = Okio.buffer(source);
            long totalBytesRead = 0;
            bufferedSource.skip(startPosition);
            long readBytes = 8192;
            long size = endPosition - startPosition;
            for (long readCount; (readCount = bufferedSource.read(sink.buffer(), readBytes)) != -1; ) {
                totalBytesRead += readCount;
                callback.uploadProgress(totalBytesRead);
                if (size - totalBytesRead < 8192) {
                    readBytes = size - totalBytesRead;
                }
                if (totalBytesRead >= (endPosition - startPosition)) {
                    break;
                }
            }
            //Log.i(TAG, "writeTo: " + size + " bytes");
        } finally {
            Util.closeQuietly(source);
        }
    }

    public interface ProgressCallback {
        void uploadProgress(long totalBytesRead);
    }
}