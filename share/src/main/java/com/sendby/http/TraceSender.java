package com.sendby.http;

import android.content.Context;
import android.provider.Settings;

public class TraceSender {

    public static String getAndroidID(Context context) {
        String androidId = null;
        try {
            String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (id != null && !id.equals("9774d56d682e549c")) {
                androidId = id;
            } else {
                androidId = new DeviceUuidFactory(context).getDeviceUuid().toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new DeviceUuidFactory(context).getDeviceUuid().toString();
        }
        return androidId;
    }

}
