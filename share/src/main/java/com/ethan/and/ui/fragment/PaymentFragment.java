package com.ethan.and.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.common.componentes.fragment.LazyInitFragment;
import com.ethan.and.ui.sendby.googlepay.AugmentedSkuDetails;
import com.google.gson.Gson;

import org.json.JSONException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class PaymentFragment extends LazyInitFragment {

    private static final String TAG = "PaymentFragment";

    private BillingClient playStoreBillingClient;

    private List<SkuDetails> skuDetailList;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startDataSourceConnections();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Correlated data sources belong inside a repository module so that the rest of
     * the app can have appropriate access to the data it needs. Still, it may be effective to
     * track the opening (and sometimes closing) of data source connections based on lifecycle
     * events. One convenient way of doing that is by calling this
     * [startDataSourceConnections] when the [BillingViewModel] is instantiated and
     * [endDataSourceConnections] inside [ViewModel.onCleared]
     */
    private void startDataSourceConnections() {
        Log.i(TAG, "startDataSourceConnections");
        instantiateAndConnectToPlayBillingService();
    }

    private void endDataSourceConnections() {
        playStoreBillingClient.endConnection();
        // normally you don't worry about closing a DB connection unless you have more than
        // one DB open. so no need to call 'localCacheBillingClient.close()'
        Log.i(TAG, "endDataSourceConnections");
    }

    private void instantiateAndConnectToPlayBillingService() {
        playStoreBillingClient = BillingClient.newBuilder(getContext().getApplicationContext())
                .enablePendingPurchases() // required or app will crash
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
                        int responseCode = billingResult.getResponseCode();
                        Log.i(TAG, "onPurchasesUpdated: " + new Gson().toJson(billingResult));
                        Log.i(TAG, "onPurchasesUpdated: " + new Gson().toJson(list));
                        if (responseCode == BillingClient.BillingResponseCode.OK) {

                        } else if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {

                        } else if (responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                            connectToPlayBillingService();
                        } else {
                            Log.i(TAG, billingResult.getDebugMessage());
                        }
                    }
                }).build();
        connectToPlayBillingService();
    }

    private boolean connectToPlayBillingService() {
        Log.i(TAG, "connectToPlayBillingService");
        if (!playStoreBillingClient.isReady()) {
            playStoreBillingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    int responseCode = billingResult.getResponseCode();
                    if (responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.i(TAG, "onBillingSetupFinished successfully");
                        querySkuDetailsAsync(BillingClient.SkuType.INAPP, Arrays.asList("gas", "premium_car"));
                        //querySkuDetailsAsync(BillingClient.SkuType.SUBS, Arrays.asList("gold_monthly", "gold_yearly"));
                        queryPurchasesAsync();
                    } else if (responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
                        Log.i(TAG, billingResult.getDebugMessage());
                    } else {
                        Log.i(TAG, billingResult.getDebugMessage());
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    Log.i(TAG, "onBillingServiceDisconnected");
                    connectToPlayBillingService();
                }
            });
            return true;
        }
        return false;
    }

    /**
     * BACKGROUND
     * <p>
     * Google Play Billing refers to receipts as [Purchases][Purchase]. So when a user buys
     * something, Play Billing returns a [Purchase] object that the app then uses to release the
     * [Entitlement] to the user. Receipts are pivotal within the [BillingRepository]; but they are
     * not part of the repo’s public API, because clients don’t need to know about them. When
     * the release of entitlements occurs depends on the type of purchase. For consumable products,
     * the release may be deferred until after consumption by Google Play; for non-consumable
     * products and subscriptions, the release may be deferred until after
     * [BillingClient.acknowledgePurchaseAsync] is called. You should keep receipts in the local
     * cache for augmented security and for making some transactions easier.
     * <p>
     * THIS METHOD
     * <p>
     * [This method][queryPurchasesAsync] grabs all the active purchases of this user and makes them
     * available to this app instance. Whereas this method plays a central role in the billing
     * system, it should be called at key junctures, such as when user the app starts.
     * <p>
     * Because purchase data is vital to the rest of the app, this method is called each time
     * the [BillingViewModel] successfully establishes connection with the Play [BillingClient]:
     * the call comes through [onBillingSetupFinished]. Recall also from Figure 4 that this method
     * gets called from inside [onPurchasesUpdated] in the event that a purchase is "already
     * owned," which can happen if a user buys the item around the same time
     * on a different device.
     */
    private void queryPurchasesAsync() {
        Log.d(TAG, "queryPurchasesAsync called");
        HashSet<Purchase> purchasesResult = new HashSet<>();
        Purchase.PurchasesResult result = playStoreBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
        Log.d(TAG, "queryPurchasesAsync INAPP results: " + result.getPurchasesList().size());
//        ConsumeParams params =
//                ConsumeParams.newBuilder().setPurchaseToken(result.getPurchasesList().get(0).getPurchaseToken()).build();
//        playStoreBillingClient.consumeAsync(params, new ConsumeResponseListener() {
//            @Override
//            public void onConsumeResponse(BillingResult billingResult, String s) {
//
//            }
//        });
        if (result.getPurchasesList() != null) {
            result.getPurchasesList().addAll(result.getPurchasesList());
        }
        if (isSubscriptionSupported()) {
            result = playStoreBillingClient.queryPurchases(BillingClient.SkuType.SUBS);
            if (result.getPurchasesList() != null) {
                purchasesResult.addAll(result.getPurchasesList());
            }

            Log.d(TAG, "queryPurchasesAsync SUBS results: " + result.getPurchasesList().size());
        }
        //processPurchases(purchasesResult)
    }

    /**
     * Checks if the user's device supports subscriptions
     */
    private boolean isSubscriptionSupported() {
        BillingResult billingResult =
                playStoreBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS);
        boolean succeeded = false;
        int responseCode = billingResult.getResponseCode();
        if (responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
            succeeded = true;
        } else if (responseCode == BillingClient.BillingResponseCode.OK) {
            connectToPlayBillingService();
        } else {
            Log.i(TAG, "isSubscriptionSupported: " + billingResult.getDebugMessage());
        }

        return succeeded;
    }

    /**
     * Presumably a set of SKUs has been defined on the Google Play Developer Console. This
     * method is for requesting a (improper) subset of those SKUs. Hence, the method accepts a list
     * of product IDs and returns the matching list of SkuDetails.
     * <p>
     * The result is passed to [onSkuDetailsResponse]
     */
    private void querySkuDetailsAsync(@BillingClient.SkuType String skuType,
                                      List<String> skuList) {
        SkuDetailsParams params = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(skuType).build();
        Log.d(TAG, "querySkuDetailsAsync for " + skuType);
        playStoreBillingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> list) {
                int responseCode = billingResult.getResponseCode();
                if (responseCode == BillingClient.BillingResponseCode.OK) {
                    if (list != null && list.size() > 0) {
                        skuDetailList = list;
                        for (SkuDetails detail : list) {
                            Log.i(TAG, "onSkuDetailsResponse: " + new Gson().toJson(detail));
                        }
                        launchBillingFlow(getActivity(), list.get(0));
                    }
                } else {
                    Log.i(TAG, "onSkuDetailsResponse: " + billingResult.getDebugMessage());
                }
            }
        });
    }

    /**
     * This is the function to call when user wishes to make a purchase. This function will
     * launch the Google Play Billing flow. The response to this call is returned in
     * [onPurchasesUpdated]
     */
    private void launchBillingFlow(Activity activity, AugmentedSkuDetails augmentedSkuDetails) throws JSONException {
        launchBillingFlow(activity, new SkuDetails(augmentedSkuDetails.getOriginalJson()));
    }

    private void launchBillingFlow(Activity activity, SkuDetails skuDetails) {
        BillingFlowParams purchaseParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build();
        playStoreBillingClient.launchBillingFlow(activity, purchaseParams);
    }
}