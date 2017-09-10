package org.ecjtu.easyserver.server.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by KerriGan on 2017/9/9.
 */

public class ApkUtil {
    public static List<PackageInfo> getInstalledApps(Context context, boolean includeSystem) {
        ArrayList<PackageInfo> appList = new ArrayList<PackageInfo>(); //用来存储获取的应用信息数据
        PackageManager manager = context.getPackageManager();
        List<PackageInfo> packages = manager.getInstalledPackages(0);

        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            //Only display the non-system app info
            if (!includeSystem && (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                appList.add(packageInfo);//如果非系统应用，则添加至appList
            }

        }
        return appList;
    }

    public static String getInstallAppNameByPath(Context context, String path) {
        List<PackageInfo> appList = getInstalledApps(context, false);
        for (PackageInfo packageInfo : appList) {
            ApplicationInfo application = packageInfo.applicationInfo;
            if (packageInfo.applicationInfo.sourceDir.equals(path)) {
                return application.loadLabel(context.getPackageManager()).toString();
            }
        }
        return "";
    }

    public static String[] getInstallAppsNameByPathArray(Context context, String[] path) {
        List<PackageInfo> appList = getInstalledApps(context, false);
        String[] retArray = new String[appList.size()];
        for (PackageInfo packageInfo : appList) {
            ApplicationInfo application = packageInfo.applicationInfo;
            for (int i = 0; i < path.length; i++) {
                String childPath = path[i];
                if (packageInfo.applicationInfo.sourceDir.equals(childPath)) {
                    retArray[i] = application.loadLabel(context.getPackageManager()).toString();
                }
            }
        }
        return retArray;
    }

    public static Bitmap getAppThumbnail(Context context, File app) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(app.getPath(), PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = app.getPath();
            appInfo.publicSourceDir = app.getPath();
            return ((BitmapDrawable) appInfo.loadIcon(pm)).getBitmap();
        }
        return null;
    }
}
