package com.ecjtu.sharebox.ui.activity

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.async.FindAllFilesHelper
import com.ecjtu.sharebox.ui.fragment.PageFragment
import com.ecjtu.sharebox.util.file.FileUtil

/**
 * Created by KerriGan on 2017/6/11.
 */

class TestActivity:AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_file_pick)
        var mTabLayout=findViewById(R.id.tab_layout) as TabLayout
        var mViewPager=findViewById(R.id.view_pager) as ViewPager

//        mViewPager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mTabLayout))

        var supportFragmentManager=this.supportFragmentManager

        mViewPager?.adapter=object : FragmentPagerAdapter(supportFragmentManager){

            override fun getItem(position: Int): Fragment {
                return PageFragment()
            }

            override fun getCount(): Int {
                return 10
            }

            override fun getPageTitle(position: Int): CharSequence {
                return ""+position
            }
        }

        mTabLayout?.setupWithViewPager(mViewPager)

//        var intent=ImmersiveFragmentActivity.newInstance(this,WebViewFragment::class.java,Bundle().apply { putString(WebViewFragment.EXTRA_URL,"index.html"); putInt(WebViewFragment.EXTRA_TYPE,WebViewFragment.TYPE_INNER_WEB) })
//        startActivity(intent)

//        var intent=RotateNoCreateActivity.newInstance(this,IjkVideoFragment::class.java)
//        startActivity(intent)

        var task = FindAllFilesHelper(this)
        task.startScanning { map->
            val result = arrayListOf<MutableMap<String,List<String>>>()

            for(entry in map){
                val localMap = LinkedHashMap<String,List<String>>()
                FileUtil.foldFiles(entry.value as MutableList<String>, localMap as java.util.LinkedHashMap<String, MutableList<String>>)
                result.add(localMap)
            }
            task.release()
        }
    }
}
