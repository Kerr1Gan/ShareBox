package com.ecjtu.sharebox.util.fileutils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import java.io.*


import java.lang.Exception
import java.util.*


/**
 * Created by KerriGan on 2017/6/13.
 */
object FileUtil {

    fun getAllMediaFile(context: Context, search: ISearch?): ArrayList<File> {

        var linkType = " like '%.mp4'" + " or _data like '%.wmv'"
        linkType += " or _data like '%.avi' or _data like '%.rmvb'" + " or _data like '%.mkv'"
        return searchFilesByType(context, linkType, search)
    }

    fun getAllMusicFile(context: Context, search: ISearch?): ArrayList<File> {
        val linkType = " like '%.mp3' or _data like '%.wav' "
        return searchFilesByType(context, linkType, search)
    }

    fun getAllApkFile(context: Context, search: ISearch?): ArrayList<File> {
        val linkType = " like '%.apk'"
        return searchFilesByType(context, linkType, search)
    }


    fun getAllImageFile(context: Context, search: ISearch?): ArrayList<File> {
        val linkType = " like '%.jpg' or _data like '%.png'"
        return searchFilesByType(context, linkType, search)
    }

    fun getAllDocFile(context: Context, search: ISearch?): ArrayList<File> {
        val linkType = " like '%.txt' or _data like '%.doc'"
        return searchFilesByType(context, linkType, search)
    }

    fun getAllRarFile(context: Context, search: ISearch?): ArrayList<File> {
        val linkType = " like '%.rar' or _data like '%.zip' or _data like '%.gzip' or _data like '%.bz2'" + " or _data like '%7-zip'"
        return searchFilesByType(context, linkType, search)
    }

    fun searchFilesByType(context: Context, linkType: String, search: ISearch?): ArrayList<File> {

        val fileList = ArrayList<File>()
        val cr = context.contentResolver
        if (cr != null) {
            val uri = android.provider.MediaStore.Files.getContentUri("external")
            val c = cr.query(uri, arrayOf(android.provider.MediaStore.Files.FileColumns.DATA, android.provider.MediaStore.Files.FileColumns.SIZE),
                    android.provider.MediaStore.Files.FileColumns.DATA + linkType, null, null)

            if (c != null) {
                c.moveToFirst()
                while (!c.isAfterLast && !Thread.interrupted()) {
                    val filePath = c.getString(
                            c.getColumnIndex(android.provider.MediaStore
                                    .Files.FileColumns.DATA)) ?: continue
                    val f = File(filePath)

                    if (f.isDirectory || !f.canRead()) {
                        c.moveToNext()
                        continue
                    }

                    fileList.add(f)
                    search?.search(f)

                    c.moveToNext()
                }
                c.close()
            }
        }
        return fileList
    }

    public interface ISearch {
        fun search(file: File)
    }


    fun getAppThumbnail(context: Context, app: File): Bitmap? {
        val pm = context.packageManager

        val info = pm.getPackageArchiveInfo(app.path, PackageManager.GET_ACTIVITIES)

        if (info != null) {
            val appInfo = info.applicationInfo

            appInfo.sourceDir = app.path
            appInfo.publicSourceDir = app.path

            return (appInfo.loadIcon(pm) as BitmapDrawable).bitmap
        }

        return null
    }

