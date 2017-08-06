package com.ecjtu.sharebox.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.ui.view.webview.SimpleWebChromeClient
import com.ecjtu.sharebox.ui.view.webview.SimpleWebViewClient


/**
 * Created by KerriGan on 2017/8/4.
 */
class WebViewFragment:Fragment(){

    companion object {
        const val EXTRA_URL="extra_url"
    }

    private var mWebView: WebView?=null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_web_view,container,false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mWebView=view?.findViewById(R.id.web_view) as WebView?
        mWebView?.setWebViewClient(SimpleWebViewClient())
        mWebView?.setWebChromeClient(SimpleWebChromeClient())

        val settings = mWebView?.getSettings()
        settings?.javaScriptEnabled = true
        if(arguments!=null){
            var url=arguments.get(EXTRA_URL)
            if(url!=null){
                mWebView?.loadUrl(url as String)
            }
        }
    }
}