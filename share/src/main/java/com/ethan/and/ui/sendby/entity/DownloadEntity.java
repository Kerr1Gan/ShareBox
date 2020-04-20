package com.ethan.and.ui.sendby.entity;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class DownloadEntity {

    private static final String STUB = new DownloadEntity().getClass().getSimpleName();

    @SerializedName("id")
    int id;
    @SerializedName("url")
    String url = "";
    @SerializedName("path")
    String path = "";

    public DownloadEntity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DownloadEntity) {
            DownloadEntity anotherString = (DownloadEntity) obj;
            return anotherString.getUrl().equalsIgnoreCase(this.url);
        }
        return super.equals(obj);
    }
}