    fun getFileImageByType(path: String): Bitmap? {
        var code = -1
        if (path.endsWith(".doc"))
            code = 0
        else if (path.endsWith(".html"))
            code = 1
        else if (path.endsWith(".movie"))
            code = 2
        else if (path.endsWith(".mp3"))
            code = 3
        else if (path.endsWith(".pdf"))
            code = 4
        else if (path.endsWith(".ppt"))
            code = 5
        else if (path.endsWith(".psd"))
            code = 6
        else if (path.endsWith(".rar"))
            code = 7
        else if (path.endsWith(".txt"))
            code = 8
        else if (path.endsWith(".xls"))
            code = 9
        else if (path.endsWith(".zip"))
            code = 10

        var b: Bitmap? = null
        when (code) {
            0 -> b = getBitmapByFactory("doc")
            1 -> b = getBitmapByFactory("html")
            2 -> b = getBitmapByFactory("movie")
            3 -> b = getBitmapByFactory("mp3")
            4 -> b = getBitmapByFactory("pdf")
            5 -> b = getBitmapByFactory("ppt")
            6 -> b = getBitmapByFactory("psd")
            7 -> b = getBitmapByFactory("rar")
            8 -> b = getBitmapByFactory("txt")
            9 -> b = getBitmapByFactory("xls")
            10 -> b = getBitmapByFactory("zip")
        }

        return b
    }

    fun getBitmapByFactory(key: String): Bitmap? {
        val b: Bitmap? = null
        //        ImageElement ele= (ImageElement) ObjectsPool.getInstance().getElement(key);
        //        if(ele!=null && !ele.isRecycled())
        //        {
        //            b=ele.getBitmap();
        //        }
        return b
    }


    var MOVIE_FORMAT = arrayOf(".mp4", ".avi", ".mkv", ".rmvb", ".wmv")
    var MP3_FORMAT = arrayOf(".mp3", ".wav")
    var IMG_FORMAT = arrayOf(".jpg", ".png")
    var APP_FORMAT = arrayOf(".apk")
    var RAR_FORMAT = arrayOf(".rar", ".zip", ".gzip", ".bz2", ".7-zip")
    var DOC_FORMAT = arrayOf(".doc", ".ppt", ".psd", ".txt", ".xls")


    fun getMediaFileTypeByName(name: String): MediaFileType {
        var name = name
        name = name.toLowerCase()
        for (i in MOVIE_FORMAT.indices) {
            if (name.endsWith(MOVIE_FORMAT[i])) {
                return MediaFileType.MOVIE
            }
        }

        for (i in MP3_FORMAT.indices) {
            if (name.endsWith(MP3_FORMAT[i])) {
                return MediaFileType.MP3
            }
        }

        for (i in IMG_FORMAT.indices) {
            if (name.endsWith(IMG_FORMAT[i])) {
                return MediaFileType.IMG
            }
        }

        for (i in APP_FORMAT.indices) {
            if (name.endsWith(APP_FORMAT[i])) {
                return MediaFileType.APP
            }
        }

        for (i in RAR_FORMAT.indices) {
            if (name.endsWith(RAR_FORMAT[i])) {
                return MediaFileType.RAR
            }
        }

        for (i in DOC_FORMAT.indices) {
            if (name.endsWith(DOC_FORMAT[i])) {
                return MediaFileType.DOC
            }
        }
        return MediaFileType.UNKNOWN
    }

    fun getFileSize(fileSize: Float): String {
        val size = (fileSize * 1.0f).toFloat()
        var res: String? = null
        val num: Float
        if (size >= 1024 && size <= 1024 * 1024) {
            num = size / 1024
            res = num.toString()
            res = res.substring(0, res.indexOf('.') + 2)
            res += " KB"
        } else if (size >= 1024 * 1024 && size <= 1024 * 1024 * 1024) {
            num = size / 1024f / 1024f
            res = num.toString()
            res = res.substring(0, res.indexOf('.') + 2)
            res += " M"
        } else if (size >= 1024 * 1024 * 1024) {
            num = size / 1024f / 1024f / 1024f
            res = num.toString()
            res = res.substring(0, res.indexOf('.') + 2)
            res += " G"
        } else {
            res = size.toString()
            res += " B"
        }
        return res
    }

