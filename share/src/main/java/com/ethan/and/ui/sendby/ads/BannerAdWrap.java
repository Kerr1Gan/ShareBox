package com.ethan.and.ui.sendby.ads;

import android.content.Context;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

public class BannerAdWrap {

    private List<AdView> adViews;

    private Context context;

    public BannerAdWrap(Context context) {
        this.adViews = new ArrayList<>();
        this.context = context;
    }

    public void load(int count, String admobBannerUnitId) {
        for (int i = 0; i < count; i++) {
            AdView adView = new AdView(context);
            adView.setAdSize(AdSize.SMART_BANNER);
            adView.setAdUnitId(admobBannerUnitId);
            adViews.add(adView);
        }
    }
}
