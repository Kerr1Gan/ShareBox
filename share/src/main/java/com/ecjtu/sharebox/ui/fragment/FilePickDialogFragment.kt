package com.ecjtu.sharebox.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatDialogFragment
import com.ecjtu.sharebox.ui.dialog.FilePickDialog

/**
 * Created by KerriGan on 2017/6/11.
 */
class FilePickDialogFragment : AppCompatDialogFragment {

    private var mActivity: FragmentActivity? = null

    constructor() : super() {

    }

    constructor(activity: FragmentActivity) : super() {
        mActivity = activity
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return FilePickDialog(context, mActivity)
    }

}