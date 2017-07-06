package com.ecjtu.sharebox.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDialogFragment
import com.ecjtu.sharebox.ui.dialog.FilePickDialog

/**
 * Created by KerriGan on 2017/6/11.
 */
class FilePickDialogFragment : AppCompatDialogFragment{

    private var mActivity:AppCompatActivity?=null

    constructor():super(){

    }

    constructor(activity: AppCompatActivity):super(){
        mActivity=activity
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return FilePickDialog(context, mActivity)
    }

}