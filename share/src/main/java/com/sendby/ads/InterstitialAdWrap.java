package com.sendby.ads;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.flybd.sharebox.BuildConfig;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;


public class InterstitialAdWrap {

    private static final String TAG = "InterstitialAdWrap";

    private static final long INTERVAL_TIME = 500;

    private Context context;

    private String admobRewardVideoId;

    private String admobInterstitialAdId;

    private String audienceRewardVideoId;

    private String audienceInterstitialAdId;

    private RewardedVideoAd admobRewardVideoAd;

    private InterstitialAd admobInterstitialAd;

    private boolean stickyShowVideo = false;

    private boolean stickyShowInterstitial = false;

    private boolean isPause = false;

    private Handler handler = new Handler(Looper.getMainLooper());

    private IRewardListener rewardListener;

    public InterstitialAdWrap(Context context, String admobRewardVideoId, String admobInterstitialAdId, String audienceRewardVideoId, String audienceInterstitialAdId) {
        this.context = context;
        this.admobRewardVideoId = admobRewardVideoId;
        this.admobInterstitialAdId = admobInterstitialAdId;
        this.audienceRewardVideoId = audienceRewardVideoId;
        this.audienceInterstitialAdId = audienceInterstitialAdId;
    }

    public void setRewardListener(IRewardListener listener) {
        this.rewardListener = listener;
    }

    public void loadAd() {
        if (isPause) {
            return;
        }
        if (admobRewardVideoAd == null) {
            admobRewardVideoAd = MobileAds.getRewardedVideoAdInstance(context);
            admobRewardVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
                @Override
                public void onRewardedVideoAdLoaded() {
                    Log.i(TAG, "onRewardedVideoAdLoaded: admob rewardVideo");
                    if (stickyShowVideo) {
                        if (!isPause) {
                            admobRewardVideoAd.show();
                        }
                        stickyShowVideo = false;
                    }
                }

                @Override
                public void onRewardedVideoAdOpened() {
                    Log.i(TAG, "onRewardedVideoAdOpened: admob rewardVideo");
                }

                @Override
                public void onRewardedVideoStarted() {
                    Log.i(TAG, "onRewardedVideoStarted: admob rewardVideo");
                    if (rewardListener != null) {
                        rewardListener.onRewardVideoAdImpression();
                    }
                }

                @Override
                public void onRewardedVideoAdClosed() {
                    Log.i(TAG, "onRewardedVideoAdClosed: admob rewardVideo");
                    loadAdmobVideoAd();
                }

                @Override
                public void onRewarded(RewardItem rewardItem) {
                    Log.i(TAG, "onRewarded: admob rewardVideo");
                    if (rewardListener != null) {
                        rewardListener.onReward();
                    }
                }

                @Override
                public void onRewardedVideoAdLeftApplication() {
                    Log.i(TAG, "onRewardedVideoAdLeftApplication: admob rewardVideo");
                }

                @Override
                public void onRewardedVideoAdFailedToLoad(int i) {
                    Log.i(TAG, "onRewardedVideoAdFailedToLoad: admob rewardVideo code=" + i);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadAd();
                        }
                    }, INTERVAL_TIME);
                }

                @Override
                public void onRewardedVideoCompleted() {
                    Log.i(TAG, "onRewardedVideoCompleted: admob rewardVideo");
                }
            });
        }
        if (!admobRewardVideoAd.isLoaded()) {
            loadAdmobVideoAd();
        }
        if (admobInterstitialAd == null) {
            admobInterstitialAd = new InterstitialAd(context);
            admobInterstitialAd.setImmersiveMode(true);
            if (BuildConfig.DEBUG) {
                admobInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
            } else {
                admobInterstitialAd.setAdUnitId(admobInterstitialAdId);
            }
            admobInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    Log.i(TAG, "onAdClosed: admob interstitial");
                    admobInterstitialAd.loadAd(new AdRequest.Builder().build());
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    Log.i(TAG, "onAdFailedToLoad: admob interstitial code=" + i);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadAd();
                        }
                    }, INTERVAL_TIME);
                }

                @Override
                public void onAdLeftApplication() {
                    Log.i(TAG, "onAdLeftApplication: admob interstitial");
                }

                @Override
                public void onAdOpened() {
                    Log.i(TAG, "onAdOpened: admob interstitial");
                    if (rewardListener != null) {
                        rewardListener.onInterstitialAdOpened();
                    }
                }

                @Override
                public void onAdLoaded() {
                    Log.i(TAG, "onAdLoaded: admob interstitial");
                    if (stickyShowInterstitial) {
                        if (!isPause) {
                            admobInterstitialAd.show();
                        }
                        stickyShowInterstitial = false;
                    }
                }

                @Override
                public void onAdClicked() {
                    Log.i(TAG, "onAdClicked: admob interstitial");
                }

                @Override
                public void onAdImpression() {
                    Log.i(TAG, "onAdImpression: admob interstitial"); // 插页广告展示的时候，这个不会被回调？？
                    if (rewardListener != null) {
                        rewardListener.onInterstitialAdImpression();
                    }
                }
            });
        }
        if (!admobInterstitialAd.isLoaded()) {
            admobInterstitialAd.loadAd(new AdRequest.Builder().build());
        }

