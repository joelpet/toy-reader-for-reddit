package se.joelpet.android.toyreaderforreddit.storage;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import se.joelpet.android.toyreaderforreddit.Preferences;
import se.joelpet.android.toyreaderforreddit.domain.AccessToken;
import se.joelpet.android.toyreaderforreddit.domain.Me;

import static com.google.common.base.Preconditions.checkNotNull;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class DefaultLocalDataStore implements LocalDataStore {

    private final Preferences mPreferences;

    @Nullable
    private BehaviorSubject<Me> mMeSubject;
    @Nullable
    private BehaviorSubject<String> mAuthCodeSubject;

    @Inject
    public DefaultLocalDataStore(Preferences preferences) {
        mPreferences = preferences;
    }

    @Override
    public Observable<Me> observeMe() {
        if (mMeSubject == null) {
            // TODO: Potentially make use of DAO layer in here, e.g. mMeDao.get();
            Me me = mPreferences.getMe();
            mMeSubject = me != null ? BehaviorSubject.create(me) : BehaviorSubject.<Me>create();
        }
        return mMeSubject;
    }

    @Override
    public Observable<Me> putMe(final Me me) {
        return Observable.create(new Observable.OnSubscribe<Me>() {
            @Override
            public void call(Subscriber<? super Me> subscriber) {
                mPreferences.putMe(me);
                subscriber.onNext(me);

                if (mMeSubject != null) {
                    mMeSubject.onNext(me);
                }
            }
        })
                .compose(this.<Me>runInBackgroundAndNotifyOnMainThread())
                .compose(this.<Me>cacheAndSubscribe());
    }

    @Override
    public Observable<Void> deleteMe() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                mPreferences.deleteMe();
                subscriber.onCompleted();
                if (mMeSubject != null) {
                    mMeSubject.onNext(null);
                }
            }
        })
                .compose(this.<Void>runInBackgroundAndNotifyOnMainThread())
                .compose(this.<Void>cacheAndSubscribe());
    }

    @Override
    public Observable<String> putAuthCode(final String authCode) {
        checkNotNull(authCode);
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                mPreferences.putAuthCode(authCode);
                subscriber.onNext(authCode);
                subscriber.onCompleted();
            }
        })
                .compose(this.<String>runInBackgroundAndNotifyOnMainThread())
                .compose(this.<String>cacheAndSubscribe());
    }

    @Override
    public Observable<AccessToken> getAccessToken() {
        return Observable.create(new Observable.OnSubscribe<AccessToken>() {
            @Override
            public void call(Subscriber<? super AccessToken> subscriber) {
                AccessToken accessToken = mPreferences.getAccessToken();
                if (accessToken != null) {
                    subscriber.onNext(accessToken);
                }
                subscriber.onCompleted();
            }
        })
                .compose(this.<AccessToken>runInBackgroundAndNotifyOnMainThread());
    }

    @Override
    public Observable<AccessToken> putAccessToken(final AccessToken accessToken) {
        checkNotNull(accessToken);
        return Observable.create(new Observable.OnSubscribe<AccessToken>() {
            @Override
            public void call(Subscriber<? super AccessToken> subscriber) {
                mPreferences.putAccessToken(accessToken);
                subscriber.onNext(accessToken);
                subscriber.onCompleted();
            }
        })
                .compose(this.<AccessToken>runInBackgroundAndNotifyOnMainThread())
                .compose(this.<AccessToken>cacheAndSubscribe());
    }

    @Override
    public Observable<Void> deleteAccessToken() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                mPreferences.deleteAccessToken();
                subscriber.onCompleted();
            }
        })
                .compose(this.<Void>runInBackgroundAndNotifyOnMainThread())
                .compose(this.<Void>cacheAndSubscribe());
    }

    private <T> Observable.Transformer<T, T> runInBackgroundAndNotifyOnMainThread() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.observeOn(Schedulers.io()).subscribeOn(mainThread());
            }
        };
    }

    private <T> Observable.Transformer<T, T> cacheAndSubscribe() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                Observable<T> cached = observable.cache(1);
                cached.subscribe();
                return cached;
            }
        };
    }
}