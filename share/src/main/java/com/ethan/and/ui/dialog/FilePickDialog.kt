@file:Suppress("CanBeVal", "UNCHECKED_CAST")

package com.ethan.and.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Build
import android.os.Message
import android.preference.PreferenceManager
import android.support.annotation.RequiresApi
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
import com.flybd.sharebox.Constants
import com.flybd.sharebox.R
import com.ethan.and.async.FindAllFilesHelper
import com.ethan.and.async.WeakHandler
import com.ethan.and.getMainApplication
import com.ethan.and.ui.adapter.FileExpandableAdapter
import com.ethan.and.ui.holder.FileExpandableInfo
import com.ethan.and.ui.holder.TabItemInfo
import com.ethan.and.ui.state.StateMachine
import com.ethan.and.ui.widget.FileExpandableListView
import com.flybd.sharebox.util.ObjectUtil
import com.flybd.sharebox.util.file.FileUtil
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
open class FilePickDialog : BaseBottomSheetDialog, Toolbar.OnMenuItemClickListener, WeakHandler.IHandleMessage {

    constructor(context: Context, activity: Activity? = null) : super(context, activity)

    private var mBehavior: BottomSheetBehavior<View>? = null

    private var mTabLayout: TabLayout? = null

    private var mViewPager: ViewPager? = null

    private var mTabItemHolders: MutableMap<String, TabItemInfo>? = mutableMapOf()

    private var mViewPagerViews = mutableMapOf<Int, View>()

    private var mBottomSheet: View? = null

    private var mExpandableListView: FileExpandableListView? = null

    private var mProgressBar: ProgressBar? = null

    private var mTempMap: MutableMap<String, ArrayList<FileExpandableInfo>> = mutableMapOf()

    private val mSavedState = if (ownerActivity != null) ownerActivity.getMainApplication().getSavedInstance() else null

    private var mDoOk = false

    private var mToolbarMachine: StateMachine? = null

    companion object {
        const val EXTRA_PROPERTY_LIST = "extra_property_list"
        private const val PREF_SELECT_ALL = "pref_extra_select_all"
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
        var item = TabItemInfo(context.getString(R.string.movie), FileUtil.string2MediaFileType("Movie"))
        mTabItemHolders?.put("Movie", item)

        item = TabItemInfo(context.getString(R.string.music), FileUtil.string2MediaFileType("Music"))
        mTabItemHolders?.put("Music", item)

        item = TabItemInfo(context.getString(R.string.photo), FileUtil.string2MediaFileType("Photo"))
        mTabItemHolders?.put("Photo", item)

        item = TabItemInfo(context.getString(R.string.doc), FileUtil.string2MediaFileType("Doc"))
        mTabItemHolders?.put("Doc", item)

        item = TabItemInfo(context.getString(R.string.apk), FileUtil.string2MediaFileType("Apk"))
        mTabItemHolders?.put("Apk", item)

        item = TabItemInfo(context.getString(R.string.rar), FileUtil.string2MediaFileType("Rar"))
        mTabItemHolders?.put("Rar", item)

    }

