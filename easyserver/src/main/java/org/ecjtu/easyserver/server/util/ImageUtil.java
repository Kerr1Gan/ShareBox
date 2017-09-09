package org.ecjtu.easyserver.server.util;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;

import java.io.FileOutputStream;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.RuntimeException;
import java.util.HashMap;

/**
 * Created by KerriGan on 2017/7/11.
 */

public class ImageUtil {

    public static boolean saveBitmap(Bitmap bitmap, String path, Bitmap.CompressFormat format, int quality) {
        try {
            bitmap.compress(format, quality,new FileOutputStream(path));
            return true;
        } catch (Exception e){
            return false;
        }
    }

    /**
     * 使用android内部native库,不需要转utf-8编码
     *
     * @param url httpUrl
     */
    public static Bitmap createVideoThumbnail(String url, int width, int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever =new MediaMetadataRetriever();
        int kind = MediaStore.Video.Thumbnails.MINI_KIND;

        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(url,new HashMap < String, String > ());
            } else {
                retriever.setDataSource(url);
            }

            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex){
            // Assume this is a corrupt video File
        } catch(RuntimeException ex){
            // Assume this is a corrupt video File.
        } finally{
            try {
                retriever.release();
            } catch (RuntimeException ex){
                // Ignore failures while cleaning up.
            }

        }
        if (kind == android.provider.MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }
}