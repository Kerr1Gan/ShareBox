package com.ethan.and.broadcast;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.flybd.sharebox.util.firebase.FirebaseManager;
import com.google.android.gms.analytics.CampaignTrackingReceiver;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * 监听并保存安装referrer
 * 测试方法：
 * 1 进到adb shell
 * 2 打开GAv4的log：setprop log.tag.GAv4 VERBOSE
 * 3 发送广播通知：
 * am broadcast -a com.android.vending.INSTALL_REFERRER -n com.mango.cash/com.duitplus.programs.biz.broadcast.InstallReferrerReceiver --es  "referrer" "utm_source=testSource&utm_medium=testMedium&utm_term=testTerm&utm_content=11&PARTNER_ID=111&PARTNER_CLICK_ID=222"
 */

public class InstallReferrerReceiver extends CampaignTrackingReceiver {

    public static String install_referrer_store_key = "GA_install_referrer_store_key";
    public static String install_referrer_from_ga_sdk_store_key = "install_referrer_from_ga_sdk_store_key";

    public void onReceive(Context context, Intent data) {
        super.onReceive(context, data);

        String referrerValue = getReferrerValue(data.getExtras());

        try {
            referrerValue = URLDecoder.decode(referrerValue, "utf-8");
            referrerValue = URLDecoder.decode(referrerValue, "utf-8");
            referrerValue = URLDecoder.decode(referrerValue, "utf-8");
            referrerValue = URLEncoder.encode(referrerValue, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(install_referrer_store_key, referrerValue).apply();
        try {
            Bundle bundle = new Bundle();
            bundle.putString("firebase_referrer", referrerValue);
            FirebaseManager.INSTANCE.logEvent("referrer", bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getReferrerValue(Bundle bundle) {
        String referrerValue = "";

        try {
            if (bundle != null) {
                referrerValue = bundle.getString("referrer");
            }
            if (referrerValue == null) {
                referrerValue = "";
            }

            if (TextUtils.isEmpty(referrerValue)) {
            } else {
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i("InstallReferrerReceiver", "referrer " + referrerValue);
        return referrerValue;
    }
}
