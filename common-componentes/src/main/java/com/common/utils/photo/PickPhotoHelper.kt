package com.common.utils.photo

import android.app.Activity
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import com.common.utils.file.FileUtil
import java.io.File

/**
 * Created by KerriGan on 2017/7/12.
 */
class PickPhotoHelper(fragmentActivity: FragmentActivity) : CropPhotoHelper() {
    companion object {
        val TAKE_PHOTO = 0x1002
    }

    private var mActivity: FragmentActivity? = null

    init {
        mActivity = fragmentActivity
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val path = Environment.getExternalStorageDirectory().absolutePath
            if (requestCode == TAKE_PHOTO) {
                photoZoom(data?.data!!, mActivity!!, path + "/head.png")
            }

            if (requestCode == PHOTO_RESULT) {
                //get corp image
                val file = File(path + "/head.png")
                FileUtil.copyFile2InternalPath(file, "head.png", mActivity!!)
                file.delete()
            }
        }
    }


    fun takePhoto() {
        val intent = Intent(Intent.ACTION_PICK, null)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        mActivity?.startActivityForResult(intent, TAKE_PHOTO)
    }

    override fun clearCache() {
        val path = Environment.getExternalStorageDirectory().absolutePath
        File(path + "/head.png").delete()
    }
}