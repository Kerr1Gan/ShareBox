package com.ecjtu.sharebox.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Message
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.TabLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.ecjtu.sharebox.Constants
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.async.FindAllFilesHelper
import com.ecjtu.sharebox.async.MemoryUnLeakHandler
import com.ecjtu.sharebox.getMainApplication
import com.ecjtu.sharebox.ui.adapter.FileExpandableAdapter
import com.ecjtu.sharebox.ui.widget.FileExpandableListView
import com.ecjtu.sharebox.util.file.FileUtil
import org.ecjtu.easyserver.server.DeviceInfo
import org.ecjtu.easyserver.server.impl.service.EasyServerService
import org.ecjtu.easyserver.server.util.cache.ServerInfoParcelableHelper
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


/**
 * Created by KerriGan on 2017/6/2.
 */
open class FilePickDialog : BaseBottomSheetDialog, Toolbar.OnMenuItemClickListener, MemoryUnLeakHandler.IHandleMessage {

    constructor(context: Context, activity: Activity? = null) : super(context, activity) {
    }

    private var mBehavior: BottomSheetBehavior<View>? = null

    private var mTabLayout: TabLayout? = null

    private var mViewPager: ViewPager? = null

    private var mTabItemHolders: MutableMap<String, TabItemHolder>? = mutableMapOf()

    private var mViewPagerViews = mutableMapOf<Int, View>()

    private var mBottomSheet: View? = null

    private var mExpandableListView: FileExpandableListView? = null

    private var mProgressBar: ProgressBar? = null

    private var mTempMap: MutableMap<String, ArrayList<FileExpandableAdapter.VH>> = mutableMapOf()

    private val mSavedState = if (ownerActivity != null) ownerActivity.getMainApplication().getSavedInstance() else null

    companion object {
        private const val EXTRA_VH_LIST = "extra_vh_list"
    }

    override fun initializeDialog() {
        super.initializeDialog()
        context.setTheme(R.style.WhiteToolbar)
    }

    override fun onCreateView(): View? {
        windowTranslucent()

        var vg = layoutInflater.inflate(R.layout.dialog_file_pick, null)

        fullScreenLayout(vg)
        return vg
    }

    override fun onViewCreated(view: View?): Boolean {
        super.onViewCreated(view)
        mBehavior = BottomSheetBehavior.from(findViewById(android.support.design.R.id.design_bottom_sheet))
        mBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED

        val display = ownerActivity.getWindowManager().getDefaultDisplay()
        mBehavior?.peekHeight = display.height * 2 / 3

        mBottomSheet = findViewById(R.id.design_bottom_sheet)

        initView(view as ViewGroup)
        return true
    }

    open protected fun initData() {
        var item = TabItemHolder(context.getString(R.string.movie), FileUtil.string2MediaFileType("Movie"))
        mTabItemHolders?.put("Movie", item)

        item = TabItemHolder(context.getString(R.string.music), FileUtil.string2MediaFileType("Music"))
        mTabItemHolders?.put("Music", item)

        item = TabItemHolder(context.getString(R.string.photo), FileUtil.string2MediaFileType("Photo"))
        mTabItemHolders?.put("Photo", item)

        item = TabItemHolder(context.getString(R.string.doc), FileUtil.string2MediaFileType("Doc"))
        mTabItemHolders?.put("Doc", item)

        item = TabItemHolder(context.getString(R.string.apk), FileUtil.string2MediaFileType("Apk"))
        mTabItemHolders?.put("Apk", item)

        item = TabItemHolder(context.getString(R.string.rar), FileUtil.string2MediaFileType("Rar"))
        mTabItemHolders?.put("Rar", item)

    }

