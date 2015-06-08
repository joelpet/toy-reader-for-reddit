package se.joelpet.android.toyredditreader.net;

import rx.Observable;
import se.joelpet.android.toyredditreader.domain.Link;
import se.joelpet.android.toyredditreader.domain.Listing;

public interface RedditApi {

    Observable<Listing<Link>> getLinkListing(String path, String after, Object tag);

    void cancelAll(Object tag);
}
