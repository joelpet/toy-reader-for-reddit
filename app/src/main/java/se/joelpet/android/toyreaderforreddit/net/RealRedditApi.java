package se.joelpet.android.toyreaderforreddit.net;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import se.joelpet.android.toyreaderforreddit.VolleySingleton;
import se.joelpet.android.toyreaderforreddit.accounts.AccountManagerHelper;
import se.joelpet.android.toyreaderforreddit.domain.AccessToken;
import se.joelpet.android.toyreaderforreddit.domain.Link;
import se.joelpet.android.toyreaderforreddit.domain.Listing;
import se.joelpet.android.toyreaderforreddit.domain.Me;
import se.joelpet.android.toyreaderforreddit.volley.AccessTokenRequest;
import se.joelpet.android.toyreaderforreddit.volley.ListingRequest;
import se.joelpet.android.toyreaderforreddit.volley.MeRequest;
import se.joelpet.android.toyreaderforreddit.volley.RefreshTokenRequest;
import timber.log.Timber;

public class RealRedditApi implements RedditApi {

    private final VolleySingleton mVolleySingleton;
    private final AccountManagerHelper mAccountManagerHelper;

    @Inject
    public RealRedditApi(VolleySingleton volleySingleton,
                         AccountManagerHelper accountManagerHelper) {
        mVolleySingleton = volleySingleton;
        mAccountManagerHelper = accountManagerHelper;
        Timber.d("Constructing new RealRedditApi with VolleySingleton: %s", mVolleySingleton);
    }

    @Override
    public Observable<Listing<Link>> getLinkListing(final String path, final String after,
                                                    final Object tag) {
        return getLinkListingUsingStoredAuthToken(path, after, tag)
                .onErrorResumeNext(new Func1<Throwable, Observable<Listing<Link>>>() {
                    @Override
                    public Observable<Listing<Link>> call(Throwable throwable) {
                        if (throwable.getCause() instanceof AuthFailureError) {
                            Timber.d("First auth failure; invalidate auth token and retry");
                            return mAccountManagerHelper.invalidateAuthToken()
                                    .concatMap(new Func1<Void, Observable<Listing<Link>>>() {
                                        @Override
                                        public Observable<Listing<Link>> call(Void aVoid) {
                                            return getLinkListingUsingStoredAuthToken(path,
                                                    after, tag);
                                        }
                                    });
                        }
                        return Observable.error(throwable);
                    }
                });
    }

    public Observable<Listing<Link>> getLinkListingUsingStoredAuthToken(final String path,
                                                                        final String after,
                                                                        final Object tag) {
        final RequestFuture<Listing<Link>> future = RequestFuture.newFuture();
        mAccountManagerHelper
                .getAuthToken()
                .single()
                .onErrorReturn(new Func1<Throwable, String>() {
                    @Override
                    public String call(Throwable throwable) {
                        return null;
                    }
                })
                .map(new Func1<String, ListingRequest<Link>>() {
                    @Override
                    public ListingRequest<Link> call(String accessToken) {
                        return new ListingRequest<>(path, after, accessToken, future);
                    }
                })
                .subscribe(new Action1<ListingRequest<Link>>() {
                    @Override
                    public void call(ListingRequest<Link> linkListingRequest) {
                        Timber.d("Adding to request queue: %s", linkListingRequest);
                        addToRequestQueueWithTag(linkListingRequest, tag);
                    }
                });
        return Observable.from(future, Schedulers.io());
    }

    @Override
    public Observable<AccessToken> getAccessToken(String code, Object tag) {
        RequestFuture<AccessToken> future = RequestFuture.newFuture();
        AccessTokenRequest request = new AccessTokenRequest(code, future, future);
        addToRequestQueueWithTag(request, tag);
        return Observable.from(future, Schedulers.io());
    }

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
