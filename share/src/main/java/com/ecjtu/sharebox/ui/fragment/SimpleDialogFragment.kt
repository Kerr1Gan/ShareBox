package com.ecjtu.sharebox.ui.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Ethan_Xiang on 2017/10/20.
 */
class SimpleDialogFragment : AppCompatDialogFragment {

    private var mDialog: Dialog? = null
    private var mReset = false

    constructor() : super()

    @SuppressLint("ValidFragment")
    constructor(dialog: Dialog) {
        mDialog = dialog
        if (mDialog is IActivityResult) {
            (mDialog as IActivityResult).setFragmentHost(this)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val local = mDialog
        return if (local != null) {
            mReset = false
            local
        } else {
            mReset = true
            AlertDialog.Builder(context).create()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (mReset) {
            dismiss()
        }
        return super.onCreateView(inflater, container, savedInstanceState)
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