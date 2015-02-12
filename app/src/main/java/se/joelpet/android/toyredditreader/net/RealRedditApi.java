package se.joelpet.android.toyredditreader.net;

import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;

import android.net.Uri;
import android.text.TextUtils;

import javax.inject.Inject;

import rx.Observable;
import rx.schedulers.Schedulers;
import se.joelpet.android.toyredditreader.Preferences;
import se.joelpet.android.toyredditreader.VolleySingleton;
import se.joelpet.android.toyredditreader.domain.AccessToken;
import se.joelpet.android.toyredditreader.domain.Link;
import se.joelpet.android.toyredditreader.domain.Listing;
import se.joelpet.android.toyredditreader.domain.Me;
import se.joelpet.android.toyredditreader.volley.AccessTokenRequest;
import se.joelpet.android.toyredditreader.volley.ListingRequest;
import se.joelpet.android.toyredditreader.volley.MeRequest;
import se.joelpet.android.toyredditreader.volley.ResponseListener;
import timber.log.Timber;

public class RealRedditApi implements RedditApi {

    private static final Uri BASE_URI = Uri.parse("https://www.reddit.com/");

    private final VolleySingleton mVolleySingleton;

    private final Preferences mPreferences;

    @Inject
    public RealRedditApi(VolleySingleton volleySingleton, Preferences preferences) {
        mVolleySingleton = volleySingleton;
        mPreferences = preferences;
        Timber.d("Constructing new RealRedditApi with VolleySingleton: %s", mVolleySingleton);
    }

    @Override
    public Observable<Listing<Link>> getLinkListing(String path, String after, Object tag) {
        Uri.Builder uriBuilder = BASE_URI.buildUpon().appendEncodedPath(path + ".json");

        if (!TextUtils.isEmpty(after)) {
            uriBuilder.appendQueryParameter("after", after);
        }

        RequestFuture<Listing<Link>> future = RequestFuture.newFuture();
        ListingRequest<Link> request = new ListingRequest<>(uriBuilder.toString(), null, future,
                future);
        request.setTag(tag);

        return Observable.from(future, Schedulers.io());
    }
    
    @Override
    public Request getAccessToken(String code, Object tag, ResponseListener<AccessToken> listener) {
        AccessTokenRequest request = new AccessTokenRequest(code, listener, listener);
        return addToRequestQueueWithTag(request, tag);
    }

    @Override
    public Request getMe(Object tag, ResponseListener<Me> listener) {
        String accessToken = mPreferences.getAccessToken();
        MeRequest request = new MeRequest(accessToken, listener, listener);
        return addToRequestQueueWithTag(request, tag);
    }

    private Request<?> addToRequestQueueWithTag(Request<?> request, Object tag) {
        request.setTag(tag);
        mVolleySingleton.addToRequestQueue(request);
        Timber.d("Added request to queue: %s", request);
        return request;
    }
    
    @Override
    public void cancelAll(Object tag) {
        mVolleySingleton.getRequestQueue().cancelAll(tag);
    }
}
