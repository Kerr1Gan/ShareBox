package com.ethan.and.async

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.flybd.sharebox.util.file.FileUtil
import java.io.File

/**
 * Created by Ethan_Xiang on 2017/8/24.
 */
class FindAllFilesHelper(val context: Context) {

    companion object {
        private const val TAG = "FindAllFilesHelper"
    }

    private var mHandler: Handler? = null

    private var mHandlerThread: HandlerThread? = null

    private val mTaskList = arrayOf("Movie", "Music", "Photo", "Doc", "Apk", "Rar")

    private var mFilesMap: MutableMap<String, List<String>>? = null

    private var mCallback: ((map: MutableMap<String, List<String>>) -> Unit)? = null

    private var mProgressListener: ((taskIndex: Int, taskSize: Int) -> Unit)? = null

    init {
        mHandlerThread = HandlerThread(TAG)
        mHandlerThread?.start()
        val looper = mHandlerThread?.looper
        mHandler = object : Handler(looper) {
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                if (mFilesMap == null) {
                    mFilesMap = linkedMapOf()
                }
                val index = msg!!.what
                findFilesWithType(context, mTaskList.get(index), mFilesMap!!)

                mProgressListener?.invoke(index + 1, mTaskList.size)

                if (index == mTaskList.size - 1 && mHandlerThread?.isInterrupted == false) {
                    mCallback?.invoke(mFilesMap!!)
                }
            }
        }
    }

    fun startScanning(callback: (map: MutableMap<String, List<String>>) -> Unit) {
        mCallback = callback
        for (index in 0 until mTaskList.size) {
            mHandler?.obtainMessage(index)?.sendToTarget()
        }
    }

    fun setProgressCallback(listener: ((taskIndex: Int, taskSize: Int) -> Unit)?) {
        mProgressListener = listener
    }

    fun release() {
        mHandler?.removeCallbacksAndMessages(null)
        mHandlerThread?.quit()
        mHandlerThread?.interrupt()
    }

    private fun findFilesWithType(context: Context, type: String, map: MutableMap<String, List<String>>) {
        var list: MutableList<File>? = null
        when (type) {
            "Movie" -> {
                list = FileUtil.getAllMediaFile(context, null)
                list.reverse()
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.put("Movie", strList)
            }
            "Music" -> {
                list = FileUtil.getAllMusicFile(context, null)
                list.reverse()
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.put("Music", strList)
            }
            "Photo" -> {
                list = FileUtil.getImagesByDCIM(context)
                list.reverse()
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.put("Photo", strList)
            }
            "Doc" -> {
                list = FileUtil.getAllDocFile(context, null)
                list.reverse()
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.put("Doc", strList)
            }
            "Apk" -> {
                list = FileUtil.getAllApkFile(context, null)
                list.reverse()
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.put("Apk", strList)
            }
            "Rar" -> {
                list = FileUtil.getAllRarFile(context, null)
                list.reverse()
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.put("Rar", strList)
            }
        }
    }

}