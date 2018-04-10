package com.ecjtu.sharebox.util.hash

/**
 * Created by Ethan_Xiang on 2017/8/14.
 */
object HashUtil {
    fun BKDRHash(string: String): Long {
        val seed = 131
        var hash = 0L
        val len = string.length
        var index = 0
        while (index < len) {
            hash = hash * seed + (string[index++].toInt())
        }
        return (hash and 0x7FFFFFFF)
    }
}