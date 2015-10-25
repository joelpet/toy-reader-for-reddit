package se.joelpet.android.toyreaderforreddit.storage;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.BehaviorSubject;
import se.joelpet.android.toyreaderforreddit.Preferences;
import se.joelpet.android.toyreaderforreddit.domain.AccessToken;
import se.joelpet.android.toyreaderforreddit.domain.Me;
import se.joelpet.android.toyreaderforreddit.rx.transformers.CacheAndSubscribeTransformer;
import se.joelpet.android.toyreaderforreddit.rx.transformers.WorkOnIoAndOnNotifyOnMainTransformer;

import static com.google.common.base.Preconditions.checkNotNull;

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
        }).compose(WorkOnIoAndOnNotifyOnMainTransformer.<Me>getInstance()
        ).compose(CacheAndSubscribeTransformer.<Me>getInstance());
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
        }).compose(WorkOnIoAndOnNotifyOnMainTransformer.<Void>getInstance()
        ).compose(CacheAndSubscribeTransformer.<Void>getInstance());
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
        }).compose(WorkOnIoAndOnNotifyOnMainTransformer.<String>getInstance()
        ).compose(CacheAndSubscribeTransformer.<String>getInstance());
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
        }).compose(WorkOnIoAndOnNotifyOnMainTransformer.<AccessToken>getInstance());
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
        }).compose(WorkOnIoAndOnNotifyOnMainTransformer.<AccessToken>getInstance()
        ).compose(CacheAndSubscribeTransformer.<AccessToken>getInstance());
    }

    @Override
    public Observable<Void> deleteAccessToken() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                mPreferences.deleteAccessToken();
                subscriber.onCompleted();
            }
        }).compose(WorkOnIoAndOnNotifyOnMainTransformer.<Void>getInstance()
        ).compose(CacheAndSubscribeTransformer.<Void>getInstance());
    }
}
