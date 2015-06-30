package se.joelpet.android.toyredditreader.storage;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import se.joelpet.android.toyredditreader.Preferences;
import se.joelpet.android.toyredditreader.domain.AccessToken;
import se.joelpet.android.toyredditreader.domain.Me;

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
    public void putMe(Me me) {
        mPreferences.putMe(me);
        if (mMeSubject != null) {
            mMeSubject.onNext(me);
        }
    }

    @Override
    public void putAuthCode(String authCode) {
        checkNotNull(authCode);
        mPreferences.putAuthCode(authCode);
        if (mAuthCodeSubject != null) {
            mAuthCodeSubject.onNext(authCode);
        }
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
        }).observeOn(Schedulers.io()).subscribeOn(mainThread());
    }

    @Override
    public Observable<AccessToken> putAccessToken(final AccessToken accessToken) {
        checkNotNull(accessToken);
        Observable<AccessToken> cache = Observable.create(new Observable.OnSubscribe<AccessToken>
                () {
            @Override
            public void call(Subscriber<? super AccessToken> subscriber) {
                mPreferences.putAccessToken(accessToken);
                subscriber.onNext(accessToken);
                subscriber.onCompleted();
            }
        }).observeOn(Schedulers.io()).subscribeOn(mainThread()).cache(1);
        // Subscribe so that access token gets stored even if returned observable is ignored
        cache.subscribe();
        return cache;
    }
}
