package se.joelpet.android.toyreaderforreddit.net;

import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import se.joelpet.android.toyreaderforreddit.accounts.AccountManagerHelper;
import se.joelpet.android.toyreaderforreddit.model.AccessToken;
import se.joelpet.android.toyreaderforreddit.model.Link;
import se.joelpet.android.toyreaderforreddit.model.Listing;
import timber.log.Timber;

public class OAuthRedditApi {

    private BaseRedditApi mBaseRedditApi;
    private AccountManagerHelper mAccountManagerHelper;
    private String mApplicationAccessToken;

    @Inject
    public OAuthRedditApi(BaseRedditApi baseRedditApi, AccountManagerHelper accountManagerHelper) {
        mBaseRedditApi = baseRedditApi;
        mAccountManagerHelper = accountManagerHelper;
    }

    public Observable<Listing<Link>> getLinkListing(final String path, final String after,
                                                    final Object tag) {
        return getUserOrApplicationAccessToken(tag)
                .flatMap(new Func1<String, Observable<Listing<Link>>>() {
                    @Override
                    public Observable<Listing<Link>> call(String authToken) {
                        return mBaseRedditApi.getLinkListing(authToken, path, after, tag);
                    }
                }).onErrorResumeNext(new Func1<Throwable, Observable<Listing<Link>>>() {
                    @Override
                    public Observable<Listing<Link>> call(Throwable throwable) {
                        boolean isAuthFailure = throwable.getCause() instanceof AuthFailureError;
                        if (!isAuthFailure) return Observable.error(throwable);
                        Timber.d("First auth failure; invalidate auth token and retry");
                        return mAccountManagerHelper
                                .invalidateAuthToken()
                                .flatMap(new Func1<Void, Observable<String>>() {
                                    @Override
                                    public Observable<String> call(Void aVoid) {
                                        return getUserOrApplicationAccessToken(tag);
                                    }
                                })
                                .flatMap(new Func1<String, Observable<Listing<Link>>>() {
                                    @Override
                                    public Observable<Listing<Link>> call(String authToken) {
                                        return mBaseRedditApi
                                                .getLinkListing(authToken, path, after, tag);
                                    }
                                });
                    }
                });
    }

    @NonNull
    private Observable<String> getUserOrApplicationAccessToken(final Object tag) {
        return mAccountManagerHelper.getAuthToken().single()
                .onErrorResumeNext(getApplicationAccessToken(tag));
    }

    private Observable<String> getApplicationAccessToken(Object tag) {
        if (mApplicationAccessToken != null) {
            return Observable.just(mApplicationAccessToken);
        }
        return mBaseRedditApi.getApplicationAccessToken(tag)
                .map(new Func1<AccessToken, String>() {
                    @Override
                    public String call(AccessToken accessToken) {
                        return accessToken.getAccessToken();
                    }
                })
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String accessToken) {
                        Timber.d("Cache application access token; length=%s", accessToken.length());
                        mApplicationAccessToken = accessToken;
                    }
                });
    }

    public void cancelAll(Object tag) {
        mBaseRedditApi.cancelAll(tag);
    }
}
