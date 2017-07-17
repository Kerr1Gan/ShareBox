package org.ecjtu.easyserver.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by KerriGan in 2016.4.25
 */
public class FileUtil {
    private static String type = "*/*";
    public static String ip = "127.0.0.1";
    public static String deviceDMRUDN = "0";
    public static String deviceDMSUDN = "0";
    public static int port = 0;


    public static String getFileType(String uri) {
        if (uri == null) {
            return type;
        }

        if (uri.endsWith(".mp3")) {
            return "audio/mpeg";
        }

        if (uri.endsWith(".mp4")) {
            return "video/mp4";
        }

        return type;
    }

    public static String getDeviceDMRUDN() {
        return deviceDMRUDN;
    }

    public static String getDeviceDMSUDN() {
        return deviceDMSUDN;
    }


    public static boolean mkdir(String name) {
        boolean bool = false;

        boolean state = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (state) {
            File f = Environment.getExternalStorageDirectory();
            String path = f.getPath();
            String dir = path + "/" + name + "/";
            File file = new File(dir);
            if (!file.exists()) {
                bool = file.mkdir();
            } else {
            }

        } else {
        }


        return bool;
    }


    public static boolean copyFile2InternalPath(File file, String name, Context context) {
        File root = context.getFilesDir();
        FileInputStream fis = null;
        BufferedOutputStream buf = null;
        try {
            fis = new FileInputStream(file);
            File temp = new File(root.getAbsolutePath(), name);
            if (temp.exists()) temp.delete();
            buf = new BufferedOutputStream(new FileOutputStream(temp));
            byte[] arr = new byte[1024 * 5];
            int len = fis.read(arr);
            while (len > 0) {
                buf.write(arr);
                len = fis.read(arr);
            }
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
            }
            try {
                if (buf != null)
                    buf.close();
            } catch (IOException e) {
            }
        }
        return true;
    }

}
