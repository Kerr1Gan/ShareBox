package com.ecjtu.sharebox.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ecjtu.sharebox.R

/**
 * Created by KerriGan on 2017/6/18.
 */
class TextItemDialog(context: Context):BaseBottomSheetDialog(context),View.OnClickListener{


    private var mBody:((id:Int)->Unit)? =null

    private var mTitles:Array<String>? =null

    private var mRoot:ViewGroup? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(): View? {
        return layoutInflater.inflate(R.layout.dialog_text_item,null)
    }

    override fun onViewCreated(view: View?): Boolean {
        mRoot= view as ViewGroup?

        for(index in 0..(mTitles!!.size-1)){
            var child=layoutInflater.inflate(R.layout.layout_dialog_text_item,mRoot,false) as TextView
            child.setText(mTitles!![index])
            mRoot?.addView(child)
            if(index!=mTitles!!.size-1){
                var divider=layoutInflater.inflate(R.layout.layout_divider,mRoot,false)
                mRoot?.addView(divider)
            }
            child.setOnClickListener(this)
            child.setTag(index)
        }

        return super.onViewCreated(view)
    }

    fun setOnClickListener(body:(id:Int)->Unit){
        mBody=body
    }

    fun setupItem(titles: Array<String>){
        mTitles=titles
    }

    override fun onClick(v: View?) {
        var index=v?.getTag() as Int
        mBody?.invoke(index)
    }
}