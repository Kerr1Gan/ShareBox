package com.ecjtu.sharebox.ui.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.ui.dialog.EditNameDialog
import com.ecjtu.sharebox.ui.dialog.WifiBottomSheetDialog
import moe.feng.common.stepperview.IStepperAdapter
import moe.feng.common.stepperview.VerticalStepperItemView
import moe.feng.common.stepperview.VerticalStepperView
import java.lang.Exception

/**
 * Created by Ethan_Xiang on 2017/8/17.
 */
class HelpFragment : Fragment(), IStepperAdapter {

    private var mVerticalStepperView: VerticalStepperView? = null

    private var mTitles: Array<String>? = null

    private var mSummaries: Array<String>? = null

    override fun onCreateView(inflater: LayoutInflater?, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_vertical_stepper_adapter, parent, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        initContent()
        mVerticalStepperView = view!!.findViewById(R.id.vertical_stepper_view) as VerticalStepperView
        mVerticalStepperView!!.setStepperAdapter(this)
    }

    private fun initContent() {
        mTitles = arrayOf("Step One", "Step two", "Step three", "Start")
        mSummaries = arrayOf("Go to set name", "Connect network", "Pick files", "Start")
    }

    override fun getTitle(index: Int): String {
        return mTitles?.get(index) ?: ""
    }

    override fun getSummary(index: Int): String? {
        return mSummaries?.get(index)
    }

    override fun size(): Int {
        return mTitles?.size ?: 0
    }

    override fun onCreateCustomView(index: Int, context: Context, parent: VerticalStepperItemView): View {
        val inflateView = LayoutInflater.from(context).inflate(if (index != 1) R.layout.layout_vertical_stepper_sample_item_1 else R.layout.layout_vertical_stepper_sample_item_2,
                parent, false)
        initEventListener(index, inflateView)
        val nextButton = inflateView.findViewById(R.id.button_next) as Button
        nextButton.setOnClickListener {
            if(!mVerticalStepperView!!.nextStep()){
               activity.finish()
            }
        }
        val prevButton = inflateView.findViewById(R.id.button_prev) as Button
        prevButton.setOnClickListener {
            mVerticalStepperView!!.prevStep()
        }
        return inflateView
    }

    override fun onShow(index: Int) {

    }

    override fun onHide(index: Int) {

    }

    private fun initEventListener(index: Int, view: View?) {
        val contentView = view?.findViewById(R.id.item_content_1) as TextView
        when (index) {
            0 -> {
                contentView.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                contentView.text = mSummaries?.get(index)
                contentView.setOnClickListener {
                    if (index == mVerticalStepperView?.currentStep) {
                        EditNameDialog(activity, activity).show()
                    }
                }
            }

            1 -> {
                contentView.text = "connect wifi"
                contentView.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                val contentView2 = view?.findViewById(R.id.item_content_2) as TextView
                contentView2.text = "open hotspot"
                contentView2.paintFlags = Paint.UNDERLINE_TEXT_FLAG

                contentView.setOnClickListener {
                    if (index != mVerticalStepperView?.currentStep) return@setOnClickListener
                    val intent = Intent()
                    val action = arrayOf(WifiManager.ACTION_PICK_WIFI_NETWORK, Settings.ACTION_WIFI_SETTINGS)
                    for (str in action) {
                        try {
                            intent.action = Settings.ACTION_WIFI_SETTINGS
                            startActivity(intent)
                            break
                        } catch (ex: Exception) {
                        }
                    }
                }

                contentView2.setOnClickListener {
                    if (index != mVerticalStepperView?.currentStep) return@setOnClickListener
                    val dlg = WifiBottomSheetDialog(activity, activity)
                    dlg.show()
                }
            }

            2 -> {
                contentView.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                contentView.text = mSummaries?.get(index)
                contentView.setOnClickListener {
                    if (index == mVerticalStepperView?.currentStep) {
                        FilePickDialogFragment(this@HelpFragment.activity).apply {
                            show(this@HelpFragment.activity.supportFragmentManager, "FilePickDialogFragment")
                        }
                    }
                }
            }

            3 -> {
                contentView.text = "准备完毕"
                (view?.findViewById(R.id.button_next) as TextView).text = "结束"
            }
        }
    }
}