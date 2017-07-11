package com.ecjtu.sharebox.util.photoutil

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import java.io.File


/**
 * Created by KerriGan on 2017/7/11.
 */
class CapturePhotoHelper(fragmentActivity: FragmentActivity) {
    private var mActivity: FragmentActivity? = null

    companion object {
        private val TAKE_PHOTO = 0x1001
        private val PHOTO_RESULT = 0x1002
        private val IMAGE_PATH = "/sdcard/"
    }

    init {
        mActivity=fragmentActivity
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == TAKE_PHOTO) {
                val picture = File(IMAGE_PATH, "temp.jpg")
                photoZoom(Uri.fromFile(picture))
            }

            if (requestCode == PHOTO_RESULT) {
                //get corp image
            }
        }
    }

    // 图片缩放
    fun photoZoom(uri: Uri) {
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
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(File(IMAGE_PATH + "head.png")))
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString())
        mActivity?.startActivityForResult(intent, PHOTO_RESULT)
    }

    fun takePhoto(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(File(IMAGE_PATH, "temp.jpg")))
        mActivity?.startActivityForResult(intent, TAKE_PHOTO)
    }

}