    fun getFileSize(file: File): String? {
        var res: String? = null

        try {
            val fs = FileInputStream(file)

            val size = (fs.available() * 1.0).toFloat()

            val num: Float
            if (size >= 1024 && size <= 1024 * 1024) {
                num = size / 1024
                res = num.toString()
                res = res.substring(0, res.indexOf('.') + 2)
                res += " KB"
            } else if (size >= 1024 * 1024 && size <= 1024 * 1024 * 1024) {
                num = size / 1024f / 1024f
                res = num.toString()
                res = res.substring(0, res.indexOf('.') + 2)
                res += " M"
            } else if (size >= 1024 * 1024 * 1024) {
                num = size / 1024f / 1024f / 1024f
                res = num.toString()
                res = res.substring(0, res.indexOf('.') + 2)
                res += " G"
            } else {
                res = size.toString()
                res += " B"
            }
            fs.close()
            return res
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }


    enum class MediaFileType {
        MOVIE,
        MP3,
        IMG,
        APP,
        RAR,
        DOC,
        UNKNOWN
    }

    val TX_PATH= arrayOf("/tencent/MicroMsg","/tencent/MobileQQ")

    fun foldFiles(input: MutableList<File>?, output: LinkedHashMap<String, MutableList<File>>,isFast:Boolean=false): Array<String>? {
        if (input == null || input.size == 0) return null
        val prefix = ArrayList<String>()
        output.put(TX_PATH[0], arrayListOf<File>())
        output.put(TX_PATH[1], arrayListOf<File>())

        for(f in input){
            var root=f.parent

            if (prefix.indexOf(root) < 0){
                prefix.add(root)
                var list = ArrayList<File>()
                output.put(root,list)
            }

        }

        for (f in input) {
            if (Thread.interrupted())
                return null
            val root = f.parent

            var list: MutableList<File>? = output[root]

            list?.add(f)

            if(!isFast){
                for (pre in prefix) {
                    if (Thread.interrupted()) return null
                    if (root.startsWith(pre)) {
                        val lst = output[pre]
                        if (lst?.indexOf(f) ?:0 < 0)
                            lst?.add(f)
                    }

                    if(root.contains(TX_PATH[0]) || root.contains(TX_PATH[1])){
                        val lst = output[if(root.contains(TX_PATH[0])) TX_PATH[0] else TX_PATH[1]]
                        if (lst?.indexOf(f) ?:0 < 0)
                            lst?.add(f)
                    }
                }
            }else{
                if(root.contains(TX_PATH[0]) || root.contains(TX_PATH[1])){
                    val lst = output[if(root.contains(TX_PATH[0])) TX_PATH[0] else TX_PATH[1]]
                    if (lst?.indexOf(f) ?:0 < 0)
                        lst?.add(f)
                }
            }

        }

        if(output.get(TX_PATH[0])?.size==0){
            output.remove(TX_PATH[0])
        }

        if(output.get(TX_PATH[1])?.size==0){
            output.remove(TX_PATH[1])
        }

        //sort paths
        val set = output.keys
        val names = set.toTypedArray()

        if(!isFast){
            for (i in names.indices) {
                for (j in i + 1..names.size - 1) {
                    val sizeF = sizeOfChar(names[i], '/')
                    val sizeL = sizeOfChar(names[j], '/')
                    if (sizeF > sizeL) {
                        val tmp = names[i]
                        names[i] = names[j]
                        names[j] = tmp
                    }
                }
            }
        }

        return names
    }

    fun sizeOfChar(str: String, c: Char): Int {
        var count = 0
        for (i in 0..str.length - 1) {
            if (str[i] == c) {
                count++
            }
        }
        return count
    }

    fun copyFile2InternalPath(file:File,name:String,context: Context):Boolean{
        var root=context.filesDir
        var fileInputStream:FileInputStream? = null
        var buf:BufferedOutputStream? = null
        try {
            fileInputStream=FileInputStream(file)
            buf=BufferedOutputStream(FileOutputStream(File(root.absoluteFile,name)))
            var arr=ByteArray(1024*5)
            var len=fileInputStream.read(arr)
            while (len>0){
                buf.write(arr)
                len=fileInputStream.read(arr)
            }
        }catch (e:Exception){
            return false
        }finally {
            fileInputStream?.close()
            buf?.close()
        }
        return true
    }
}

