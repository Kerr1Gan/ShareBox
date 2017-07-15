package com.ecjtu.sharebox.util.image

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import java.io.FileOutputStream
import java.lang.Exception
import java.util.HashMap

/**
 * Created by KerriGan on 2017/7/11.
 */

object ImageUtil{

    fun saveBitmap(bitmap:Bitmap, path:String, format: Bitmap.CompressFormat, quality:Int):Boolean{
        try {
            bitmap.compress(format,quality,FileOutputStream(path))
            return true
        }catch (e:Exception){
            return false
        }
    }

    /**
     * 使用android内部native库,不需要转utf-8编码
     * @param url httpUrl
     */
    fun createVideoThumbnail(url: String, width: Int, height: Int): Bitmap? {
        var bitmap: Bitmap? = null
        val retriever = MediaMetadataRetriever()
        val kind = MediaStore.Video.Thumbnails.MINI_KIND

        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(url, HashMap<String, String>())
            } else {
                retriever.setDataSource(url)
            }

            bitmap = retriever.frameAtTime
        } catch (ex: IllegalArgumentException) {
            // Assume this is a corrupt video File
        } catch (ex: RuntimeException) {
            // Assume this is a corrupt video File.
        } finally {
            try {
                retriever.release()
            } catch (ex: RuntimeException) {
                // Ignore failures while cleaning up.
            }

        }
        if (kind == android.provider.MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT)
        }
        return bitmap
    }
}