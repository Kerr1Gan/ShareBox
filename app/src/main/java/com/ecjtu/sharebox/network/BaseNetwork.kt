package com.ecjtu.sharebox.network

import android.text.TextUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

/**
 * Created by Ethan_Xiang on 2017/7/14.
 */
abstract class BaseNetwork{
    companion object {
        const val TIME_OUT= 3 * 1000
        const val CHARSET= "UTF-8"
        const val HEADER_CONTENT_ENCODING="Content-Encoding"
        const val HEADER_CONTENT_LENGTH="Content-Length"
        const val HTTP_PREFIX="http://"
        const val CACHE_SIZE= 5 * 1024
    }

    object Method{
        const val OPTIONS="OPTIONS"
        const val GET="GET"
        const val HEAD="HEAD"
        const val POST="POST"
        const val PUT="PUT"
        const val DELETE="DELETE"
        const val TRACE="TRACE"
    }

    private var mCallback:IRequestCallback?=null

    private var mHttpUrlConnection:HttpURLConnection? =null

    fun setRequestCallback(callback: IRequestCallback){
        mCallback=callback
    }

    @JvmOverloads
    fun request(urlStr:String,mutableMap: MutableMap<String,String>? =null){
        var ex:Exception?=null

        var ret=""
        try {
            var url=URL(urlStr)
            mHttpUrlConnection=url.openConnection() as HttpURLConnection
            setupRequest(mHttpUrlConnection!!)
            var paramStr=setParams(mHttpUrlConnection!!,mutableMap)
            connect()
            ret=getContent(mHttpUrlConnection!!,paramStr)
        }catch (e:Exception){
            e.printStackTrace()
            ex=e
        }finally {
            if(ex!=null){
                mCallback?.onError(mHttpUrlConnection!!,ex)
            }else{
                mCallback?.onSuccess(mHttpUrlConnection!!,ret)
            }
            mHttpUrlConnection?.disconnect()
        }
    }

    open fun setupRequest(httpURLConnection: HttpURLConnection){
        httpURLConnection.doInput=true
        httpURLConnection.doOutput=true
        httpURLConnection.requestMethod= Method.GET
        httpURLConnection.connectTimeout=TIME_OUT
        httpURLConnection.readTimeout= TIME_OUT
        httpURLConnection.setRequestProperty(HEADER_CONTENT_ENCODING, CHARSET)
    }

    open fun setParams(httpURLConnection: HttpURLConnection,mutableMap: MutableMap<String,String>?=null):String{
        var ret=""
        mutableMap?.let {
            httpURLConnection.requestMethod=Method.POST

            var param:String?=null

            for(obj in mutableMap.entries){
                if(param?.length!=0){
                    param+="&"
                }
                param+="${obj.key}=${obj.value}"
            }
            httpURLConnection.setRequestProperty(HEADER_CONTENT_LENGTH, param?.toByteArray()?.size.toString())
            ret=param!!
        }
        return ret
    }

    open fun connect(){
        try{
            mHttpUrlConnection?.connect()
        }catch (io:IOException){
            throw io
        }
    }

    open fun getContent(httpURLConnection: HttpURLConnection,param:String? =null):String{
        var ret=""
        try {
            if(httpURLConnection.requestMethod == Method.POST){
                if(!TextUtils.isEmpty(param)){
                    httpURLConnection.outputStream.write(param?.toByteArray())
                }
            }
            if(httpURLConnection.responseCode==HttpURLConnection.HTTP_OK){
                var os=ByteArrayOutputStream()
                var temp=ByteArray(CACHE_SIZE,{ index -> 0})
                var `is`=httpURLConnection.inputStream

                var len:Int
                len=`is`.read(temp)
                while (len>0){
                    os.write(temp)
                    `is`.read(temp)
                }
                ret= String(os.toByteArray())
            }
        }catch (ex:Exception){
            ex.printStackTrace()
            throw ex
        }
        return ret
    }
}