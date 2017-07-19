package org.ecjtu.easyserver.server.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by KerriGan on 2017/7/15.
 */

public class CacheUtil {

    public static String makeCache(String key, Bitmap bmp, int width, int height, Context context) {
        if (bmp == null || bmp.isRecycled())
            return null;

        File rootPath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        String localKey = key.replace("/", "_");

        String fileName = localKey + "_cache" + ".png";

        File tempFile = new File(rootPath, fileName);

        if (tempFile.exists()){
//            tempFile.delete();
            return tempFile.getAbsolutePath();
        }

        Bitmap saveBitmap = Bitmap.createScaledBitmap(bmp, width, height, true);

        try {
            tempFile.createNewFile();
            FileOutputStream out = new FileOutputStream(tempFile);
            saveBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();

            //there is a bug need to fix bitmap will recycle.in 2016.7.5 by KerriGan
//            saveBitmap.recycle();
            return rootPath.getPath() + fileName;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        saveBitmap.recycle();
        return null;
    }

    public static String getCachePath(Context context, String key) {
        String root = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
        String localKey = key.replace("/", "_");
        String filePath = root + "/" + localKey + "_cache" + ".png";
        return filePath;
    }

    public static Bitmap getBitmapByCache(Context context, String key) {
        File rootPath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Bitmap bmp = null;
        String localKey = key.replace("/", "_");
        String fileName = localKey + "_cache" + ".png";
        File tempFile = new File(rootPath, fileName);

        if (tempFile.exists()) {
            try {
                FileInputStream input = new FileInputStream(tempFile);
                bmp = BitmapFactory.decodeStream(input);
                if(bmp==null){
                    tempFile.delete();//delete wrong cache
                }
                return bmp;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return bmp;
    }
}
