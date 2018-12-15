package com.ecjtu.sharebox

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.support.multidex.MultiDexApplication
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.ecjtu.sharebox.parcel.FileExpandablePropertyCache
import com.ecjtu.sharebox.ui.main.MainActivity
import com.ecjtu.sharebox.ui.dialog.FilePickDialog
import com.google.android.gms.ads.MobileAds
import com.tencent.bugly.crashreport.CrashReport
import org.ecjtu.easyserver.server.DeviceInfo
import java.io.*
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by KerriGan on 2017/6/9 0009.
 */
class MainApplication : MultiDexApplication() {

    companion object {
        @JvmStatic
        private val FILE_CATEGORIES = arrayOf("Movie", "Music", "Photo", "Doc", "Apk", "Rar")
    }

    private val mSavedInstance = HashMap<String, Any>()

    private val mActivityList = ArrayList<WeakReference<Activity?>>()

    override fun onCreate() {
        super.onCreate()
        if (isAppMainProcess(BuildConfig.APPLICATION_ID)) {
            initMainProcess()
        } else {
            //child process
        }
    }

    private fun initMainProcess() {
        Log.i("ShareBox", "init main process")
        val module = SimpleGlideModule()
        val builder = GlideBuilder()
        module.applyOptions(this, builder)

        val glide = builder.build(this)

        Glide.init(glide)

//        WifiDirectManager.getInstance(this)

        LocalBroadcastManager.getInstance(this)

        initSavedState()

        initError()

        initSDK()

        loadCache()

        this.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {
            }

            override fun onActivityResumed(activity: Activity?) {
            }

            override fun onActivityStarted(activity: Activity?) {
            }

            override fun onActivityDestroyed(activity: Activity?) {
                val iter = mActivityList.iterator()
                while (iter.hasNext()) {
                    val obj = iter.next()
                    val act = obj.get()
                    if (activity == act || act == null) {
                        iter.remove()
                    }
                }
                if (activity is MainActivity) {
                    mSavedInstance.clear()
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            }

            override fun onActivityStopped(activity: Activity?) {
            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                mActivityList.add(WeakReference(activity))
            }
        })
    }

    fun closeApp() {
        var end = mActivityList.size - 1
        while (end >= 0) {
            val activity = mActivityList.get(end--).get()
            activity?.finish()
        }
        mActivityList.clear()
        System.exit(0)
    }

    fun closeActivitiesByIndex(start: Int, end: Int = mActivityList.size) {
        if (start >= end) return
        var index = 0
        val iter = mActivityList.iterator()
        while (iter.hasNext()) {
            val activity = iter.next()
            if (index++ in start..(end - 1)) {
                activity.get()?.finish()
                iter.remove()
            }
        }
    }

    fun getActivityByIndex(index: Int): Activity? {
        if (index >= getActivityLength()) return null
        return mActivityList[index].get()
    }

    fun getTopActivity(): Activity? {
        return mActivityList[mActivityList.size - 1].get()
    }

    fun getBottomActivity(): Activity? {
        if (getActivityLength() >= 1) return null
        return mActivityList[0].get()
    }

    fun getActivityLength(): Int {
        return mActivityList.size
    }

    fun getSavedInstance(): MutableMap<String, Any> {
        return mSavedInstance
    }

    private fun initSavedState() {
        val deviceInfo = DeviceInfo()
        deviceInfo.iconPath = filesDir.absolutePath + "/" + Constants.ICON_HEAD
        deviceInfo.fileMap = mutableMapOf()
        mSavedInstance.put(Constants.KEY_INFO_OBJECT, deviceInfo)
    }

    private fun initSDK() {
        CrashReport.initCrashReport(getApplicationContext(), getString(R.string.bugly_id), true)
        MobileAds.initialize(this, getString(R.string.admob_app_id))
    }

    private fun initError() {
        val exHandler = Thread.currentThread().uncaughtExceptionHandler
        Thread.currentThread().setUncaughtExceptionHandler { thread, ex ->
            //write error logs add in 2016/6/23 by KerriGan
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val root = Environment.getExternalStorageDirectory().absolutePath

                val dir = File(root)
                if (!dir.exists() || !dir.isDirectory)
                    dir.mkdirs()

                val file = java.io.File(root + "/error.txt")
                if (!file.exists())
                    file.delete()

                try {
                    file.createNewFile()
                    val output = FileOutputStream(file)

                    val sBuf = StringBuffer()
                    val context = getStackTrace(ex, sBuf, "", null).toString()

                    //write timestamp
                    val date = Date()
                    val time = "" + (date.month + 1) + "." + date.date + "." + date.hours + ":"
                    (+date.minutes).toString() + "\n"
                    output.write(time.toByteArray(charset("utf-8")))
                    output.write(context.toByteArray(charset("utf-8")))

                    output.close()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: NoSuchMethodException) {
                    e.printStackTrace()
                } catch (e: NoSuchFieldException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }

            }

            exHandler.uncaughtException(thread, ex)
        }
    }

    class SimpleGlideModule : AppGlideModule() {
        override fun applyOptions(context: Context, builder: GlideBuilder) {
            //定义缓存大小为100M
            val diskCacheSize = 100 * 1024 * 1024

            //自定义缓存 路径 和 缓存大小
//            val diskCachePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/glideCache"

            //提高图片质量
            builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888)

            //自定义磁盘缓存:这种缓存只有自己的app才能访问到
            builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSize));
            // builder.setDiskCache( new InternalCacheDiskCacheFactory( context , diskCachePath , diskCacheSize  )) ;
            //自定义磁盘缓存：这种缓存存在SD卡上，所有的应用都可以访问到
