package com.ecjtu.sharebox.ui.dialog

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.ecjtu.netcore.network.AsyncNetwork
import com.ecjtu.netcore.network.IRequestCallbackV2
import com.ecjtu.sharebox.R
import java.lang.Exception
import java.net.HttpURLConnection

/**
 * Created by Ethan_Xiang on 2017/10/17.
 */
class IPSearchDialog(activity: Activity) : CloseBottomSheetDialog(activity, activity, R.style.BottomSheetDialogStyle) {

    private var mIp: String = ""

    private var mListener: ((String) -> Unit)? = null

    override fun onCreateView(): View? {
        val supView = super.onCreateView()
        setTitle(context.getString(R.string.searched_by_ip), supView as ViewGroup)
        val child = layoutInflater.inflate(R.layout.layout_ip_search_dialog, supView, false)
        supView.addView(child)
        return supView
    }

    override fun onViewCreated(view: View?): Boolean {
        val edit = view?.findViewById<View>(R.id.edit) as TextView?
        view?.findViewById<View>(R.id.positive)?.setOnClickListener {
            (findViewById<View>(R.id.progress_bar) as ProgressBar).visibility = View.VISIBLE
            edit?.let {
                mIp = edit.text.toString()
                if (!mIp.startsWith("http://")) {
                    mIp = "http://" + mIp
                }
                AsyncNetwork().request(mIp).setRequestCallback(object : IRequestCallbackV2 {
                    override fun onSuccess(httpURLConnection: HttpURLConnection?, response: String) {
                        view.post {
                            (findViewById<View>(R.id.progress_bar) as ProgressBar).visibility = View.INVISIBLE
                            mListener?.invoke(mIp)
                        }
                        this@IPSearchDialog.cancel()
                    }

                    override fun onError(httpURLConnection: HttpURLConnection?, exception: Exception) {
                        view.post {
                            (findViewById<View>(R.id.progress_bar) as ProgressBar).visibility = View.INVISIBLE
                            mListener?.invoke("")
                        }
                        this@IPSearchDialog.cancel()
                    }
                })
            }
        }
        return super.onViewCreated(view)
    }

    fun getIP(): String {
        return mIp
    }

    fun setCallback(l: ((ip: String) -> Unit)) {
        mListener = l
    }
}
