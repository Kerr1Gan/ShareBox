package com.newindia.sharebox.presenter

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View

/**
 * Created by KerriGan on 2016/9/17.
 */
abstract class Delegate<T>(val owner: T) {

    fun findViewById(resId: Int): View? {
        if (owner is Activity) {
            return owner.findViewById(resId)
        }
        return null
    }

    val mContext: Context?
        get() {
            if (owner is Context) {
                return owner
            }
            return null
        }

    val mResources: Resources?
        get() {
            if (owner is Context) {
                return owner.resources
            }
            return null
        }

    val mIntent: Intent?
        get() {
            if (owner is Activity) {
                return owner.intent
            }
            return null
        }

    val mApplication: Application?
        get() {
            if (owner is Activity) {
                return owner.application
            }
            return null
        }

    val mLayoutInflater: LayoutInflater?
        get() {
            if (owner is Activity) {
                return owner.layoutInflater
            }
            return null
        }

    val mMenuInflater: MenuInflater?
        get() {
            if (owner is Activity) {
                return owner.menuInflater
            }
            return null
        }

    fun getSystemService(serviceName: String): Any? {
        if (owner is Activity) {
            return owner.getSystemService(serviceName)
        }
        return null
    }
}
