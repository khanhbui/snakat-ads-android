package com.snakat.ads;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.lang.ref.WeakReference;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Function;

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
        mInstance.dispose();
        mInstance = null;
    }

    private final WeakReference<Context> mContext;

    private boolean mInitialized;

    private AdsManager(Context context) {
        mContext = new WeakReference<>(context);

        mInitialized = false;
    }

    @NonNull
    private Completable initialize() {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                MobileAds.initialize(mContext.get(), new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                        mInitialized = true;
                        emitter.onComplete();
                    }
                });
            }
        });
    }

    private void dispose() {
        mContext.clear();
    }

    @NonNull
    private Observable<EventType> loadAdView(@NonNull ViewGroup container, @NonNull AdSize adSize, @NonNull String adUnitId) {
        return Observable.create(new ObservableOnSubscribe<EventType>() {
            @Override
            public void subscribe(ObservableEmitter<EventType> emitter) throws Exception {
                AdView adView = new AdView(mContext.get());
                adView.setAdSize(adSize);
                adView.setAdUnitId(adUnitId);
                adView.loadAd(new AdRequest.Builder().build());
                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        emitter.onError(new Exception(loadAdError.getMessage()));
                    }

                    @Override
                    public void onAdLoaded() {
                        emitter.onNext(EventType.LOADED);
                    }

                    @Override
                    public void onAdClicked() {
                        emitter.onNext(EventType.CLICKED);
                    }

                    @Override
                    public void onAdClosed() {
                        emitter.onNext(EventType.DISMISSED);
                    }
                });
                container.addView(adView);
            }
        });
    }

    @NonNull
    private Single<InterstitialAd> loadInterstitialAd(@NonNull String adUnitId) {
        return Single.create(new SingleOnSubscribe<InterstitialAd>() {
            @Override
            public void subscribe(SingleEmitter<InterstitialAd> emitter) throws Exception {
                AdRequest adRequest = new AdRequest.Builder().build();
                InterstitialAd.load(mContext.get(), adUnitId, adRequest, new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        emitter.onError(new Exception(loadAdError.getMessage()));
                    }

                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        emitter.onSuccess(interstitialAd);
                    }
                });
            }
        });
    }

    public Observable<EventType> showBanner(@NonNull ViewGroup container, @StringRes int adUnitId) {
        return showBanner(container, mContext.get().getString(adUnitId));
    }

    public Observable<EventType> showBanner(@NonNull ViewGroup container, @NonNull String adUnitId) {
        return mInitialized ?
                loadAdView(container, AdSize.BANNER, adUnitId) : initialize().andThen(loadAdView(container, AdSize.BANNER, adUnitId));
    }

    public Observable<EventType> showInterstitial(Activity activity, int adUnitId) {
        return showInterstitial(activity, mContext.get().getString(adUnitId));
    }

    @NonNull
    public Observable<EventType> showInterstitial(Activity activity, String adUnitId) {
        Single<InterstitialAd> single = mInitialized ?
                loadInterstitialAd(adUnitId) : initialize().andThen(loadInterstitialAd(adUnitId));
        return single.flatMapObservable(new Function<InterstitialAd, ObservableSource<? extends EventType>>() {
            @Override
            public ObservableSource<? extends EventType> apply(InterstitialAd interstitialAd) throws Exception {
                return Observable.create(new ObservableOnSubscribe<EventType>() {
                    @Override
                    public void subscribe(ObservableEmitter<EventType> emitter) throws Exception {
                        emitter.onNext(EventType.LOADED);

                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdClicked() {
                                emitter.onNext(EventType.CLICKED);
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                emitter.onNext(EventType.DISMISSED);
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                emitter.onError(new Exception(adError.getMessage()));
                            }
                        });
                        interstitialAd.show(activity);
                    }
                });
            }
        });
    }

    public enum EventType {
        LOADED,
        CLICKED,
        DISMISSED
    }
}
