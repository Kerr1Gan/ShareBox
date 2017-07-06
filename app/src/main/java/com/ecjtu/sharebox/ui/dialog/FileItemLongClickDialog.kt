package com.ecjtu.sharebox.ui.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ecjtu.sharebox.R
import org.w3c.dom.Text

/**
 * Created by KerriGan on 2017/6/18.
 */
class FileItemLongClickDialog(context: Context):BaseBottomSheetDialog(context){

    private var mBody:((id:Int)->Void)? =null

    private var mTitles:Array<String>? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(): View? {
        return layoutInflater.inflate(R.layout.dialog_file_item_long_click,null)
    }

    override fun onViewCreated(view: View?): Boolean {

        view?.findViewById(R.id.open)?.setOnClickListener {
            mBody?.invoke(R.id.open)
        }


        view?.findViewById(R.id.close)?.setOnClickListener {
            mBody?.invoke(R.id.close)
        }

        return super.onViewCreated(view)
    }

    fun setOnClickListener(body:(id:Int)->Void){
        mBody=body
    }

    fun setupItem(titles: Array<String>){
        mTitles=titles
    }
}