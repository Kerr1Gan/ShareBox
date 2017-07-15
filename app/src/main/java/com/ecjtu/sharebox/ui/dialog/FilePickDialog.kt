package com.ecjtu.sharebox.ui.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.ecjtu.sharebox.R
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.Toolbar
import android.view.*
import android.os.AsyncTask
import android.support.design.widget.TabLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.widget.ProgressBar
import com.ecjtu.sharebox.Constants
import com.ecjtu.sharebox.domain.DeviceInfo
import com.ecjtu.sharebox.getMainApplication
import com.ecjtu.sharebox.server.impl.servlet.GetFiles
import com.ecjtu.sharebox.ui.view.FileExpandableListView
import com.ecjtu.sharebox.util.file.FileUtil
import java.io.File


/**
 * Created by KerriGan on 2017/6/2.
 */
class FilePickDialog :BaseBottomSheetDialog,Toolbar.OnMenuItemClickListener{
    constructor(context: Context,activity: Activity? = null):super(context,activity){

    }

    private var mBehavior:BottomSheetBehavior<View>? =null

    private var mHeight=0

    private var mTabLayout:TabLayout? =null

    private var mViewPager:ViewPager? =null

    private var mTabItemHolders:MutableMap<String,TabItemHolder>? = mutableMapOf()

    private var mViewPagerViews = mutableMapOf<Int,View>()

    private var mBottomSheet:View? =null

    private var mExpandableListView:FileExpandableListView? =null

    private var mProgressBar:ProgressBar? =null

    override fun initializeDialog() {
        super.initializeDialog()
        context.setTheme(R.style.WhiteToolbar)
    }

    override fun onCreateView(): View? {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        var vg= layoutInflater.inflate(R.layout.dialog_file_pick,null)

        val display = ownerActivity.getWindowManager().getDefaultDisplay()
        val width = display.getWidth()
        val height = display.height/*getScreenHeight(ownerActivity)+getStatusBarHeight(context)*/
        mHeight=height

        vg.layoutParams=ViewGroup.LayoutParams(width,height)
        return vg
    }

    override fun onViewCreated(view: View?):Boolean {
        super.onViewCreated(view)
        mBehavior= BottomSheetBehavior.from(findViewById(android.support.design.R.id.design_bottom_sheet))
        mBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED

//        mBehavior?.skipCollapsed=true
        mBehavior?.peekHeight=mHeight*2/3

        mBottomSheet=findViewById(R.id.design_bottom_sheet)

        initView(view as ViewGroup)
        return true
    }

    private fun initData(){
        var item=TabItemHolder(context.getString(R.string.movie),string2MediaFileType("Movie"))
        mTabItemHolders?.put("Movie",item)

        item=TabItemHolder(context.getString(R.string.music),string2MediaFileType("Music"))
        mTabItemHolders?.put("Music",item)

        item=TabItemHolder(context.getString(R.string.photo),string2MediaFileType("Photo"))
        mTabItemHolders?.put("Photo",item)

        item=TabItemHolder(context.getString(R.string.doc),string2MediaFileType("Doc"))
        mTabItemHolders?.put("Doc",item)

        item=TabItemHolder(context.getString(R.string.apk),string2MediaFileType("Apk"))
        mTabItemHolders?.put("Apk",item)

        item=TabItemHolder(context.getString(R.string.rar),string2MediaFileType("Rar"))
        mTabItemHolders?.put("Rar",item)

    }

    protected fun initView(vg:ViewGroup){
        initData()

        var toolbar=vg.findViewById(R.id.toolbar) as Toolbar

        toolbar.setNavigationIcon(ColorDrawable(Color.TRANSPARENT))

        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        toolbar.inflateMenu(R.menu.menu_file_pick)

        toolbar.setOnMenuItemClickListener(this)

        mBehavior?.setBottomSheetCallback(object :BottomSheetBehavior.BottomSheetCallback(){
            var mFitSystemWindow=false

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if(slideOffset==1.0f){
                    mFitSystemWindow=true
                }else{
                    if(mFitSystemWindow!=false){
                        toolbar.setNavigationIcon(ColorDrawable(Color.TRANSPARENT))
                        toolbar.fitsSystemWindows=false
                        toolbar.setPadding(toolbar.paddingLeft,0,toolbar.paddingRight,toolbar.paddingBottom)
                    }
                    mFitSystemWindow=false
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState==BottomSheetBehavior.STATE_EXPANDED){
                    val attrsArray = intArrayOf(android.R.attr.homeAsUpIndicator)
                    val typedArray = context.obtainStyledAttributes(attrsArray)
                    val dw = typedArray.getDrawable(0)
                    toolbar.setNavigationIcon(dw)
                    toolbar.fitsSystemWindows=true
                    toolbar.requestFitSystemWindows()

                    // don't forget the resource recycling
                    typedArray.recycle()
                    return
                }else if(newState==BottomSheetBehavior.STATE_HIDDEN){
                    dismiss()
                }

                if(!mFitSystemWindow){
                    toolbar.setNavigationIcon(ColorDrawable(Color.TRANSPARENT))
                    toolbar.fitsSystemWindows=false
                    toolbar.setPadding(toolbar.paddingLeft,0,toolbar.paddingRight,toolbar.paddingBottom)
                }
            }
        })

