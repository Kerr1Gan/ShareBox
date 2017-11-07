package com.ecjtu.sharebox.ui.activity

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.async.FindAllFilesHelper
import com.ecjtu.sharebox.notification.ServerComingNotification
import com.ecjtu.sharebox.ui.fragment.PageFragment
import com.ecjtu.sharebox.util.file.FileUtil
import org.json.JSONObject
import javax.security.auth.login.LoginException

/**
 * Created by KerriGan on 2017/6/11.
 */

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_file_pick)
        var mTabLayout = findViewById(R.id.tab_layout) as TabLayout
        var mViewPager = findViewById(R.id.view_pager) as ViewPager

//        mViewPager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mTabLayout))

        var supportFragmentManager = this.supportFragmentManager

        mViewPager?.adapter = object : FragmentPagerAdapter(supportFragmentManager) {

            override fun getItem(position: Int): Fragment {
                return PageFragment()
            }

            override fun getCount(): Int {
                return 10
            }

            override fun getPageTitle(position: Int): CharSequence {
                return "" + position
            }
        }

        mTabLayout?.setupWithViewPager(mViewPager)

//        var intent=ImmersiveFragmentActivity.newInstance(this,WebViewFragment::class.java,Bundle().apply { putString(WebViewFragment.EXTRA_URL,"index.html"); putInt(WebViewFragment.EXTRA_TYPE,WebViewFragment.TYPE_INNER_WEB) })
//        startActivity(intent)

//        var intent=RotateNoCreateActivity.newInstance(this,IjkVideoFragment::class.java)
//        startActivity(intent)

//        var task = FindAllFilesHelper(this)
//        task.startScanning { map->
//            val result = arrayListOf<MutableMap<String,List<String>>>()
//
//            for(entry in map){
//                val localMap = LinkedHashMap<String,List<String>>()
//                FileUtil.foldFiles(entry.value as MutableList<String>, localMap as java.util.LinkedHashMap<String, MutableList<String>>)
//                result.add(localMap)
//            }
//            task.release()
//        }
//        var intent=RotateNoCreateActivity.newInstance(this,IjkVideoFragment::class.java)
//        startActivity(intent)

//        var task = FindAllFilesHelper(this)
//        task.startScanning { map->
//            val result = arrayListOf<MutableMap<String,List<String>>>()
//
//            for(entry in map){
//                val localMap = LinkedHashMap<String,List<String>>()
//                FileUtil.foldFiles(entry.value as MutableList<String>, localMap as java.util.LinkedHashMap<String, MutableList<String>>)
//                result.add(localMap)
//            }
//            task.release()
//        }

//        ServerComingNotification(this).buildServerComingNotification("title","content","ticker").send()

        val jsonObject = JSONObject("{\n" +
                "    \"name\": \"BeJson\",\n" +
                "    \"url\": \"http://www.bejson.com\",\n" +
                "    \"page\": 88,\n" +
                "    \"isNonProfit\": true,\n" +
                "    \"address\": {\n" +
                "        \"street\": \"科技园路.\",\n" +
                "        \"city\": \"江苏苏州\",\n" +
                "        \"country\": \"中国\"\n" +
                "    },\n" +
                "    \"links\": [\n" +
                "        {\n" +
                "            \"name\": \"Google\",\n" +
                "            \"url\": \"http://www.google.com\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"name\": \"Baidu\",\n" +
                "            \"url\": \"http://www.baidu.com\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"name\": \"SoSo\",\n" +
                "            \"url\": \"http://www.SoSo.com\"\n" +
                "        }\n" +
                "    ]\n" +
                "}")

        try {
            val jsonObj2 = JSONObject("")
        }catch (ex:Exception){
        }

        loop@
        for(i in 0..5){
            Log.e("KotlinTest","loop begin")
            break@loop
            Log.e("KotlinTest","$i")
        }

        loop2@if(5>1){
            return@loop2
        }

    }
}
