package com.sendby.entity;

import com.google.gson.annotations.SerializedName;

public class ConfigEntity {


    /**
     * chunkSize : 10485760
     * chunkedStreamingMode : 10485760
     */

    @SerializedName("chunkSize")
    private int chunkSize;
    @SerializedName("chunkedStreamingMode")
    private int chunkedStreamingMode;

    private static final String STUB = new ConfigEntity().getClass().getSimpleName();

    public ConfigEntity() {
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkedStreamingMode() {
        return chunkedStreamingMode;
    }

    public void setChunkedStreamingMode(int chunkedStreamingMode) {
        this.chunkedStreamingMode = chunkedStreamingMode;
    }
}
