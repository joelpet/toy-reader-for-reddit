package se.joelpet.android.toyredditreader.net;

import com.android.volley.Request;

import rx.Observable;
import se.joelpet.android.toyredditreader.domain.AccessToken;
import se.joelpet.android.toyredditreader.domain.Link;
import se.joelpet.android.toyredditreader.domain.Listing;
import se.joelpet.android.toyredditreader.domain.Me;
import se.joelpet.android.toyredditreader.volley.ResponseListener;

public interface RedditApi {

    Observable<Listing<Link>> getLinkListing(String path, String after, Object tag);

    Request getAccessToken(String code, Object tag, ResponseListener<AccessToken> listener);

    Request getMe(Object tag, ResponseListener<Me> listener);

    void cancelAll(Object tag);
}
