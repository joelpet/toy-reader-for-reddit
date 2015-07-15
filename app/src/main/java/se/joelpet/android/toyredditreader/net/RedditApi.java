package se.joelpet.android.toyredditreader.net;

import rx.Observable;
import se.joelpet.android.toyredditreader.domain.AccessToken;
import se.joelpet.android.toyredditreader.domain.Link;
import se.joelpet.android.toyredditreader.domain.Listing;
import se.joelpet.android.toyredditreader.domain.Me;

public interface RedditApi {

    Observable<Listing<Link>> getLinkListing(String path, String after, Object tag);

    Observable<AccessToken> getAccessToken(String code, Object tag);

    Observable<Me> getMe(Object tag);

    void cancelAll(Object tag);
}
