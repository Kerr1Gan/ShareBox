package com.ecjtu.sharebox.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.ui.view.webview.SimpleWebChromeClient
import com.ecjtu.sharebox.ui.view.webview.SimpleWebViewClient
import com.ecjtu.sharebox.util.file.FileUtil
import java.io.*


/**
 * Created by KerriGan on 2017/8/4.
 */
class WebViewFragment : Fragment() {

    companion object {
        const val TAG = "WebViewFragment"
        const val EXTRA_URL = "extra_url"
        const val WEB_ROOT_PATH = "web"
        const val EXTRA_TYPE = "extra_type"
        const val TYPE_INNER_WEB = 0x10
        const val TYPE_DEFAULT = TYPE_INNER_WEB shl 1
    }

    private var mWebView: WebView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_web_view, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mWebView = view?.findViewById(R.id.web_view) as WebView?
        mWebView?.setWebViewClient(SimpleWebViewClient())
        mWebView?.setWebChromeClient(SimpleWebChromeClient())

        val settings = mWebView?.getSettings()
        settings?.javaScriptEnabled = true
        if (arguments != null) {
            var url = arguments.get(EXTRA_URL) as String
            var type = arguments.get(EXTRA_TYPE)
            if (type == null) {
                type = TYPE_DEFAULT
            }

            if (type == TYPE_DEFAULT) {
                if (url != null) {
                    mWebView?.loadUrl(url)
                }
            } else if (type == TYPE_INNER_WEB) {
                var file = context.getExternalFilesDir(WEB_ROOT_PATH)
                if (file != null) {
                    file = File(file, url)
                    if (file.exists() && !file.isDirectory) {
                        mWebView?.loadUrl("file://${file.absolutePath}")
                        Log.e(TAG,"load by dynamic")
                        return
                    }
                }
                mWebView?.loadUrl("file:///android_asset/${WEB_ROOT_PATH}/${url}")
                Log.e(TAG,"load by static")
            }
        }
    }
}