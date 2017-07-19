package com.ecjtu.sharebox;

import java.util.List;
import java.util.Map;

/**
 * Created by KerriGan on 2017/7/19.
 */
public class DeviceInfo {
    public String name;
    public String ip = "";
    int port = 0;
    String icon = "";
    Map<String, List<String>> fileMap;

    public DeviceInfo(String name, String ip, int port, String icon, Map<String, List<String>> fileMap) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.icon = icon;
        this.fileMap = fileMap;
    }

    public DeviceInfo(String name, String ip) {
        this(name, ip, 0, "", null);
    }

    public DeviceInfo(String name, String ip, int port, String icon) {
        this(name, ip, port, icon, null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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

    public Map<String, List<String>> getFileMap() {
        return fileMap;
    }

    public void setFileMap(Map<String, List<String>> fileMap) {
        this.fileMap = fileMap;
    }
}
