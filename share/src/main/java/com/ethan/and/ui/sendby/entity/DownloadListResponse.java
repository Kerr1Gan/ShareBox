package com.ethan.and.ui.sendby.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DownloadListResponse extends HttpResponse<List<DownloadListResponse.DownloadItem>> {

    // 保护混淆后的构造函数不被清理
    private static final String TAG = new DownloadListResponse().getClass().getSimpleName();

    public static class DownloadItem {

        // 保护混淆后的构造函数不被清理
        private static final String TAG = new DownloadItem().getClass().getSimpleName();
        @SerializedName("fileName")
        String fileName;
        @SerializedName("url")
        String url;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
