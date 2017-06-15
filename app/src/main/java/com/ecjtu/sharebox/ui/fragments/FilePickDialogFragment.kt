package com.ecjtu.sharebox.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDialogFragment
import com.ecjtu.sharebox.ui.dialog.FilePickDialog

/**
 * Created by KerriGan on 2017/6/11.
 */
class FilePickDialogFragment(activity:AppCompatActivity) : AppCompatDialogFragment() {

    private var mActivity:AppCompatActivity?=null

    init {
        mActivity=activity
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return FilePickDialog(context, mActivity)
    }

}