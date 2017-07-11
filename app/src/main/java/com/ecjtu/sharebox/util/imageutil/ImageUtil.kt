package com.ecjtu.sharebox.util.imageutil

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.FileOutputStream
import java.lang.Exception

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

}