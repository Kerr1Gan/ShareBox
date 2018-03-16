package com.ecjtu.sharebox.ui.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDialogFragment

/**
 * Created by Ethan_Xiang on 2017/10/20.
 */
class SimpleDialogFragment : AppCompatDialogFragment {

    private var mDialog: Dialog? = null

    constructor() : super()

    @SuppressLint("ValidFragment")
    constructor(dialog: Dialog) {
        mDialog = dialog
        if(mDialog is IActivityResult){
            (mDialog as IActivityResult).setFragmentHost(this)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return mDialog!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (mDialog is IActivityResult) {
            (mDialog as IActivityResult).onActivityResult(requestCode, resultCode, data)
        }
    }

    interface IActivityResult {
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
        fun setFragmentHost(fragment: Fragment?)
        fun getFragmentHost(): Fragment?
    }
}