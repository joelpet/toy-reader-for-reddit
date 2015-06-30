package se.joelpet.android.toyredditreader.net;

import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import se.joelpet.android.toyredditreader.VolleySingleton;
import se.joelpet.android.toyredditreader.domain.AccessToken;
import se.joelpet.android.toyredditreader.domain.Link;
import se.joelpet.android.toyredditreader.domain.Listing;
import se.joelpet.android.toyredditreader.domain.Me;
import se.joelpet.android.toyredditreader.storage.LocalDataStore;
import se.joelpet.android.toyredditreader.volley.AccessTokenRequest;
import se.joelpet.android.toyredditreader.volley.ListingRequest;
import se.joelpet.android.toyredditreader.volley.MeRequest;
import se.joelpet.android.toyredditreader.volley.RefreshTokenRequest;
import timber.log.Timber;

public class RealRedditApi implements RedditApi {


    private final VolleySingleton mVolleySingleton;

    private final LocalDataStore mLocalDataStore;

    @Inject
    public RealRedditApi(VolleySingleton volleySingleton, LocalDataStore localDataStore) {
        mVolleySingleton = volleySingleton;
        mLocalDataStore = localDataStore;
        Timber.d("Constructing new RealRedditApi with VolleySingleton: %s", mVolleySingleton);
    }

    @Override
    public Observable<Listing<Link>> getLinkListing(final String path, final String after, final Object tag) {
        final RequestFuture<Listing<Link>> future = RequestFuture.newFuture();
        mLocalDataStore.getAccessToken().singleOrDefault(null)
                .flatMap(new Func1<AccessToken, Observable<AccessToken>>() {
                    @Override
                    public Observable<AccessToken> call(AccessToken accessToken) {
                        if (accessToken == null) {
                            // We are not going to attempt an authenticated request -- carry on
                            return null;
                        } else if (accessToken.isExpired()) {
                            return refreshAccessToken(accessToken, tag);
                        } else {
                            return Observable.just(accessToken);
                        }
                    }
                })
                .map(new Func1<AccessToken, String>() {
                    @Override
                    public String call(AccessToken accessToken) {
                        // We now either have null or a valid access token
                        return accessToken != null ? accessToken.getAccessToken() : null;
                    }
                })
                .map(new Func1<String, ListingRequest<Link>>() {
                    @Override
                    public ListingRequest<Link> call(String accessToken) {
                        return new ListingRequest<>(path, after, accessToken, future);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ListingRequest<Link>>() {
                    @Override
                    public void call(ListingRequest<Link> request) {
                        addToRequestQueueWithTag(request, tag);
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

    public Observable<AccessToken> refreshAccessToken(AccessToken accessToken, Object tag) {
        RequestFuture<AccessToken> future = RequestFuture.newFuture();
        RefreshTokenRequest request = new RefreshTokenRequest(accessToken, future);
        addToRequestQueueWithTag(request, tag);
        return Observable.from(future, Schedulers.io())
                // Store the refreshed access token when received before emitting to subscribers
                .flatMap(new Func1<AccessToken, Observable<AccessToken>>() {
                    @Override
                    public Observable<AccessToken> call(AccessToken accessToken) {
                        return mLocalDataStore.putAccessToken(accessToken);
                    }
                });
    }

    // TODO: getMe() should probably take accessToken as parameter
    // And so should every API method so that this API class does not have to know where to get an
    // access token from. That would also loosen the coupling between this API and LocalDataStore.
    @Override
    public Observable<Me> getMe(final Object tag) {
        final RequestFuture<Me> future = RequestFuture.newFuture();
        mLocalDataStore.getAccessToken().singleOrDefault(null)
                .map(new Func1<AccessToken, String>() {
                    @Override
                    public String call(AccessToken accessToken) {
                        if (accessToken == null) {
                            throw new RuntimeException("An access token is required");
                        }
                        return accessToken.getAccessToken();
                    }
                })
                .map(new Func1<String, MeRequest>() {
                    @Override
                    public MeRequest call(String accessToken) {
                        return new MeRequest(accessToken, future);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MeRequest>() {
                    @Override
                    public void call(MeRequest request) {
                        future.setRequest(addToRequestQueueWithTag(request, tag));
                    }
                });
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
