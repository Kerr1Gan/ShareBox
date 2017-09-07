package com.ecjtu.sharebox.async

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.ecjtu.sharebox.util.file.FileUtil
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
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.put("Movie", strList)
            }
            "Music" -> {
                list = FileUtil.getAllMusicFile(context, null)
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.put("Music", strList)
            }
            "Photo" -> {
                list = FileUtil.getImagesByDCIM(context)
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.put("Photo", strList)
            }
            "Doc" -> {
                list = FileUtil.getAllDocFile(context, null)
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.put("Doc", strList)
            }
            "Apk" -> {
                list = FileUtil.getAllApkFile(context, null)
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.put("Apk", strList)
            }
            "Rar" -> {
                list = FileUtil.getAllRarFile(context, null)
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.put("Rar", strList)
            }
        }
    }

}