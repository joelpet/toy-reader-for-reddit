package se.joelpet.android.toyredditreader.net;

import se.joelpet.android.toyredditreader.domain.SubredditListingWrapper;
import se.joelpet.android.toyredditreader.gson.ListingRequest;

import static com.android.volley.Response.ErrorListener;
import static com.android.volley.Response.Listener;

public interface RedditApi {

    ListingRequest<SubredditListingWrapper> getSubredditListing(String after,
            Listener<SubredditListingWrapper> listener, ErrorListener errorListener);

    void cancelAll(String tag);
}
