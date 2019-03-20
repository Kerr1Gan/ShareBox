package com.ethan.and.async

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.LruCache
import android.widget.ImageView
import com.common.utils.file.FileUtil
import java.io.File

/**
 * Created by KerriGan on 2017/6/18.
 */
class AppThumbTask : AsyncTask<File, Void, Bitmap?> {

    private var mLruCache: LruCache<String, Bitmap>? = null

    private var mContext: Context? = null

    private var mImageView: ImageView? = null

    constructor(lruCache: LruCache<String, Bitmap>, context: Context, imgView: ImageView) : super() {
        mLruCache = lruCache
        mContext = context
        mImageView = imgView
    }

    override fun doInBackground(vararg params: File?): Bitmap? {
        val f = params[0]

        val bit = FileUtil.getAppThumbnail(mContext!!, f!!)

        if (bit != null) {
            mLruCache?.put(f.absolutePath, bit)
        }

        return bit
    }

    override fun onPostExecute(result: Bitmap?) {
        super.onPostExecute(result)
        if (result != null) {
            mImageView?.setImageBitmap(result)
        }
        mLruCache = null
        mImageView = null
        mContext = null
    }

    override fun onCancelled() {
        super.onCancelled()
        mImageView = null
        mLruCache = null
        mContext = null
    }
}