    open protected fun initView(vg: ViewGroup) {
        initData()

        var toolbar = vg.findViewById<View>(R.id.toolbar) as Toolbar

        toolbar.setNavigationIcon(ColorDrawable(Color.TRANSPARENT))

        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        toolbar.inflateMenu(R.menu.menu_file_pick)

        mToolbarMachine = object : StateMachine(context, R.array.file_pick_dialog_toolbar_array, null) {
            override fun updateView(index: Int) {
                super.updateView(index)
                toolbar.menu.findItem(R.id.select_all).setTitle(getArrayStringByIndex(index))
            }
        }
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_SELECT_ALL, false)) {
            mToolbarMachine?.updateView(1)
        }

        toolbar.menu.findItem(R.id.select_all)

        toolbar.setOnMenuItemClickListener(this)

        mBehavior?.setBottomSheetCallback(BottomSheetCallback(toolbar, this))

        mTabLayout = vg.findViewById<View>(R.id.tab_layout) as TabLayout
        mViewPager = vg.findViewById<View>(R.id.view_pager) as ViewPager
        mProgressBar = vg.findViewById<View>(R.id.progress_bar) as ProgressBar

        mViewPager?.adapter = getViewPagerAdapter()

        mViewPager?.setOnPageChangeListener(SimplePageListener(this))

        mTabLayout?.setupWithViewPager(mViewPager)
    }

    open fun getViewPagerAdapter(): PagerAdapter {
        return object : PagerAdapter() {

            override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

            override fun getCount(): Int = mTabItemHolders?.size ?: 0

            override fun getPageTitle(position: Int): CharSequence {
                var key = mTabItemHolders?.keys?.elementAt(position)!!
                return mTabItemHolders?.get(key)?.title as CharSequence
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
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
                var oldCache: List<FileExpandableInfo>? = null
                if (isLoadCache()) {
                    oldCache = getOldCacheAndClone(title)
                    if (oldCache != null) {
                        mDoOk = true
                    }
                } else {
                    var fileList = mTabItemHolders?.get(title)?.fileList
                    if (fileList != null) {
                        var map = LinkedHashMap<String, MutableList<String>>()
                        oldCache = makePropertyList(fileList, map, title, false)
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

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
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

        private var mHolder: TabItemInfo? = null

        constructor(context: Context, holder: TabItemInfo) : super() {
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

    private fun findFilesWithType(context: Context, type: FileUtil.MediaFileType, map: MutableMap<String, TabItemInfo>) {
        var list: MutableList<File>? = null
        when (type) {
            FileUtil.MediaFileType.MOVIE -> {
                list = FileUtil.getAllMediaFile(context, null)
                list.reverse()
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.get("Movie")?.fileList = strList
            }
            FileUtil.MediaFileType.MP3 -> {
                list = FileUtil.getAllMusicFile(context, null)
                list.reverse()
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.get("Music")?.fileList = strList
            }
            FileUtil.MediaFileType.IMG -> {
//                    list=FileUtil.getAllImageFile(mContext!!,null)
                list = FileUtil.getImagesByDCIM(context)
                list.reverse()
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.get("Photo")?.fileList = strList
            }
            FileUtil.MediaFileType.DOC -> {
                list = FileUtil.getAllDocFile(context, null)
                list.reverse()
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.get("Doc")?.fileList = strList
            }
            FileUtil.MediaFileType.APP -> {
                list = FileUtil.getAllApkFile(context, null)
                list.reverse()
                var strList = arrayListOf<String>()
                for (path in list.iterator()) {
                    strList.add(path.absolutePath)
                }
                map.get("Apk")?.fileList = strList
            }
            FileUtil.MediaFileType.RAR -> {
                list = FileUtil.getAllRarFile(context, null)
                list.reverse()
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
        if (!mDoOk) {
            for (entry in mViewPagerViews) {
                val pager = entry.value as FileExpandableListView
                val adapter = pager.fileExpandableAdapter
                val save = pager.fileExpandableAdapter.propertyList
                for (vh in save) {
                    vh.activate(false)
                }
                if (mSavedState != null && save != null) {
                    mSavedState.put(EXTRA_PROPERTY_LIST + adapter.title, save)
                }
            }
        }

        thread {
            ownerActivity.getMainApplication().saveCache()
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        var id = item?.itemId
        mDoOk = true
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

    @Suppress("UNCHECKED_CAST")
    private fun updateFileMap(fileList: MutableList<String>, itemHolder: MutableMap<String, TabItemInfo>): MutableMap<String, List<String>> {
        var map = mutableMapOf<String, List<String>>()
        var index = 0
        // load current scanning files
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

        // load cache files
        for (element in itemHolder.entries) {
            var title = element.key
            var strList: MutableList<String>? = mutableListOf()
            if (mSavedState == null) continue

            var obj = mSavedState.get(EXTRA_PROPERTY_LIST + title)
            var vhList = if (obj != null) obj as List<FileExpandableInfo> else null

            if (vhList != null) {
                for (vh in vhList) {
                    var fList = vh.activatedList
                    for (file in fList) {
                        if (fileList.indexOf(file) < 0)
                            fileList.add(file)
                        if (strList?.indexOf(file) ?: 0 < 0) {
                            strList?.add(file)
                        }
                    }
                }
            }
            map.put(title, strList!!)
        }
        return map
    }

    private fun clearFileMap(itemHolder: MutableMap<String, TabItemInfo>) {
        var index = 0
        // load current scanning files
        for (element in itemHolder.entries) {
            var pager: View? = mViewPagerViews.get(index++) ?: continue
            pager = pager as FileExpandableListView
            pager.fileExpandableAdapter.selectAll(false)
        }

        // load cache files
        for (element in itemHolder.entries) {
            var title = element.key
            if (mSavedState == null) continue
            var obj = mSavedState.get(EXTRA_PROPERTY_LIST + title)
            var vhList = if (obj != null) obj as List<FileExpandableInfo> else null
            if (vhList != null) {
                for (vh in vhList) {
                    vh.activate(false)
                }
            }
        }
    }

    protected fun setTabItemsHolder(holder: MutableMap<String, TabItemInfo>) {
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

    private var mHandler: WeakHandler<FilePickDialog>? = WeakHandler<FilePickDialog>(this)

    override fun handleMessage(msg: Message) {

    }

    private fun makePropertyList(fileList: List<String>, map: LinkedHashMap<String, MutableList<String>>? = null, title: String, isActivated: Boolean): List<FileExpandableInfo>? {
        var localMap: LinkedHashMap<String, MutableList<String>>? = map
        if (localMap == null) localMap = LinkedHashMap<String, MutableList<String>>()

        if (title.equals("Apk", true)) {
            val arrayList = ArrayList<String>()
            val installedApps = FileUtil.getInstalledApps(ownerActivity, false)
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
            localMap.put(context.getString(R.string.installed), arrayList)
        }

        val names = FileUtil.foldFiles(fileList as MutableList<String>, localMap)

        names?.let {
            val newArr = ArrayList<FileExpandableInfo>()

            for (name in names.iterator()) {
                val vh = FileExpandableInfo(name, localMap!!.get(name))
                vh.activate(isActivated)
                newArr.add(vh)
            }
            return newArr
        }
        return null
    }

    open protected fun isLoadCache(): Boolean = true

    private fun getOldCacheAndClone(title: String): List<FileExpandableInfo>? {
        if (mSavedState == null) return null
        var cache = mSavedState.get(EXTRA_PROPERTY_LIST + title) as List<*>?
        if (cache != null) {
            return ObjectUtil.deepCopy(cache) as List<FileExpandableInfo>?
        }
        return null
    }

    private fun doOk() {
        cancelAllTask()
        var fileList = mutableListOf<String>()

        for (entry in mViewPagerViews) {
            var pager = entry.value as FileExpandableListView
            var adapter = pager.fileExpandableAdapter
            var save = pager.fileExpandableAdapter.propertyList
            if (mSavedState != null && save != null) {
                mSavedState.put(EXTRA_PROPERTY_LIST + adapter.title, save)
            }
        }

        var map = updateFileMap(fileList, mTabItemHolders!!)
        if (mSavedState != null) {
            var deviceInfo = mSavedState.get(Constants.KEY_INFO_OBJECT) as DeviceInfo?
            deviceInfo?.let {
                deviceInfo.fileMap = map
                thread {
                    val helper = ServerInfoParcelableHelper(context.filesDir.absolutePath)
                    helper.put(Constants.KEY_INFO_OBJECT, deviceInfo)
                    val intent = EasyServerService.getSetupServerIntent(context, Constants.KEY_INFO_OBJECT)
                    context.startService(intent)
                }
            }
        }

        this@FilePickDialog.cancel()
        Toast.makeText(context, R.string.select_success, Toast.LENGTH_SHORT).show()
    }

    private fun doSelectAll() {
        val isSelectAll = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_SELECT_ALL, false)
        if (!isSelectAll) {
            val findAllTask = FindAllFilesHelper(context)
            val progressDialog = ProgressDialog(context, ownerActivity).apply {
                setOnDismissListener {
                    findAllTask.release()
                    ownerActivity.runOnUiThread {
                        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_SELECT_ALL, false)) {
                            mToolbarMachine?.updateView(1)
                        }
                    }
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
                        var newArr: List<FileExpandableInfo>? = makePropertyList(fileList, res, title, true) ?: continue
                        if (mSavedState != null) {
                            mSavedState.put(EXTRA_PROPERTY_LIST + title, newArr!!)
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
                        mTempMap.put(EXTRA_PROPERTY_LIST + title, newArr as ArrayList<FileExpandableInfo>)
                    }
                }
                progressDialog.cancel()
                PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_SELECT_ALL, true).apply()
            }
        } else {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_SELECT_ALL, false).apply()
            mToolbarMachine?.updateView(0)
            mTabItemHolders?.let {
                clearFileMap(mTabItemHolders!!)
            }
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

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
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