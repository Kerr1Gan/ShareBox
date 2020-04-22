package com.ethan.and.ui.sendby.http.bean;

import com.google.gson.annotations.SerializedName;

public class HttpResponse<T> {

    // 保护混淆后的构造函数不被清理
    private static final String TAG = new HttpResponse().getClass().getSimpleName();

    @SerializedName("status")
    int status;
    @SerializedName("info")
    String info;
    @SerializedName("data")
    T data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
