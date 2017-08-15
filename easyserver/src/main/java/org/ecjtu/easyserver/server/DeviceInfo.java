package org.ecjtu.easyserver.server;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by KerriGan on 2017/7/19.
 */
public class DeviceInfo implements Serializable{
    private String name;
    private String ip = "";
    private int port = 0;
    private String icon = "";
    private Map<String, List<String>> fileMap;
    private long updateTime;

    public DeviceInfo(String name, String ip, int port, String icon, Map<String, List<String>> fileMap) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.icon = icon;
        this.fileMap = fileMap;
        this.updateTime= System.currentTimeMillis();
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
        setUpdateTime(System.currentTimeMillis());
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
        setUpdateTime(System.currentTimeMillis());
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
        setUpdateTime(System.currentTimeMillis());
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
        setUpdateTime(System.currentTimeMillis());
    }

    public Map<String, List<String>> getFileMap() {
        return fileMap;
    }

    public void setFileMap(Map<String, List<String>> fileMap) {
        this.fileMap = fileMap;
        setUpdateTime(System.currentTimeMillis());
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof DeviceInfo)) return false;
        DeviceInfo info= (DeviceInfo) obj;
        if(info.ip.equals(this.ip)){
            return true;
        }
        return super.equals(obj);
    }
}
