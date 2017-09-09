package org.ecjtu.easyserver.server.util.cache;

import android.os.Parcel;

import org.ecjtu.easyserver.server.DeviceInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by KerriGan on 2017/8/26.
 */

public class ServerInfoParcelableHelper extends ParcelableFileCacheHelper {
    public ServerInfoParcelableHelper(String path) {
        super(path);
    }

    @Override
    <T> T readParcel(Parcel parcel) {
        return (T) readDeviceInfoFully(parcel);
    }

    @Override
    <T> Parcel writeParcel(Parcel parcel, T object) {
        DeviceInfo local = (DeviceInfo) object;
        writeDeviceInfo(parcel, local);
        if(local.getOtherDevices()!=null){
            int size = local.getOtherDevices().size();
            parcel.writeInt(local.getOtherDevices().size());
            List<DeviceInfo> list = local.getOtherDevices();
            for (int i = 0; i < size; i++) {
                writeDeviceInfo(parcel, list.get(i));
            }
        }
        return parcel;
    }

    private void writeDeviceInfo(Parcel parcel, DeviceInfo info) {
        parcel.writeString(info.getName());
        parcel.writeString(info.getIp());
        parcel.writeInt(info.getPort());
        parcel.writeString(info.getIcon());
        parcel.writeMap(info.getFileMap());
        parcel.writeLong(info.getUpdateTime());
    }

    private DeviceInfo readDeviceInfoFully(Parcel parcel) {
        DeviceInfo info1 = null;
        info1 = readDeviceInfo(parcel);
        int size = parcel.readInt();
        List<DeviceInfo> list = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            DeviceInfo info2 = readDeviceInfo(parcel);
            list.add(info2);
        }
        if (info1 != null) {
            info1.setOtherDevices(list);
        }
        return info1;
    }

    private DeviceInfo readDeviceInfo(Parcel parcel) {
        String name = parcel.readString();
        String ip = parcel.readString();
        DeviceInfo info = new DeviceInfo(name, ip);
        info.setPort(parcel.readInt());
        info.setIcon(parcel.readString());
        LinkedHashMap<String, List<String>> out = new LinkedHashMap<>();
        parcel.readMap(out, getClass().getClassLoader());
        info.setFileMap(out);
        info.setUpdateTime(parcel.readLong());
        return info;
    }
}
