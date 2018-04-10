package com.ecjtu.sharebox.ui.state

import android.content.Context
import android.view.View
import java.lang.ref.WeakReference


/**
 * Created by Ethan_Xiang on 2017/8/31.
 */
open class StateMachine(val context: Context, private val arrayRes: Int? = null, target: View? = null) {
    private val mWeak: WeakReference<View?> = WeakReference(target)
    private var mResArray: IntArray? = null
    private var mStringArray: Array<String>? = null
    private var mIntegerArray: IntArray? = null

    fun getArrayRefByIndex(index: Int): Int? {
        if (arrayRes != null) {
            val typeArr = context.resources.obtainTypedArray(arrayRes)
            val len = typeArr.length()
            mResArray = IntArray(len)
            for (i in 0 until len)
                mResArray!![i] = typeArr.getResourceId(i, 0)
            typeArr.recycle()
        } else {
            mResArray = null
        }
        return mResArray?.getOrNull(index)
    }

    fun getArrayStringByIndex(index: Int): String? {
        if (arrayRes != null) {
            val typeArr = context.resources.obtainTypedArray(arrayRes)
            val len = typeArr.length()
            mStringArray = Array<String>(len, { "" })
            for (i in 0 until len)
                mStringArray!![i] = typeArr.getString(i)
            typeArr.recycle()
        } else {
            mStringArray = null
        }
        return mStringArray?.get(index)
    }

    fun getArrayIntegerByIndex(index: Int): Int? {
        if (arrayRes != null) {
            val typeArr = context.resources.obtainTypedArray(arrayRes)
            val len = typeArr.length()
            mIntegerArray = IntArray(len, { 0 })
            for (i in 0 until len)
                mIntegerArray!![i] = typeArr.getInteger(index, 0)
            typeArr.recycle()
        } else {
            mIntegerArray = null
        }
        return mIntegerArray?.get(index)
    }

    open fun getArrayValueByKey(key: String): Int? {
        //override it
        return null
    }

    open fun updateView(key: String) {
        //override it
    }

    open fun updateView(index: Int) {
        //override it
    }

    open fun getTarget(): View? {
        return mWeak.get()
    }
}