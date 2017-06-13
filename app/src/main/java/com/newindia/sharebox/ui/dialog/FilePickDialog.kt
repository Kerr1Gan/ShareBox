package com.newindia.sharebox.ui.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.newindia.sharebox.R
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.Toolbar
import android.view.*
import android.content.res.TypedArray
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.newindia.sharebox.ui.activities.MainActivity
import com.newindia.sharebox.ui.fragments.PageFragment


/**
 * Created by KerriGan on 2017/6/2.
 */
class FilePickDialog :BaseBottomSheetDialog{

    constructor(context: Context,activity: Activity? = null):super(context,activity){

    }

    private var mBehavior:BottomSheetBehavior<View>? =null

    private var mHeight=0

    private var mTabLayout:TabLayout? =null

    private var mViewPager:ViewPager? =null

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

        initView(view as ViewGroup)
        return true
    }

    protected fun initView(vg:ViewGroup){
        var toolbar=vg.findViewById(R.id.toolbar) as Toolbar

        toolbar.setNavigationIcon(ColorDrawable(Color.TRANSPARENT))

        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        toolbar.inflateMenu(R.menu.menu_file_pick)

        mBehavior?.setBottomSheetCallback(object :BottomSheetBehavior.BottomSheetCallback(){
            var mFitSystemWindow=false

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if(slideOffset==1.0f){
                    mFitSystemWindow=true
                }else{
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

//        mViewPager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mTabLayout))

        mViewPager?.adapter=object :PagerAdapter(){
            override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
                return view==`object`
            }

            override fun getCount(): Int {
                return 10
            }

            override fun getPageTitle(position: Int): CharSequence {
                return ""+position
            }

            override fun instantiateItem(container: ViewGroup?, position: Int): Any {
                var vg=layoutInflater.inflate(R.layout.layout_main_activity_data,container,false)
                container?.addView(vg)
                return vg
            }

            override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
                container?.removeView(`object` as View)
            }
        }

        mTabLayout?.setupWithViewPager(mViewPager)


    }
}