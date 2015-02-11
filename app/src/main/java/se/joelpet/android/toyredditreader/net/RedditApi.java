package se.joelpet.android.toyredditreader.net;

import se.joelpet.android.toyredditreader.domain.Link;
import se.joelpet.android.toyredditreader.domain.Listing;
import se.joelpet.android.toyredditreader.gson.ListingRequest;

import static com.android.volley.Response.ErrorListener;
import static com.android.volley.Response.Listener;

public interface RedditApi {

    ListingRequest<Link> getLinkListing(String path, String after,
            Listener<Listing<Link>> listener, ErrorListener errorListener, Object tag);

    void cancelAll(Object tag);
}