//            builder.setDiskCache(DiskLruCacheFactory(diskCachePath, diskCacheSize))

            //Memory Cache
            builder.setMemoryCache(LruResourceCache(24 * 1024 * 1024))
        }
    }

    @Throws(IOException::class, NoSuchMethodException::class, InvocationTargetException::class, IllegalAccessException::class, NoSuchFieldException::class)
    fun getStackTrace(throwable: Throwable, buffer: StringBuffer, indent: String, parentStack: Array<StackTraceElement>?): StringBuffer {
        buffer.append(throwable.toString())
        buffer.append("\n")

        val getInternalStackTraceMethod = Throwable::class.java.getDeclaredMethod("getInternalStackTrace")
        getInternalStackTraceMethod.isAccessible = true
        val stack = getInternalStackTraceMethod.invoke(throwable) as Array<StackTraceElement>

        getInternalStackTraceMethod.isAccessible = false


        val countDuplicatesMethod = Throwable::class.java.getDeclaredMethod("countDuplicates", Array<StackTraceElement>::class.java, Array<StackTraceElement>::class.java)
        countDuplicatesMethod.isAccessible = true
        if (stack != null) {
            val duplicates = if (parentStack != null)
                countDuplicatesMethod.invoke(throwable, stack, parentStack) as Int
            else
                0
            for (i in 0..stack.size - duplicates - 1) {
                buffer.append(indent)
                buffer.append("\tat ")
                buffer.append(stack[i].toString())
                buffer.append("\n")
            }

            if (duplicates > 0) {
                buffer.append(indent)
                buffer.append("\t... ")
                buffer.append(Integer.toString(duplicates))
                buffer.append(" more\n")
            }
        }
        countDuplicatesMethod.isAccessible = false

        // Print suppressed exceptions indented one level deeper.
        val field = Throwable::class.java.getDeclaredField("suppressedExceptions")
        field.isAccessible = true
        val suppressedExceptions = field.get(throwable) as List<Throwable>
        if (suppressedExceptions != null) {
            for (throwables in suppressedExceptions) {
                buffer.append(indent)
                buffer.append("\tSuppressed: ")
                getStackTrace(throwables, buffer, indent + "\t", stack)
            }
        }

        val cause = throwable.cause
        if (cause != null) {
            buffer.append(indent)
            buffer.append("Caused by: ")
            getStackTrace(cause, buffer, indent, stack)
        }

        field.isAccessible = false
        return buffer
    }

    /**
     * 判断是不是UI主进程，因为有些东西只能在UI主进程初始化
     */
    fun isAppMainProcess(packageName: String): Boolean {
        val pid = android.os.Process.myPid()
        val process = getAppNameByPID(this, pid)
        return packageName.equals(process)
    }

    /**
     * 根据Pid得到进程名
     */
    fun getAppNameByPID(context: Context, pid: Int): String {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (processInfo in manager.runningAppProcesses) {
            if (processInfo.pid == pid) {
                return processInfo.processName
            }
        }
        return ""
    }

    /**
     * 程序是否在前台运行
     *
     */
    fun isAppOnForeground(context: Context): Boolean {
        val activityManager = context.getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val packageName = context.getApplicationContext().getPackageName()
        /**
         * 获取Android设备中所有正在运行的App
         */
        val appProcesses = activityManager
                .runningAppProcesses ?: return false
        for (appProcess in appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName == packageName && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true
            }
        }
        return false
    }

    private fun loadCache() {
        val array = FILE_CATEGORIES
        val cache = FileExpandablePropertyCache(filesDir.absolutePath)
        for (key in array) {
            mSavedInstance.put(FilePickDialog.EXTRA_PROPERTY_LIST + key, cache.get(key))
        }
    }

    fun saveCache() {
        val array = FILE_CATEGORIES
        val cache = FileExpandablePropertyCache(filesDir.absolutePath)
        for (key in array) {
            val obj = mSavedInstance.get(FilePickDialog.EXTRA_PROPERTY_LIST + key)
            if (obj != null && (obj is List<*>)) {
                cache.put(key, obj)
            }
        }
    }
}
