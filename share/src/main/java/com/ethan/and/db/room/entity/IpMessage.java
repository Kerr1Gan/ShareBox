package com.ethan.and.db.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "ip_message")
public class IpMessage {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private int id;

    @ColumnInfo(name = "ip")
    private String ip;

    @ColumnInfo(name = "port")
    private String port;

    public IpMessage(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IpMessage)) return false;
        IpMessage ipMessage = (IpMessage) o;
        return id == ipMessage.id &&
                ip.equals(ipMessage.ip) &&
                port.equals(ipMessage.port);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
