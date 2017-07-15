package com.ecjtu.sharebox.util.photo

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import java.io.File

/**
 * Created by KerriGan on 2017/7/12.
 */
abstract class CropPhotoHelper{

    companion object {
        @JvmStatic protected val PHOTO_RESULT = 0x1000
    }

    // 图片缩放
    fun photoZoom(uri: Uri,fragmentActivity: FragmentActivity,outputPath:String) {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        intent.putExtra("crop", "true")
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 200)
        intent.putExtra("outputY", 200)
        intent.putExtra("return-data", false)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(File(outputPath)))
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString())
        fragmentActivity?.startActivityForResult(intent, PHOTO_RESULT)
    }

    abstract fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}