package com.ethan.and.ui.holder

import com.ethan.and.ui.dialog.FilePickDialog
import com.ecjtu.sharebox.util.file.FileUtil

/**
 * Created by Ethan_Xiang on 2017/10/27.
 */
data class TabItemInfo(var title: String? = null, var type: FileUtil.MediaFileType? = null
                       , var task: FilePickDialog.LoadingFilesTask? = null, var fileList: List<String>? = null)