//        rewardedVideoAd.setRewardData(new RewardData("YOUR_USER_ID", "YOUR_REWARD"));
    }

    private void loadAdmobVideoAd() {
        if (admobRewardVideoAd == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            admobRewardVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());
        } else {
            admobRewardVideoAd.loadAd(admobRewardVideoId, new AdRequest.Builder().build());
        }
    }

    public boolean showAd() {
        stickyShowVideo = true;
        stickyShowInterstitial = true;
        if (admobRewardVideoAd != null && admobRewardVideoAd.isLoaded()) {
            admobRewardVideoAd.show();
            stickyShowVideo = false;
            stickyShowInterstitial = false;
            return true;
        } else if (admobInterstitialAd != null && admobInterstitialAd.isLoaded()) {
            admobInterstitialAd.show();
            stickyShowVideo = false;
            stickyShowInterstitial = false;
            return true;
        }
        return false;
    }

    public void forceDestroy() {
        //admobRewardVideoAd.destroy(context); 不能销毁，应该是单例，销毁后视频广告会黑屏
        admobRewardVideoAd = null;
        admobInterstitialAd = null;
        stickyShowVideo = false;
        stickyShowInterstitial = false;
    }

    public void onResume() {
        isPause = false;
        loadAd();
        if (admobRewardVideoAd != null) {
            admobRewardVideoAd.resume(context);
        }
    }

    public void onPause() {
        isPause = true;
        handler.removeCallbacksAndMessages(null);
        if (admobRewardVideoAd != null) {
            admobRewardVideoAd.pause(context);
        }
    }

    public void onDestroy() {
        if (admobRewardVideoAd != null) {
            admobRewardVideoAd.destroy(context);
        }
        handler.removeCallbacksAndMessages(null);
    }

    public boolean isVideoReady() {
        if ((admobRewardVideoAd != null && admobRewardVideoAd.isLoaded())) {
            return true;
        }
        return false;
    }

    public boolean isInterstitialReady() {
        if ((admobInterstitialAd != null && admobInterstitialAd.isLoaded())) {
            return true;
        }
        return false;
    }

    public void showInterstitialAd() {
        stickyShowVideo = false;
        stickyShowInterstitial = true;
        if (admobInterstitialAd != null && admobInterstitialAd.isLoaded()) {
            admobInterstitialAd.show();
            stickyShowInterstitial = false;
        }
    }

    public void showVideoAd() {
        stickyShowVideo = true;
        stickyShowInterstitial = false;
        if (admobRewardVideoAd != null && admobRewardVideoAd.isLoaded()) {
            admobRewardVideoAd.show();
            stickyShowVideo = false;
        }
    }

    public interface IRewardListener {
        void onReward();

        void onInterstitialAdImpression();

        void onInterstitialAdOpened();

        void onRewardVideoAdImpression();
    }
}
