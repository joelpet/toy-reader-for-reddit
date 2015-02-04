package se.joelpet.android.toyredditreader.net;

import se.joelpet.android.toyredditreader.domain.Listing;
import se.joelpet.android.toyredditreader.domain.Thing;
import se.joelpet.android.toyredditreader.gson.ListingRequest;

import static com.android.volley.Response.ErrorListener;
import static com.android.volley.Response.Listener;

public interface RedditApi {

    <T extends Thing> ListingRequest<T> getListing(String after, Listener<Listing<T>> listener,
            ErrorListener errorListener);

    void cancelAll(String tag);
}
