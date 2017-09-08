package com.ecjtu.sharebox.ui.widget.web

import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * Created by KerriGan on 2017/8/5.
 */
class SimpleWebViewClient : WebViewClient() {

    init {

    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        view?.loadUrl(url)
        return true
    }
}
