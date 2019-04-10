package com.ethan.and.ui.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageInfo
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.flybd.sharebox.R
import com.ethan.and.async.FindAllFilesHelper
import com.ethan.and.getMainApplication
import com.ethan.and.ui.dialog.FilePickDialog
import com.ethan.and.ui.holder.FileExpandableInfo
import com.common.utils.file.FileUtil
import java.util.*

/**
 * Created by KerriGan on 2017/6/11.
 */
@SuppressLint("ValidFragment")
class FilePickDialogFragment : AppCompatDialogFragment {

    companion object {
        private const val TAG = "FilePickDialogFragment"
    }

    private var mActivity: FragmentActivity? = null

    private val array = arrayOf("Movie", "Music", "Photo", "Doc", "Apk", "Rar")

    private var mLoadingDialog: AlertDialog? = null

    private var mFindFilesHelper: FindAllFilesHelper? = null

    private var isPaused = false

    constructor() : super()

    constructor(activity: FragmentActivity) : super() {
        mActivity = activity
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val map = activity!!.getMainApplication().getSavedInstance()
        var flag = false
        for (key in array) {
            if (map.get(FilePickDialog.EXTRA_PROPERTY_LIST + key) == null) {
                Log.i(TAG, "instance key $key is null")
                flag = true
            }
        }

        return if (flag) {
            val builder = AlertDialog.Builder(context!!)
            builder.setTitle(R.string.is_being_initialized)
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
            FilePickDialog(context!!, mActivity)
        }
    }

    override fun onStart() {
        isPaused = false
        super.onStart()
    }

    override fun onPause() {
        isPaused = true
        super.onPause()
    }

    private fun onShowDialog(dialog: DialogInterface) {
        val saveInstance = activity!!.getMainApplication().getSavedInstance()
        mFindFilesHelper = FindAllFilesHelper(context!!)
        mFindFilesHelper?.setProgressCallback { taskIndex, taskSize ->
            mActivity?.runOnUiThread {
                val pert = taskIndex * 1f / (taskSize * 1f) * 100
                val bar = (dialog as AlertDialog).findViewById<View>(R.id.progress_bar) as ProgressBar
                val txt = dialog.findViewById<View>(R.id.percent) as TextView
                txt.setText("${pert.toInt()}%")
                bar.progress = pert.toInt()
            }
        }
        mFindFilesHelper?.startScanning { map ->
            run {
                for (entry in map) {
                    val title = entry.key
                    val fileList = entry.value

                    val localMap = LinkedHashMap<String, MutableList<String>>()
                    if (title.equals("Apk", true)) {
                        val arrayList = ArrayList<String>()
                        val ctx = context ?: return@run
                        val installedApps = FileUtil.getInstalledApps(ctx, false)
                        Collections.sort(installedApps, object : Comparator<PackageInfo> {
                            override fun compare(lhs: PackageInfo?, rhs: PackageInfo?): Int {
                                if (lhs == null || rhs == null) {
                                    return 0
                                }
                                if (lhs.lastUpdateTime < rhs.lastUpdateTime) {
                                    return 1
                                } else if (lhs.lastUpdateTime > rhs.lastUpdateTime) {
                                    return -1
                                } else {
                                    return 0
                                }
                            }
                        })
                        for (packageInfo in installedApps) {
                            arrayList.add(packageInfo.applicationInfo.sourceDir)
                        }
                        localMap.put(ctx.getString(R.string.installed), arrayList)
                    }

                    if (fileList is MutableList<String>) {
                        val names = FileUtil.foldFiles(fileList, localMap)
                        val newArr = ArrayList<FileExpandableInfo>()
                        names?.let {
                            for (name in names.iterator()) {
                                val vh = FileExpandableInfo(name, localMap.get(name))
                                newArr.add(vh)
                            }
                        }
                        saveInstance.put(FilePickDialog.EXTRA_PROPERTY_LIST + title, newArr)
                    }
                }
                val act = mActivity
                if (act != null && !act.isFinishing && !isPaused) {
                    FilePickDialogFragment(act).show(act.supportFragmentManager, "show_file_pick_dialog")
                }
            }
            dialog.cancel()
        }
    }

    private fun onCancelDialog(dialog: DialogInterface) {
        mFindFilesHelper?.release()
    }

}