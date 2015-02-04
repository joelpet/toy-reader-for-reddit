package se.joelpet.android.toyredditreader.net;

import android.text.TextUtils;

import javax.inject.Inject;

import se.joelpet.android.toyredditreader.VolleySingleton;
import se.joelpet.android.toyredditreader.domain.Listing;
import se.joelpet.android.toyredditreader.domain.Thing;
import se.joelpet.android.toyredditreader.gson.ListingRequest;
import timber.log.Timber;

import static com.android.volley.Response.ErrorListener;
import static com.android.volley.Response.Listener;

public class RealRedditApi implements RedditApi {

    private VolleySingleton mVolleySingleton;

    @Inject
    public RealRedditApi(VolleySingleton volleySingleton) {
        mVolleySingleton = volleySingleton;
    }

    @Override
    public <T extends Thing> ListingRequest<T> getListing(String after,
            Listener<Listing<T>> listener, ErrorListener errorListener) {
        String url = "http://www.reddit.com/hot.json";

        if (!TextUtils.isEmpty(after)) {
            url += "?after=" + after;
        }

        ListingRequest<T> request = new ListingRequest<>(url, null, listener, errorListener);

        mVolleySingleton.addToRequestQueue(request);
        Timber.d("Added listing request to queue: ", request);

        return request;
    }

    @Override
    public void cancelAll(String tag) {
        mVolleySingleton.getRequestQueue().cancelAll(tag);
    }
}
