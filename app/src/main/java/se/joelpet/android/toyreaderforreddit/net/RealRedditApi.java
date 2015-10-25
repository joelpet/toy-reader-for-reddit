package se.joelpet.android.toyreaderforreddit.net;

import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import se.joelpet.android.toyreaderforreddit.VolleySingleton;
import se.joelpet.android.toyreaderforreddit.accounts.AccountManagerHelper;
import se.joelpet.android.toyreaderforreddit.domain.AccessToken;
import se.joelpet.android.toyreaderforreddit.domain.Link;
import se.joelpet.android.toyreaderforreddit.domain.Listing;
import se.joelpet.android.toyreaderforreddit.domain.Me;
import se.joelpet.android.toyreaderforreddit.storage.LocalDataStore;
import se.joelpet.android.toyreaderforreddit.volley.AccessTokenRequest;
import se.joelpet.android.toyreaderforreddit.volley.ListingRequest;
import se.joelpet.android.toyreaderforreddit.volley.MeRequest;
import se.joelpet.android.toyreaderforreddit.volley.RefreshTokenRequest;
import timber.log.Timber;

public class RealRedditApi implements RedditApi {

    private final VolleySingleton mVolleySingleton;
    private final LocalDataStore mLocalDataStore;
    private final AccountManagerHelper mAccountManagerHelper;

    @Inject
    public RealRedditApi(VolleySingleton volleySingleton, LocalDataStore localDataStore,
                         AccountManagerHelper accountManagerHelper) {
        mVolleySingleton = volleySingleton;
        mLocalDataStore = localDataStore;
        mAccountManagerHelper = accountManagerHelper;
        Timber.d("Constructing new RealRedditApi with VolleySingleton: %s", mVolleySingleton);
    }

    @Override
    public Observable<Listing<Link>> getLinkListing(final String path, final String after, final
    Object tag) {
        final RequestFuture<Listing<Link>> future = RequestFuture.newFuture();
        mAccountManagerHelper
                .getAuthToken()
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ListingRequest<Link>>() {
                    @Override
                    public void call(ListingRequest<Link> request) {
                        addToRequestQueueWithTag(request, tag);
                    }
                });
        // TOOD: Take care of 40x responses -- invalidate token
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
    // Or with AccountManager it makes sense to have that as a helper for getting auth tokens.
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
