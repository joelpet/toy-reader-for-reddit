package se.joelpet.android.toyreaderforreddit.net;

import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import se.joelpet.android.toyreaderforreddit.VolleySingleton;
import se.joelpet.android.toyreaderforreddit.accounts.AccountManagerHelper;
import se.joelpet.android.toyreaderforreddit.domain.AccessToken;
import se.joelpet.android.toyreaderforreddit.domain.Link;
import se.joelpet.android.toyreaderforreddit.domain.Listing;
import se.joelpet.android.toyreaderforreddit.domain.Me;
import se.joelpet.android.toyreaderforreddit.volley.ApplicationAccessTokenRequest;
import se.joelpet.android.toyreaderforreddit.volley.ListingRequest;
import se.joelpet.android.toyreaderforreddit.volley.MeRequest;
import se.joelpet.android.toyreaderforreddit.volley.RefreshTokenRequest;
import se.joelpet.android.toyreaderforreddit.volley.UserAccessTokenRequest;
import timber.log.Timber;

public class RealBaseRedditApi implements BaseRedditApi {

    private final VolleySingleton mVolleySingleton;
    private final AccountManagerHelper mAccountManagerHelper;

    @Inject
    public RealBaseRedditApi(VolleySingleton volleySingleton,
                             AccountManagerHelper accountManagerHelper) {
        mVolleySingleton = volleySingleton;
        mAccountManagerHelper = accountManagerHelper;
    }

    @Override
    public Observable<AccessToken> getApplicationAccessToken(Object tag) {
        RequestFuture<AccessToken> future = RequestFuture.newFuture();
        Request request = new ApplicationAccessTokenRequest(
                ApplicationAccessTokenRequest.DEVICE_ID_DO_NOT_TRACK, future, future);
        addToRequestQueueWithTag(request, tag);
        return Observable.from(future, Schedulers.io());
    }

    @Override
    public Observable<AccessToken> getUserAccessToken(String code, Object tag) {
        RequestFuture<AccessToken> future = RequestFuture.newFuture();
        UserAccessTokenRequest request = new UserAccessTokenRequest(code, future, future);
        addToRequestQueueWithTag(request, tag);
        return Observable.from(future, Schedulers.io());
    }

    @Override
    public Observable<AccessToken> refreshAccessToken(String refreshToken, Object tag) {
        RequestFuture<AccessToken> future = RequestFuture.newFuture();
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken, future);
        addToRequestQueueWithTag(request, tag);
        return Observable.from(future, Schedulers.io())
                // Store the refreshed access token when received before emitting to subscribers
                .flatMap(new Func1<AccessToken, Observable<AccessToken>>() {
                    @Override
                    public Observable<AccessToken> call(AccessToken accessToken) {
                        mAccountManagerHelper.setAuthToken(accessToken.getAccessToken());
                        return Observable.just(accessToken);
                    }
                });
    }

    @Override
    public Observable<Me> getMe(@NonNull String accessToken, final Object tag) {
        final RequestFuture<Me> future = RequestFuture.newFuture();
        MeRequest request = new MeRequest(accessToken, future);
        addToRequestQueueWithTag(request, future);
        return Observable.from(future, Schedulers.io());
    }

    @Override
    public Observable<Listing<Link>> getLinkListing(@Nullable String accessToken, String path,
                                                    String after, Object tag) {
        final RequestFuture<Listing<Link>> future = RequestFuture.newFuture();
        ListingRequest<Link> request = new ListingRequest<>(path, after, accessToken, future);
        Timber.d("Adding to request queue: %s", request);
        addToRequestQueueWithTag(request, tag);
        return Observable.from(future, Schedulers.io());
    }

    private <T> Request<T> addToRequestQueueWithTag(Request<T> request, Object tag) {
        Timber.d("Adding request to queue: %s", request);
        request.setTag(tag);
        return mVolleySingleton.addToRequestQueue(request);
    }

    @Override
    public void cancelAll(Object tag) {
        mVolleySingleton.getRequestQueue().cancelAll(tag);
    }
}
