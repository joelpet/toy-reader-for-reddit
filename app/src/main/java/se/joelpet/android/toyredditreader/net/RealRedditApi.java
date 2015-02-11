package se.joelpet.android.toyredditreader.net;

import android.net.Uri;
import android.text.TextUtils;

import javax.inject.Inject;

import se.joelpet.android.toyredditreader.VolleySingleton;
import se.joelpet.android.toyredditreader.domain.Link;
import se.joelpet.android.toyredditreader.domain.Listing;
import se.joelpet.android.toyredditreader.gson.ListingRequest;
import timber.log.Timber;

import static com.android.volley.Response.ErrorListener;
import static com.android.volley.Response.Listener;

public class RealRedditApi implements RedditApi {

    private static final Uri BASE_URI = Uri.parse("https://www.reddit.com/");

    private VolleySingleton mVolleySingleton;

    @Inject
    public RealRedditApi(VolleySingleton volleySingleton) {
        mVolleySingleton = volleySingleton;
        Timber.i("Constructing new RealRedditApi with VolleySingleton: %s", mVolleySingleton);
    }

    @Override
    public ListingRequest<Link> getLinkListing(String path, String after,
            Listener<Listing<Link>> listener, ErrorListener errorListener, Object tag) {
        Uri.Builder uriBuilder = BASE_URI.buildUpon().appendEncodedPath(path + ".json");

        if (!TextUtils.isEmpty(after)) {
            uriBuilder.appendQueryParameter("after", after);
        }

        ListingRequest<Link> request = new ListingRequest<>(uriBuilder.toString(), null, listener,
                errorListener);
        request.setTag(tag);

        mVolleySingleton.addToRequestQueue(request);
        Timber.d("Added listing request to queue: %s", request);

        return request;
    }

    @Override
    public void cancelAll(Object tag) {
        mVolleySingleton.getRequestQueue().cancelAll(tag);
    }
}
