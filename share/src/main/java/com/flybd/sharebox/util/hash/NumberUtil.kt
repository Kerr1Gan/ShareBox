package com.flybd.sharebox.util.hash

/**
 * Created by Ethan_Xiang on 2017/8/14.
 */
object NumberUtil{
    fun getUnsignedByte(value: Byte): Int {
        return value.toInt() and 0xFF
    }

    fun getUnsignedShort(value: Short):Int{
        return value.toInt() and 0xFFFF
    }

    fun getUnsignedInt(value: Int): Long{
        return value.toLong() and 0xFFFFFFFF
    }
}