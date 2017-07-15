package com.ecjtu.sharebox.async

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.LruCache
import android.widget.ImageView
import com.ecjtu.sharebox.util.file.FileUtil
import java.io.File

/**
 * Created by KerriGan on 2017/6/18.
 */
class AppThumbTask:AsyncTask<File,Void,Bitmap?>{

    private var mLruCache:LruCache<String,Bitmap>? = null

    private var mContext: Context? =null

    private var mImageView:ImageView?=null
    constructor(lruCache: LruCache<String,Bitmap>,context: Context,imgView:ImageView):super(){
        mLruCache=lruCache
        mContext=context
        mImageView=imgView
    }

    override fun doInBackground(vararg params: File?): Bitmap? {
        var f=params[0]

        var bit = FileUtil.getAppThumbnail(mContext!!, f!!)

        if(bit!=null){
            mLruCache?.put(f.absolutePath, bit)
        }

        return bit
    }

    override fun onPostExecute(result: Bitmap?) {
        super.onPostExecute(result)
        if(result!=null){
            mImageView?.setImageBitmap(result)
        }
        mLruCache=null
        mImageView=null
    }

    override fun onCancelled() {
        super.onCancelled()
        mImageView=null
        mLruCache=null
    }
}
