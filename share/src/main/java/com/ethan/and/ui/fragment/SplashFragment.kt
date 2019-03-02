package com.ethan.and.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ecjtu.sharebox.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

/**
 * Created by Ethan_Xiang on 2017/9/6.
 */
class SplashFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adView = view?.findViewById<View>(R.id.adView) as AdView
        val request = AdRequest.Builder()
        adView.loadAd(request.build())
    }
}