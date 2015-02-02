package se.joelpet.android.toyredditreader.net;

import android.content.Context;
import android.text.TextUtils;

import javax.inject.Inject;

import se.joelpet.android.toyredditreader.VolleySingleton;
import se.joelpet.android.toyredditreader.dagger.ForApplication;
import se.joelpet.android.toyredditreader.domain.SubredditListingWrapper;
import se.joelpet.android.toyredditreader.fragments.SubredditListingFragment;
import se.joelpet.android.toyredditreader.gson.ListingRequest;
import timber.log.Timber;

import static com.android.volley.Response.ErrorListener;
import static com.android.volley.Response.Listener;

public class RealRedditApi implements RedditApi {

    @Inject
    @ForApplication
    Context mApplicationContext;

    // TODO: @Inject
    VolleySingleton mVolleySingleton;

    @Override
    public ListingRequest<SubredditListingWrapper> getSubredditListing(String after,
            Listener<SubredditListingWrapper> listener, ErrorListener errorListener) {
        String url = "http://www.reddit.com/hot.json";

        if (!TextUtils.isEmpty(after)) {
            url += "?after=" + after;
        }

        ListingRequest<SubredditListingWrapper> request = new ListingRequest<>(url,
                SubredditListingWrapper.class, null, listener, errorListener);
        request.setTag(SubredditListingFragment.class.getName());

        VolleySingleton.getInstance(mApplicationContext).addToRequestQueue(request);
        Timber.d("Added listing request to queue: ", request);

        return request;
    }
}