        mTabLayout=vg.findViewById(R.id.tab_layout) as TabLayout
        mViewPager=vg.findViewById(R.id.view_pager) as ViewPager
        mProgressBar=vg.findViewById(R.id.progress_bar) as ProgressBar

        mViewPager?.adapter=object :PagerAdapter(){


            override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
                return view==`object`
            }

            override fun getCount(): Int {
                return mTabItemHolders?.size ?:0
            }

            override fun getPageTitle(position: Int): CharSequence {
                var key=mTabItemHolders?.keys?.elementAt(position)!!
                return mTabItemHolders?.get(key)?.title as CharSequence
            }

            override fun instantiateItem(container: ViewGroup?, position: Int): Any {

                var vg:FileExpandableListView? =null

                vg= mViewPagerViews.get(position) as FileExpandableListView?
                if(vg==null){
                    Log.e("ViewPager","create view")
                    vg=layoutInflater.inflate(R.layout.layout_file_expandable_list_view,container,false) as FileExpandableListView
                }

                container?.addView(vg)

                var title=mTabItemHolders?.keys?.elementAt(position) as String

                var holder=mTabItemHolders?.get(title)
                vg.initData(holder)

                if(mTabItemHolders?.get(title)?.task==null){
                    var task=LoadingFilesTask(context,holder!!)
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    mTabItemHolders?.get(title)?.task=task
                }

                mViewPagerViews.put(position,vg)

                if(mExpandableListView==null)
                    mExpandableListView=mViewPagerViews.get(0) as FileExpandableListView
                return vg
            }

