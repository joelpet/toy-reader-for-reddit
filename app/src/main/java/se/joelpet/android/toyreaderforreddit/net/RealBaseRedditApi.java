package se.joelpet.android.toyreaderforreddit.net;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;

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
import se.joelpet.android.toyreaderforreddit.net.ratelimit.RateLimitExceededError;
import se.joelpet.android.toyreaderforreddit.volley.ApplicationAccessTokenRequest;
import se.joelpet.android.toyreaderforreddit.volley.ListingRequest;
import se.joelpet.android.toyreaderforreddit.volley.MeRequest;
import se.joelpet.android.toyreaderforreddit.volley.RefreshTokenRequest;
import se.joelpet.android.toyreaderforreddit.volley.UserAccessTokenRequest;

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
        RequestFuture<AccessToken> future = newFuture();
        Request<AccessToken> request = new ApplicationAccessTokenRequest(
                ApplicationAccessTokenRequest.DEVICE_ID_DO_NOT_TRACK, future);
        return enqueueRequestWithTag(future, request, tag);
    }

    @Override
    public Observable<AccessToken> getUserAccessToken(String code, Object tag) {
        RequestFuture<AccessToken> future = RequestFuture.newFuture();
        UserAccessTokenRequest request = new UserAccessTokenRequest(code, future);
        return enqueueRequestWithTag(future, request, tag);
    }

    @Override
    public Observable<AccessToken> refreshAccessToken(String refreshToken, Object tag) {
        RequestFuture<AccessToken> future = newFuture();
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken, future);
        return enqueueRequestWithTag(future, request, tag)
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
        RequestFuture<Me> future = newFuture();
        MeRequest request = new MeRequest(accessToken, future);
        return enqueueRequestWithTag(future, request, tag);
    }

    @Override
    public Observable<Listing<Link>> getLinkListing(@Nullable String accessToken, String path,
                                                    String after, Object tag) {
        RequestFuture<Listing<Link>> future = newFuture();
        ListingRequest<Link> request = new ListingRequest<>(path, after, accessToken, future);
        return enqueueRequestWithTag(future, request, tag);
    }

    public static <T> RequestFuture<T> newFuture() {
        return RequestFuture.newFuture();
    }

    private <T> Observable<T> enqueueRequestWithTag(RequestFuture<T> future, Request<T> request,
                                                    Object tag) {
        try {
            mVolleySingleton.addToRequestQueue(request.setTag(tag));
        } catch (RateLimitExceededError e) {
            return Observable.error(e);
        }
        return Observable.from(future, Schedulers.io());
    }

    @Override
    public void cancelAll(Object tag) {
        mVolleySingleton.getRequestQueue().cancelAll(tag);
    }
}
