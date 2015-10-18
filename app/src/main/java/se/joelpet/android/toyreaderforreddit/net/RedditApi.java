package se.joelpet.android.toyreaderforreddit.net;

import rx.Observable;
import se.joelpet.android.toyreaderforreddit.domain.AccessToken;
import se.joelpet.android.toyreaderforreddit.domain.Link;
import se.joelpet.android.toyreaderforreddit.domain.Listing;
import se.joelpet.android.toyreaderforreddit.domain.Me;

public interface RedditApi {

    Observable<Listing<Link>> getLinkListing(String path, String after, Object tag);

    Observable<AccessToken> getAccessToken(String code, Object tag);

    Observable<Me> getMe(Object tag);

    void cancelAll(Object tag);
}
