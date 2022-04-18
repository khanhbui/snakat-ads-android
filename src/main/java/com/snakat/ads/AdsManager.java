package com.snakat.ads;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.interstitial.InterstitialAd;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public final class AdsManager extends AdsManagerInternal {

    private static AdsManager mInstance;

    public static AdsManager getInstance() {
        return mInstance;
    }

    public static void createInstance(@NonNull Context context) {
        createInstance(context, false);
    }

    public static void createInstance(@NonNull Context context, boolean logEnabled) {
        if (mInstance == null) {
            mInstance = new AdsManager(context, logEnabled);
        }
    }

    public static void destroyInstance() {
        mInstance.dispose();
        mInstance = null;
    }

    private AdsManager(@NonNull Context context, boolean logEnabled) {
        super(context, logEnabled);
    }

    @NonNull
    public Observable<AdsEvent> showBanner(@NonNull ViewGroup container, @StringRes int adUnitId) {
        return showBanner(container, mContext.get().getString(adUnitId));
    }

    @NonNull
    public Observable<AdsEvent> showBanner(@NonNull ViewGroup container, @NonNull String adUnitId) {
        Observable<AdsEvent> observable = initialize()
                .andThen(loadAdView(container, AdSize.BANNER, adUnitId));

        if (LOG_ENABLED) {
            observable = addLog("showBanner", observable);
        }

        return observable;
    }

    @NonNull
    public Observable<AdsEvent> showInterstitial(@NonNull Activity activity, @StringRes int adUnitId) {
        return showInterstitial(activity, mContext.get().getString(adUnitId));
    }

    @NonNull
    public Observable<AdsEvent> showInterstitial(@NonNull Activity activity, @NonNull String adUnitId) {
        Observable<AdsEvent> observable = initialize()
                .andThen(loadInterstitialAd(adUnitId))
                .flatMapObservable(new Function<InterstitialAd, ObservableSource<? extends AdsEvent>>() {
                    @Override
                    public ObservableSource<? extends AdsEvent> apply(InterstitialAd interstitialAd) throws Exception {
                        return showInterstitialAd(activity, interstitialAd);
                    }
                });

        if (LOG_ENABLED) {
            observable = addLog("showInterstitial", observable);
        }

        return observable;
    }
}