            override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
                var title=mTabItemHolders?.keys?.elementAt(position) as String
                if(mTabItemHolders?.get(title)?.task!=null){
                    var task=mTabItemHolders?.get(title)?.task
                    if(task?.status==AsyncTask.Status.FINISHED){
                        //do nothing
                    }else{
                        task?.cancel(true)
                        mTabItemHolders?.get(title)?.task=null
                    }
                }
                container?.removeView(`object` as View)
//                mViewPagerViews.remove(position)
            }
        }

        mViewPager?.setOnPageChangeListener(object :ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                mExpandableListView=mViewPagerViews.get(position) as FileExpandableListView
                mExpandableListView?.loadedData()
            }
        })

        mTabLayout?.setupWithViewPager(mViewPager)
    }

    inner class LoadingFilesTask:AsyncTask<List<File>?,Void,List<File>?>{
        private val TAG="LoadingFilesTask"

        private var mType:FileUtil.MediaFileType? =null

        private var mContext: Context? =null

        private var mHolder: TabItemHolder? =null
        constructor(context: Context,holder: TabItemHolder):super(){
            mType=holder.type
            mContext=context
            mHolder=holder
        }

        override fun doInBackground(vararg params: List<File>?): List<File>? {
            Log.e(TAG,mediaFileType2String(mType!!)+" task begin")
            publishProgress()
            var list:List<File>? = null

            when(mType){
                FileUtil.MediaFileType.MOVIE->{
                    list=FileUtil.getAllMediaFile(mContext!!,null)
                    mTabItemHolders?.get("Movie")?.fileList=list
                }
                FileUtil.MediaFileType.MP3->{
                    list=FileUtil.getAllMusicFile(mContext!!,null)
                    mTabItemHolders?.get("Music")?.fileList=list
                }
                FileUtil.MediaFileType.IMG->{
//                    list=FileUtil.getAllImageFile(mContext!!,null)
                    list=FileUtil.getImagesByDCIM(mContext!!)
                    mTabItemHolders?.get("Photo")?.fileList=list
                }
                FileUtil.MediaFileType.DOC->{
                    list=FileUtil.getAllDocFile(mContext!!,null)
                    mTabItemHolders?.get("Doc")?.fileList=list
                }
                FileUtil.MediaFileType.APP->{
                    list=FileUtil.getAllApkFile(mContext!!,null)
                    mTabItemHolders?.get("Apk")?.fileList=list
                }
                FileUtil.MediaFileType.RAR->{
                    list=FileUtil.getAllRarFile(mContext!!,null)
                    mTabItemHolders?.get("Rar")?.fileList=list
                }
            }

            if(!isCancelled)
                Log.e(TAG,mediaFileType2String(mType!!)+" task finished")
            return list
        }

        override fun onProgressUpdate(vararg values: Void?) {
            super.onProgressUpdate(*values)
            mProgressBar?.visibility=View.VISIBLE
        }

        override fun onPostExecute(result: List<File>?) {
            super.onPostExecute(result)
            mProgressBar?.visibility=View.INVISIBLE
            var index=mViewPager?.currentItem
            (mViewPagerViews.get(index) as FileExpandableListView).loadedData()
        }

        override fun onCancelled(result: List<File>?) {
            super.onCancelled(result)
            Log.e(TAG,mediaFileType2String(mType!!)+" task cancelled")
        }
    }

    fun string2MediaFileType(str:String):FileUtil.MediaFileType?{
        var ret:FileUtil.MediaFileType?=null
        when(str){
            "Movie"->{
                ret=FileUtil.MediaFileType.MOVIE
            }
            "Music"->{
                ret=FileUtil.MediaFileType.MP3
            }
            "Photo"->{
                ret=FileUtil.MediaFileType.IMG
            }
            "Doc"->{
                ret=FileUtil.MediaFileType.DOC
            }
            "Apk"->{
                ret=FileUtil.MediaFileType.APP
            }
            "Rar"->{
                ret=FileUtil.MediaFileType.RAR
            }
        }
        return ret
    }

    fun mediaFileType2String(type:FileUtil.MediaFileType):String?{
        var ret:String?=null
        when(type){
            FileUtil.MediaFileType.MOVIE->{
                ret="Movie"
            }
            FileUtil.MediaFileType.MP3->{
                ret="Music"
            }
            FileUtil.MediaFileType.IMG->{
                ret="Photo"
            }
            FileUtil.MediaFileType.DOC->{
                ret="Doc"
            }
            FileUtil.MediaFileType.APP->{
                ret="Apk"
            }
            FileUtil.MediaFileType.RAR->{
                ret="Rar"
            }
        }
        return ret
    }

    data class TabItemHolder(var title:String?=null,var type:FileUtil.MediaFileType?=null
                                       ,var task:LoadingFilesTask?=null,var fileList:List<File>?=null)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        //resolve listView doesn't not support NestedScrolling
        var ret = false
        if (mExpandableListView?.getFirstVisiblePosition() == 0) {
            val topChildView = mExpandableListView?.getChildAt(0)
            ret = topChildView?.getTop() == 0
        }

        if(mBehavior?.state!=BottomSheetBehavior.STATE_EXPANDED){
            ret=true
        }

        if (ret) {
            return super.dispatchTouchEvent(ev)
        } else {
            return mBottomSheet?.dispatchTouchEvent(ev)!!
        }

        return super.dispatchTouchEvent(ev)
    }

    override fun onStop() {
        super.onStop()
        var iter=mTabItemHolders?.iterator()
        while (iter?.hasNext() ?: false){
            var obj=iter?.next()
            obj?.value?.task?.cancel(true)
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        var id=item?.itemId
        when(id){
            R.id.ok->{
                if(mTabItemHolders==null) return true

                var map= mutableMapOf<String,List<String>>()
                var fileList= ArrayList<File>()

                for(element in mTabItemHolders!!.entries){
                    var strList= mutableListOf<String>()
                    if(element.value.fileList==null) continue
                    for(child in element.value.fileList!!.iterator()){
                        strList.add(child.absolutePath)
                        if(fileList.indexOf(child)<0){
                            fileList.add(child)
                        }
                    }
                    map.put(element.key,strList)
                }
                var deviceInfo=ownerActivity.getMainApplication().getSavedInstance().
                        get(Constants.KEY_INFO_OBJECT) as DeviceInfo
                deviceInfo.fileMap=map

                GetFiles.init(null,fileList,context.applicationContext)
                com.ecjtu.sharebox.server.impl.servlet.File.addFiles(fileList)
            }

            R.id.select_all->{

            }
        }
        return true
    }

}