package com.sendby.ads;

import android.content.Context;

import com.google.android.gms.ads.MobileAds;

public class AdCoreManager {

    public static void initInmobi(Context context, String accountId) {
//        JSONObject consentObject = new JSONObject();
//        try {
//            // Provide correct consent value to sdk which is obtained by User
//            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true);
//            // Provide 0 if GDPR is not applicable and 1 if applicable
//            consentObject.put("gdpr", "0");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        InMobiSdk.init(context.getApplicationContext(), accountId, consentObject);
    }

//    public static void initFbAudienceNetwork(Context context) {
//        // Example for setting the SDK to crash when in debug mode
//        AdSettings.setIntegrationErrorMode(AdSettings.IntegrationErrorMode.INTEGRATION_ERROR_CALLBACK_MODE);
//        AudienceNetworkInitializeHelper.initialize(context.getApplicationContext());
//    }

    public static void initAdmob(Context context, String appId) {
        MobileAds.initialize(context, appId);
    }
}

