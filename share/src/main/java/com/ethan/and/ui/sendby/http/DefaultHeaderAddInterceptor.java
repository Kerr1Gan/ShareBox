package com.ethan.and.ui.sendby.http;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.flybd.sharebox.BuildConfig;
import com.flybd.sharebox.R;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import org.ecjtu.channellibrary.wifiutil.NetworkUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by XLEO on 2018/1/30.
 */
public class DefaultHeaderAddInterceptor implements Interceptor {

    private Context context;

    public DefaultHeaderAddInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request userRequest = chain.request();
        Request.Builder requestBuilder = userRequest.newBuilder();

        if (userRequest.header("Connection") == null) {
            requestBuilder.header("Connection", "Keep-Alive");
        }
//        if (userRequest.header("Accept-Encoding") == null && userRequest.header("Range") == null) {
//            requestBuilder.header("Accept-Encoding", "gzip");
//        }
        String appsFlyersId = "";
        Locale locale = context.getResources().getConfiguration().locale;
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        requestBuilder.header("X-APP-TYPE", "ANDROID")
                .header("X-APP-VERSION", String.valueOf(BuildConfig.VERSION_CODE))
                .header("X-APP-VERSION-NAME", String.valueOf(BuildConfig.VERSION_NAME))
                .header("X-APP-PACKAGE-NAME", getPackageName())
                .header("X-APP-NAME", getAppName())
                .header("X-AF-ID", appsFlyersId != null ? appsFlyersId : "")
                .header("X-GA-ID", getGAId())
                .header("X-LANGUAGE", lang)
                .header("X-COUNTRY", country)
                .header("X-APP-SIGNATURE", getSignatureHashKey())
                .header("X-SK", getSK())
                .header("X-APP-TYPE-V2", "ANDROID")
                .header("X-VERSION-" + BuildConfig.VERSION_CODE, getSK())
                .header("X-ANDROID-ID", TraceSender.getAndroidID(context))
                .header("X-REFERRER", getReferrer())
                .header("X-SIM-OPERATOR", NetworkUtil.getCurrentSimOperator(context))
                .header("X-NETWORK-STATE", String.valueOf(NetworkUtil.getNetworkState(context)))
                .header("X-GP-AVAILABLE", String.valueOf(isGooglePlayServiceAvailable()));

        return chain.proceed(requestBuilder.build());
    }

    private @NotNull
    String getPackageName() {
        return BuildConfig.APPLICATION_ID;
    }

    private String getGAId() {
        AdvertisingIdClient.Info adInfo = null;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            final String id = adInfo.getId();
            //final boolean isLAT = adInfo.isLimitAdTrackingEnabled();
            return id;
        } catch (IOException e) {
            // Unrecoverable error connecting to Google Play services (e.g.,
            // the old version of the service doesn't support getting AdvertisingId).

        } catch (GooglePlayServicesNotAvailableException e) {
            // Google Play services is not available entirely.
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
        return "";
    }

    @NotNull
    private String getAppName() {
        return context.getString(R.string.app_name);
    }

    private boolean isGooglePlayServiceAvailable() {
        try {
            if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
                // The SafetyNet Attestation API is available.
                return true;
            } else {
                // Prompt user to update Google Play services.
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private String getSignatureHashKey() {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                //KeyHash 就是你要的，不用改任何代码  复制粘贴 ;
                return Base64.encodeToString(md.digest(), Base64.DEFAULT).replaceAll("\n", "");
            }
        } catch (Exception e) {
        }
        return "";
    }

    private String getSK() {
        String sk = String.valueOf(System.currentTimeMillis());
        sk = new StringBuffer(sk).reverse().toString();
        return sk;
    }

    private String getReferrer() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("referrer", "");
    }
}

