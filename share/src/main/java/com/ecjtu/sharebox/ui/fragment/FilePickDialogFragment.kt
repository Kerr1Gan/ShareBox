package com.ecjtu.sharebox.ui.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.widget.ProgressBar
import android.widget.TextView
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.async.FindAllFilesHelper
import com.ecjtu.sharebox.getMainApplication
import com.ecjtu.sharebox.ui.dialog.FilePickDialog
import com.ecjtu.sharebox.ui.holder.FileExpandableProperty
import com.ecjtu.sharebox.util.file.FileUtil
import java.util.*

/**
 * Created by KerriGan on 2017/6/11.
 */
class FilePickDialogFragment : AppCompatDialogFragment {

    private var mActivity: FragmentActivity? = null

    private val array = arrayOf("Movie", "Music", "Photo", "Doc", "Apk", "Rar")

    private var mLoadingDialog: AlertDialog? = null

    private var mFindFilesHelper: FindAllFilesHelper? = null

    constructor() : super()

    constructor(activity: FragmentActivity) : super() {
        mActivity = activity
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val map = activity.getMainApplication().getSavedInstance()
        var flag = false
        for (key in array) {
            if (map.get(FilePickDialog.EXTRA_PROPERTY_LIST + key) == null) {
                flag = true
            }
        }

        return if (flag) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("正在初始化中")
                    .setView(R.layout.dialog_file_pick_loading)
                    .setPositiveButton(R.string.positive, { dialog: DialogInterface, which: Int ->
                    })
            builder.setCancelable(false)
            mLoadingDialog = builder.create()
            mLoadingDialog?.setCanceledOnTouchOutside(false)
            mLoadingDialog?.setOnShowListener { dialog ->
                onShowDialog(dialog)
            }
            mLoadingDialog?.setOnCancelListener { dialog ->
                onCancelDialog(dialog)
            }
            mLoadingDialog!!
        } else {
            FilePickDialog(context, mActivity)
        }
    }

    private fun onShowDialog(dialog: DialogInterface) {
        val saveInstance = activity.getMainApplication().getSavedInstance()
        mFindFilesHelper = FindAllFilesHelper(context)
        mFindFilesHelper?.setProgressCallback { taskIndex, taskSize ->
            mActivity?.runOnUiThread {
                val pert = taskIndex * 1f / (taskSize * 1f) * 100
                val bar = (dialog as AlertDialog).findViewById(R.id.progress_bar) as ProgressBar
                val txt = dialog.findViewById(R.id.percent) as TextView
                txt.setText("${pert.toInt()}%")
                bar.progress = pert.toInt()
            }
        }
        mFindFilesHelper?.startScanning { map ->
            for (entry in map) {
                val title = entry.key
                val fileList = entry.value

                val localMap = LinkedHashMap<String, MutableList<String>>()
                if (title.equals("Apk", true)) {
                    val arrayList = ArrayList<String>()
                    val installedApps = FileUtil.getInstalledApps(context, false)
                    for (packageInfo in installedApps) {
                        arrayList.add(packageInfo.applicationInfo.sourceDir)
                    }
                    localMap.put(context.getString(R.string.installed), arrayList)
                }

                if (fileList is MutableList<String>) {
                    val names = FileUtil.foldFiles(fileList, localMap)
                    val newArr = ArrayList<FileExpandableProperty>()
                    names?.let {
                        for (name in names.iterator()) {
                            val vh = FileExpandableProperty(name, localMap.get(name))
                            newArr.add(vh)
                        }
                    }
                    saveInstance.put(FilePickDialog.EXTRA_PROPERTY_LIST + title, newArr)
                }
            }
            dialog.cancel()
        }
    }

    private fun onCancelDialog(dialog: DialogInterface) {
        mFindFilesHelper?.release()
    }

}