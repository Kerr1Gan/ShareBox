package com.ecjtu.sharebox.model;

/**
 * Created by Ethan_Xiang on 2017/10/25.
 */

public class DeviceModel {

    private String name;

    private int port;

    private String icon;

    public DeviceModel(){
    }

    public DeviceModel(String name, int port, String icon) {
        this.name = name;
        this.port = port;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
