package com.ecjtu.sharebox.util.cache;

import android.content.Context;
import android.os.Parcel;

import org.ecjtu.easyserver.server.DeviceInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by Ethan_Xiang on 2017/8/22.
 */

public class ParcelableWrapper {

    public static void saveDeviceInfo(Context context, DeviceInfo info) {
        DeviceInfoHelper helper = new DeviceInfoHelper(context.getFilesDir().getAbsolutePath());
        helper.persistObject("key", info);
    }

    public static DeviceInfo readDeviceInfo(Context context){
        DeviceInfoHelper helper = new DeviceInfoHelper(context.getFilesDir().getAbsolutePath());
        return helper.readObject("key");
    }


    private static class DeviceInfoHelper extends ParcelableFileCacheHelper {

        public DeviceInfoHelper(String path) {
            super(path);
        }

        @Override
        <T> T readParcel(Parcel parcel) {
            String name=parcel.readString();
            String ip=parcel.readString();
            int port=parcel.readInt();
            String icon=parcel.readString();
            Map<String,List<String>> map=parcel.readHashMap(ClassLoader.getSystemClassLoader());
            long updateTime=parcel.readLong();
            DeviceInfo info=new DeviceInfo(name,ip,port,icon,map);
            info.setUpdateTime(updateTime);
            return (T) info;
        }

        @Override
        <T> Parcel writeParcel(Parcel parcel, T object) {
            if (object instanceof DeviceInfo) {
                DeviceInfo localObj = (DeviceInfo) object;
                parcel.writeString(localObj.getName());
                parcel.writeString(localObj.getIp());
                parcel.writeInt(localObj.getPort());
                parcel.writeString(localObj.getIcon());
                parcel.writeMap(localObj.getFileMap());
                parcel.writeLong(localObj.getUpdateTime());
            }
            return parcel;
        }
    }
}
