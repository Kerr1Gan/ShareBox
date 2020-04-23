package com.ethan.and.ui.sendby.entity;

import com.google.gson.annotations.SerializedName;

public class KeyEntity {

    private static final String STUB = new KeyEntity().getClass().getSimpleName();
    @SerializedName("key")
    String key;
    @SerializedName("server")
    String server;

    public KeyEntity() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
