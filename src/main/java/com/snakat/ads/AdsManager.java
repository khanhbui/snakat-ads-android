package com.snakat.ads;

import android.content.Context;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class AdsManager {

    private static AdsManager mInstance;
    public static AdsManager getInstance() {
        return mInstance;
    }
    public static void createInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AdsManager(context);
        }
    }
    public static void destroyInstance() {
        mInstance = null;
    }

    private AdsManager(Context context) {
        MobileAds.initialize(context, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
    }
}
