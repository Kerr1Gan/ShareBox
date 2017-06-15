package com.ecjtu.sharebox

import android.app.Application
import android.content.Context
import android.os.Environment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory
import com.bumptech.glide.load.DecodeFormat
import android.os.Environment.getExternalStorageDirectory
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemoryCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.module.GlideModule
import com.bumptech.glide.module.LibraryGlideModule
import org.ecjtu.channellibrary.wifidirect.WifiDirectManager


/**
 * Created by KerriGan on 2017/6/9 0009.
 */
class MainApplication:Application(){


    private val mSavedStateInstance=HashMap<String,Unit>()

    override fun onCreate() {
        super.onCreate()

        var module=SimpleGlideModule()
        var builder=GlideBuilder()
        module.applyOptions(this,builder)

        var glide=builder.build(this)

        Glide.init(glide)

        WifiDirectManager.getInstance(this)

        initSavedState()
    }

    fun getSavedStateInstance():Map<String,Unit>{
        return mSavedStateInstance
    }

    private fun initSavedState(){

    }


    inner class SimpleGlideModule : AppGlideModule() {
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
}