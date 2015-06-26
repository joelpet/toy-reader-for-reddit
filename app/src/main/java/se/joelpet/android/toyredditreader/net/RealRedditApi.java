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
import timber.log.Timber;

public class RealRedditApi implements RedditApi {

    /** Base URI for unauthorized requests. */
    private static final Uri BASE_URI = Uri.parse("https://www.reddit.com/");
    /** Base URI for authorized requests. */
    private static final Uri BASE_OAUTH_URI = Uri.parse("https://oauth.reddit.com/");

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
        // TODO: Replace with getBaseOauthUri()
        String accessToken = mPreferences.getAccessToken();
        Uri baseUri = accessToken != null ? BASE_OAUTH_URI : BASE_URI;
        Uri.Builder uriBuilder = baseUri.buildUpon().appendEncodedPath(path + ".json");

        if (!TextUtils.isEmpty(after)) {
            uriBuilder.appendQueryParameter("after", after);
        }

        RequestFuture<Listing<Link>> future = RequestFuture.newFuture();
        ListingRequest<Link> request = new ListingRequest<>(uriBuilder.toString(), future, future);

        if (accessToken != null) {
            request.setAccessToken(accessToken);
        }

        addToRequestQueueWithTag(request, tag);
        return Observable.from(future, Schedulers.io());
    }

    @Override
    public Observable<AccessToken> getAccessToken(String code, Object tag) {
        RequestFuture<AccessToken> future = RequestFuture.newFuture();
        AccessTokenRequest request = new AccessTokenRequest(code, future, future);
        addToRequestQueueWithTag(request, tag);
        return Observable.from(future, Schedulers.io());
    }

    @Override
    public Observable<Me> getMe(Object tag) {
        String accessToken = mPreferences.getAccessToken();
        RequestFuture<Me> future = RequestFuture.newFuture();
        MeRequest request = new MeRequest(accessToken, future, future);
        addToRequestQueueWithTag(request, tag);
        return Observable.from(future, Schedulers.io());
    }

    private void addToRequestQueueWithTag(Request<?> request, Object tag) {
        request.setTag(tag);
        mVolleySingleton.addToRequestQueue(request);
        Timber.d("Added request to queue: %s", request);
    }

    @Override
    public void cancelAll(Object tag) {
        mVolleySingleton.getRequestQueue().cancelAll(tag);
    }
}
