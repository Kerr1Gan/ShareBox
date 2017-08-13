package com.ecjtu.sharebox

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.support.v4.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.ecjtu.sharebox.service.MainService
import org.ecjtu.channellibrary.wifidirect.WifiDirectManager
import org.ecjtu.easyserver.server.ServerManager
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.util.*


/**
 * Created by KerriGan on 2017/6/9 0009.
 */
class MainApplication:Application(){


    private val mSavedInstance =HashMap<String,Any>()

    companion object {
        var sInstance:MainApplication? =null
    }

    override fun onCreate() {
        super.onCreate()
        sInstance=this
        var module=SimpleGlideModule()
        var builder=GlideBuilder()
        module.applyOptions(this,builder)

        var glide=builder.build(this)

        Glide.init(glide)

        WifiDirectManager.getInstance(this)

        LocalBroadcastManager.getInstance(this)

        initSavedState()

        startService(Intent(this,MainService::class.java))

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
        }
    }

    fun getSavedInstance():MutableMap<String,Any>{
        return mSavedInstance
    }

    private fun initSavedState(){
        ServerManager.getInstance().setIconPath(filesDir.absolutePath+"/"+Constants.ICON_HEAD)
        ServerManager.getInstance().setContext(applicationContext)
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
             builder.setDiskCache(InternalCacheDiskCacheFactory( context , diskCacheSize )) ;
            // builder.setDiskCache( new InternalCacheDiskCacheFactory( context , diskCachePath , diskCacheSize  )) ;
            //自定义磁盘缓存：这种缓存存在SD卡上，所有的应用都可以访问到
//            builder.setDiskCache(DiskLruCacheFactory(diskCachePath, diskCacheSize))

            //Memory Cache
            builder.setMemoryCache(LruResourceCache(24*1024*1024))
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
}
