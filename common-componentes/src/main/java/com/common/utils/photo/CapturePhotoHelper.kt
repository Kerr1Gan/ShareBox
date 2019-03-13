package com.common.utils.photo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import com.common.utils.file.FileUtil
import java.io.File


/**
 * Created by KerriGan on 2017/7/11.
 */
class CapturePhotoHelper(fragmentActivity: FragmentActivity) : CropPhotoHelper(){
    private var mActivity: FragmentActivity? = null

    companion object {
        private const val TAKE_PHOTO = 0x1001
        private val IMAGE_PATH = Environment.getExternalStorageDirectory().absolutePath
    }

    init {
        mActivity=fragmentActivity
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == TAKE_PHOTO) {
                val picture = File(IMAGE_PATH, "temp.jpg")
                photoZoom(Uri.fromFile(picture),mActivity!!, IMAGE_PATH +"/head.png")
            }

            if (requestCode == PHOTO_RESULT) {
                //get corp image
                val file=File(IMAGE_PATH +"/head.png")
                FileUtil.copyFile2InternalPath(file,"head.png",mActivity!!)

                File(IMAGE_PATH +"/temp.jpg").delete()
                file.delete()
            }
        }
    }


    fun takePhoto(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(File(IMAGE_PATH, "temp.jpg")))
        mActivity?.startActivityForResult(intent, TAKE_PHOTO)
    }

    override fun clearCache() {
        File(IMAGE_PATH +"/temp.jpg").delete()
        File(IMAGE_PATH +"/head.png").delete()
    }
}