    open protected fun initView(vg: ViewGroup) {
        initData()

        var toolbar = vg.findViewById(R.id.toolbar) as Toolbar

        toolbar.setNavigationIcon(ColorDrawable(Color.TRANSPARENT))

        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        toolbar.inflateMenu(R.menu.menu_file_pick)

        toolbar.setOnMenuItemClickListener(this)

        mBehavior?.setBottomSheetCallback(BottomSheetCallback(toolbar, this))

        mTabLayout = vg.findViewById(R.id.tab_layout) as TabLayout
        mViewPager = vg.findViewById(R.id.view_pager) as ViewPager
        mProgressBar = vg.findViewById(R.id.progress_bar) as ProgressBar

        mViewPager?.adapter = getViewPagerAdapter()

        mViewPager?.setOnPageChangeListener(SimplePageListener(this))

        mTabLayout?.setupWithViewPager(mViewPager)
    }

    open fun getViewPagerAdapter(): PagerAdapter {
        return object : PagerAdapter() {

            override fun isViewFromObject(view: View?, `object`: Any?): Boolean = view == `object`

            override fun getCount(): Int = mTabItemHolders?.size ?: 0

            override fun getPageTitle(position: Int): CharSequence {
                var key = mTabItemHolders?.keys?.elementAt(position)!!
                return mTabItemHolders?.get(key)?.title as CharSequence
            }

            override fun instantiateItem(container: ViewGroup?, position: Int): Any {
                var vg: FileExpandableListView? = null

                vg = mViewPagerViews.get(position) as FileExpandableListView?
                if (vg == null) {
                    Log.e("ViewPager", "create view")
                    vg = layoutInflater.inflate(R.layout.layout_file_expandable_list_view, container, false) as FileExpandableListView
                }

                container?.addView(vg)
                mViewPagerViews.put(position, vg)
                if (mExpandableListView == null)
                    mExpandableListView = getListView(0) as FileExpandableListView

                var title = mTabItemHolders?.keys?.elementAt(position) as String

                var holder = mTabItemHolders?.get(title)
                vg.fileExpandableAdapter = getFileAdapter(vg, title)
                var oldCache: List<FileExpandableAdapter.VH>? = null
                if (isLoadCache()) {
                    oldCache = getOldCacheAndClone(title)
                } else {
                    var fileList = mTabItemHolders?.get(title)?.fileList
                    if (fileList != null) {
                        var map = LinkedHashMap<String, MutableList<String>>()
                        oldCache = makeVhList(fileList, map, title, false)
                    }
                }

                vg.initData(holder, oldCache)

                if (mTabItemHolders?.get(title)?.task == null && mTabItemHolders?.get(title)?.fileList == null) {
                    var task = LoadingFilesTask(context, holder!!)
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    mTabItemHolders?.get(title)?.task = task
                }

                refreshData()

                return vg
            }

            override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
                var title = mTabItemHolders?.keys?.elementAt(position) as String
                if (mTabItemHolders?.get(title)?.task != null) {
                    var task = mTabItemHolders?.get(title)?.task
                    if (task?.status == AsyncTask.Status.FINISHED) {
                        //do nothing
                    } else {
                        task?.cancel(true)
                        mTabItemHolders?.get(title)?.task = null
                    }
                }
                container?.removeView(`object` as View)
            }
        }
    }

    inner class LoadingFilesTask : AsyncTask<List<File>?, Void, List<File>?> {
        private val TAG = "LoadingFilesTask"

        private var mType: FileUtil.MediaFileType? = null

        private var mContext: Context? = null

        private var mHolder: TabItemHolder? = null

        constructor(context: Context, holder: TabItemHolder) : super() {
            mType = holder.type
            mContext = context
            mHolder = holder
        }

        override fun doInBackground(vararg params: List<File>?): List<File>? {
            val key = FileUtil.mediaFileType2String(mType!!)
            Log.e(TAG, key + " task begin")
            publishProgress()

            findFilesWithType(mContext!!, mType!!, mTabItemHolders!!)

            if (!isCancelled)
                Log.e(TAG, key + " task finished")
            return null
        }

        override fun onProgressUpdate(vararg values: Void?) {
            super.onProgressUpdate(*values)
            refresh(true)
        }

        override fun onPostExecute(result: List<File>?) {
            super.onPostExecute(result)
            refresh(false)
            var index = mViewPager?.currentItem
            getListView(index!!)?.loadedData()
        }

        override fun onCancelled(result: List<File>?) {
            super.onCancelled(result)
            Log.e(TAG, FileUtil.mediaFileType2String(mType!!) + " task cancelled")
        }
    }

    private fun findFilesWithType(context: Context, type: FileUtil.MediaFileType, map: MutableMap<String, TabItemHolder>) {
        var list: MutableList<File>? = null
        when (type) {
            FileUtil.MediaFileType.MOVIE -> {
                list = FileUtil.getAllMediaFile(context, null)
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.get("Movie")?.fileList = strList
            }
            FileUtil.MediaFileType.MP3 -> {
                list = FileUtil.getAllMusicFile(context, null)
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.get("Music")?.fileList = strList
            }
            FileUtil.MediaFileType.IMG -> {
//                    list=FileUtil.getAllImageFile(mContext!!,null)
                list = FileUtil.getImagesByDCIM(context)
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.get("Photo")?.fileList = strList
            }
            FileUtil.MediaFileType.DOC -> {
                list = FileUtil.getAllDocFile(context, null)
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.get("Doc")?.fileList = strList
            }
            FileUtil.MediaFileType.APP -> {
                list = FileUtil.getAllApkFile(context, null)
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.get("Apk")?.fileList = strList
            }
            FileUtil.MediaFileType.RAR -> {
                list = FileUtil.getAllRarFile(context, null)
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.get("Rar")?.fileList = strList
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        //resolve listView doesn't not support NestedScrolling
        var ret = false
        if (mExpandableListView?.getFirstVisiblePosition() == 0) {
            val topChildView = mExpandableListView?.getChildAt(0)
            ret = topChildView?.getTop() ?: 0 >= 0
        }

        if (mBehavior?.state != BottomSheetBehavior.STATE_EXPANDED) {
            ret = true
        }

        if (ret) {
            return super.dispatchTouchEvent(ev)
        } else {
            return mBottomSheet?.dispatchTouchEvent(ev)!!
        }
    }

    override fun onStop() {
        super.onStop()
        cancelAllTask()
        mHandler?.removeCallbacksAndMessages(null)
        mHandler = null
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        var id = item?.itemId
        when (id) {
            R.id.ok -> {
                doOk()
            }

            R.id.select_all -> {
                doSelectAll()
            }
        }
        return true
    }

    private fun updateFileMap(fileList: MutableList<String>, itemHolder: MutableMap<String, FilePickDialog.TabItemHolder>): MutableMap<String, List<String>> {
        var map = mutableMapOf<String, List<String>>()
        var index = 0
        for (element in itemHolder.entries) {
            var strList = mutableListOf<String>()
            var pager: View? = mViewPagerViews.get(index++) ?: continue
            pager = pager as FileExpandableListView
            if (element.value.fileList == null) continue
            var fileArr = pager.fileExpandableAdapter.selectedFile
            for (file in fileArr) {
                if (fileList.indexOf(file) < 0)
                    fileList.add(file)
                strList.add(file)
            }
            map.put(element.key, strList)
        }
        for (element in itemHolder.entries) {
            var title = element.key
            var strList = mutableListOf<String>()

            if (mSavedState == null) continue

            var obj = mSavedState.get(EXTRA_VH_LIST + title)
            var vhList = if (obj != null) obj as List<FileExpandableAdapter.VH> else null

            if (vhList != null) {
                for (vh in vhList) {
                    var fList = vh.activatedList
                    for (file in fList) {
                        if (fileList.indexOf(file) < 0)
                            fileList.add(file)
                        strList.add(file)
                    }
                }
            }
            map.put(title, strList)
        }
        return map
    }

    private fun updateAllFileList(fileList: MutableList<String>, itemHolder: MutableMap<String, FilePickDialog.TabItemHolder>): MutableMap<String, List<String>> {
        var map = mutableMapOf<String, List<String>>()
        for (element in itemHolder.entries) {
            var title = element.key
            var strList = mutableListOf<String>()
            if (element.value.fileList == null) continue
            if (mSavedState == null) continue

            if (mSavedState.get(EXTRA_VH_LIST + title) != null) {
                val vhList = mSavedState.get(EXTRA_VH_LIST + title) as List<FileExpandableAdapter.VH>
                for (vh in vhList) {
                    var fList = vh.activatedList
                    for (file in fList) {
                        if (fileList.indexOf(file) < 0)
                            fileList.add(file)
                        strList.add(file)
                    }
                }
                map.put(title, strList)
            }
        }
        return map
    }

    protected fun setTabItemsHolder(holder: MutableMap<String, TabItemHolder>) {
        mTabItemHolders = holder
    }

    fun refreshData() {
        var index = mViewPager?.currentItem as Int
        getListView(index)?.loadedData()
    }

    fun refresh(refresh: Boolean) {
        if (refresh) {
            mProgressBar?.visibility = View.VISIBLE
        } else {
            mProgressBar?.visibility = View.INVISIBLE
        }
    }

    open fun getListView(position: Int): FileExpandableListView? {
        mViewPagerViews.get(position)?.let {
            return mViewPagerViews.get(position) as FileExpandableListView
        }
        return null
    }

    open fun getFileAdapter(vg: FileExpandableListView, title: String): FileExpandableAdapter {
        return vg.fileExpandableAdapter.apply { setup(title) }
    }

    private var mHandler: MemoryUnLeakHandler<FilePickDialog>? = MemoryUnLeakHandler<FilePickDialog>(this)

    override fun handleMessage(msg: Message) {

    }

    private fun selectViewPager(fileExpandableListView: FileExpandableListView) {
        fileExpandableListView.fileExpandableAdapter.selectAll(true)
    }

    private fun makeVhList(fileList: List<String>, map: LinkedHashMap<String, MutableList<String>>? = null, title: String, isActivated: Boolean): List<FileExpandableAdapter.VH>? {
        var localMap: LinkedHashMap<String, MutableList<String>>? = map
        if (localMap == null) localMap = LinkedHashMap<String, MutableList<String>>()

        if (title.equals("Apk", true)) {
            val arrayList = ArrayList<String>()
            val installedApps = FileUtil.getInstalledApps(ownerActivity, false)
            for (packageInfo in installedApps) {
                arrayList.add(packageInfo.applicationInfo.sourceDir)
            }
            localMap.put("已安装", arrayList)
        }

        val names = FileUtil.foldFiles(fileList as MutableList<String>, localMap)

        names?.let {
            val newArr = ArrayList<FileExpandableAdapter.VH>()

            for (name in names.iterator()) {
                val vh = FileExpandableAdapter.VH(name, localMap!!.get(name))
                vh.activate(isActivated)
                newArr.add(vh)
            }
            return newArr
        }
        return null
    }

    open protected fun isLoadCache(): Boolean = true

    private fun getOldCacheAndClone(title: String): List<FileExpandableAdapter.VH>? {
        if (mSavedState == null) return null
        var cache = mSavedState!!.get(EXTRA_VH_LIST + title) as List<FileExpandableAdapter.VH>?
        var newList = arrayListOf<FileExpandableAdapter.VH>()
        if (cache != null) {
            for (vh in cache) {
                var newVh = vh.clone() as FileExpandableAdapter.VH
                if (newVh != null) {
                    newList.add(newVh)
                }
            }
        }
        return newList
    }

    private fun doOk() {
        cancelAllTask()
        var fileList = mutableListOf<String>()

        for (entry in mTabItemHolders!!.entries) {
            var title = entry.key
            var key = EXTRA_VH_LIST + title
            var vhList = mTempMap.get(key)
            if (mSavedState != null && vhList != null) {
                mSavedState.put(key, vhList)
            }
        }

        for (entry in mViewPagerViews) {
            var pager = entry.value as FileExpandableListView
            var adapter = pager.fileExpandableAdapter
            var save = pager.fileExpandableAdapter.vhList
            if (mSavedState != null && save != null) {
                mSavedState.put(EXTRA_VH_LIST + adapter.title, save)
            }
        }

        var map = updateFileMap(fileList, mTabItemHolders!!)
        if (mSavedState != null) {
            var deviceInfo = mSavedState.get(Constants.KEY_INFO_OBJECT) as DeviceInfo
            deviceInfo.fileMap = map
            var serverList = arrayListOf<File>()
            for (path in fileList) {
                serverList.add(File(path))
            }

            thread {
                val helper = ServerInfoParcelableHelper(context.filesDir.absolutePath)
                helper.put(Constants.KEY_INFO_OBJECT, deviceInfo)
                val intent = EasyServerService.getSetupServerIntent(context, Constants.KEY_INFO_OBJECT)
                context.startService(intent)
            }
        }

        this@FilePickDialog.cancel()
        Toast.makeText(context, R.string.select_success, Toast.LENGTH_SHORT).show()
    }

    private fun doSelectAll() {
        val findAllTask = FindAllFilesHelper(context)

        val progressDialog = ProgressDialog(context, ownerActivity).apply {
            setOnDismissListener {
                findAllTask.release()
            }
            show()
        }

        findAllTask.startScanning { map ->
            mTempMap.clear()
            val res = LinkedHashMap<String, MutableList<String>>()
            for (entry in map) {
                res.clear()
                var title = entry.key
                var fileList = entry.value
                if (fileList != null) {
                    var newArr: List<FileExpandableAdapter.VH>? = makeVhList(fileList, res, title, true) ?: continue
                    if (mSavedState != null) {
                        mSavedState.put(EXTRA_VH_LIST + title, newArr!!)
                    }
                    for (view in mViewPagerViews) {
                        var viewPager = view.value as FileExpandableListView
                        if (viewPager.fileExpandableAdapter.title.equals(title)) {
                            viewPager.fileExpandableAdapter.replaceVhList(newArr)
                            viewPager.post {
                                viewPager.loadedData()
                            }
                        }
                    }
                    mTempMap.put(EXTRA_VH_LIST + title, newArr as ArrayList<FileExpandableAdapter.VH>)
                }
            }

            progressDialog.cancel()
        }
    }

    private fun cancelAllTask() {
        if (mTabItemHolders == null) return
        var iter = mTabItemHolders?.iterator()
        while (iter?.hasNext() ?: false) {
            var obj = iter?.next()
            obj?.value?.task?.cancel(true)
        }
    }

    data class TabItemHolder(var title: String? = null, var type: FileUtil.MediaFileType? = null
                             , var task: LoadingFilesTask? = null, var fileList: List<String>? = null)

    private class BottomSheetCallback(val toolbar: Toolbar, val dialog: Dialog) : BottomSheetBehavior.BottomSheetCallback() {

        var mFitSystemWindow = false

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (slideOffset == 1.0f) {
                mFitSystemWindow = true
            } else {
                if (mFitSystemWindow != false) {
                    toolbar.setNavigationIcon(ColorDrawable(Color.TRANSPARENT))
                    toolbar.fitsSystemWindows = false
                    toolbar.setPadding(toolbar.paddingLeft, 0, toolbar.paddingRight, toolbar.paddingBottom)
                }
                mFitSystemWindow = false
            }
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                val attrsArray = intArrayOf(android.R.attr.homeAsUpIndicator)
                val typedArray = dialog.context.obtainStyledAttributes(attrsArray)
                val dw = typedArray.getDrawable(0)
                toolbar.setNavigationIcon(dw)
                toolbar.fitsSystemWindows = true
                toolbar.requestFitSystemWindows()

                // don't forget the resource recycling
                typedArray.recycle()
                return
            } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dialog.dismiss()
            }

            if (!mFitSystemWindow) {
                toolbar.setNavigationIcon(ColorDrawable(Color.TRANSPARENT))
                toolbar.fitsSystemWindows = false
                toolbar.setPadding(toolbar.paddingLeft, 0, toolbar.paddingRight, toolbar.paddingBottom)
            }
        }
    }

    private class SimplePageListener(val dialog: FilePickDialog) : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        }

        override fun onPageSelected(position: Int) {
            dialog.mExpandableListView = dialog.getListView(position)
            dialog.mExpandableListView?.loadedData()
        }

    }
}