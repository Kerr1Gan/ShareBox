package com.ecjtu.sharebox.ui.state

import android.content.Context
import android.view.View
import java.lang.ref.WeakReference


/**
 * Created by Ethan_Xiang on 2017/8/31.
 */
open class StateMachine(val context: Context, arrayRes: Int? = null, target: View? = null) {
    private val mResArray: IntArray?
    private val mWeak: WeakReference<View?> = WeakReference(target)

    init {
        if (arrayRes != null) {
            val typeArr = context.resources.obtainTypedArray(arrayRes)
            val len = typeArr.length()
            mResArray = IntArray(len)
            for (i in 0 until len)
                mResArray[i] = typeArr.getResourceId(i, 0)
            typeArr.recycle()
        } else {
            mResArray = null
        }
    }

    fun getArrayValueByIndex(index: Int): Int? {
        return mResArray?.getOrNull(index)
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