package com.snakat.ads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

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
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

class AdsManagerInternal {

    private static final String TAG = AdsManager.class.getName();

    static boolean LOG_ENABLED = false;

    protected final WeakReference<Context> mContext;

    protected boolean mInitialized = false;

    protected AdsManagerInternal(Context context, boolean logEnabled) {
        mContext = new WeakReference<>(context);

        LOG_ENABLED = logEnabled;
    }

    @NonNull
    protected Completable initialize() {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                if (mInitialized) {
                    emitter.onComplete();
                } else {
                    MobileAds.initialize(mContext.get(), new OnInitializationCompleteListener() {
                        @Override
                        public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                            if (LOG_ENABLED) {
                                Log.i(TAG, "Ads initialized.");
                            }
                            mInitialized = true;
                            emitter.onComplete();
                        }
                    });
                }
            }
        });
    }

    protected void dispose() {
        mContext.clear();
    }

    @NonNull
    protected Observable<AdsEvent> loadAdView(@NonNull ViewGroup container, @NonNull AdSize adSize, @NonNull String adUnitId) {
        return Observable.create(new ObservableOnSubscribe<AdsEvent>() {
            @Override
            public void subscribe(ObservableEmitter<AdsEvent> emitter) throws Exception {
                AdView adView = new AdView(mContext.get());
                adView.setAdSize(adSize);
                adView.setAdUnitId(adUnitId);
                adView.loadAd(new AdRequest.Builder().build());
                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        emitter.onNext(new AdsEvent(AdsEvent.Type.FAILED_TO_LOAD, loadAdError.getMessage()));
                    }

                    @Override
                    public void onAdLoaded() {
                        emitter.onNext(new AdsEvent(AdsEvent.Type.LOADED));
                    }

                    @Override
                    public void onAdClicked() {
                        emitter.onNext(new AdsEvent(AdsEvent.Type.CLICKED));
                    }

                    @Override
                    public void onAdClosed() {
                        emitter.onNext(new AdsEvent(AdsEvent.Type.DISMISSED));
                    }
                });
                container.addView(adView);
            }
        });
    }

    @NonNull
    protected Single<InterstitialAd> loadInterstitialAd(@NonNull String adUnitId) {
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

    @NonNull
    protected Observable<AdsEvent> showInterstitialAd(@NonNull Activity activity, @NonNull InterstitialAd interstitialAd) {
        return Observable.create(new ObservableOnSubscribe<AdsEvent>() {
            @Override
            public void subscribe(ObservableEmitter<AdsEvent> emitter) throws Exception {
                emitter.onNext(new AdsEvent(AdsEvent.Type.LOADED));

                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdClicked() {
                        emitter.onNext(new AdsEvent(AdsEvent.Type.CLICKED));
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        emitter.onNext(new AdsEvent(AdsEvent.Type.DISMISSED));
                        emitter.onComplete();
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

    @NonNull
    protected <T> Observable<T> addLog(@NonNull String title, @NonNull Observable<T> observable) {
        return observable
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        Log.i(TAG, String.format("%s.OnSubscribe.", title));
                    }
                })
                .doOnNext(new Consumer<T>() {
                    @Override
                    public void accept(T t) throws Exception {
                        Log.i(TAG, String.format("%s.OnNext.item=%s", title, t));
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.i(TAG, String.format("%s.OnError: %s", title, throwable.getLocalizedMessage()));
                        throwable.printStackTrace();
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.i(TAG, String.format("%s.OnComplete.", title));
                    }
                });
    }
}
