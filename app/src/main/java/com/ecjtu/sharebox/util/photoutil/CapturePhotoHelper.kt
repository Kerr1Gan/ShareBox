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
class CapturePhotoHelper(fragmentActivity: FragmentActivity) :CropPhotoHelper(){
    private var mActivity: FragmentActivity? = null

    companion object {
        private val TAKE_PHOTO = 0x1001
        private val IMAGE_PATH = "/sdcard/"
    }

    init {
        mActivity=fragmentActivity
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == TAKE_PHOTO) {
                val picture = File(IMAGE_PATH, "temp.jpg")
                photoZoom(Uri.fromFile(picture),mActivity!!, IMAGE_PATH+"head.png")
            }

            if (requestCode == PHOTO_RESULT) {
                //get corp image
            }
        }
    }


    fun takePhoto(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(File(IMAGE_PATH, "temp.jpg")))
        mActivity?.startActivityForResult(intent, TAKE_PHOTO)
    }

}