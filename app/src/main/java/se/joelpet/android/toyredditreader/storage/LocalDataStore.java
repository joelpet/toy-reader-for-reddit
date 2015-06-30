package se.joelpet.android.toyredditreader.storage;

import rx.Observable;
import se.joelpet.android.toyredditreader.domain.AccessToken;
import se.joelpet.android.toyredditreader.domain.Me;

public interface LocalDataStore {

    /** Returns a hot observable source of Me values. */
    Observable<Me> observeMe();

    /** Stores the given Me object. */
    void putMe(Me me);

    /** Stores the given auth code. */
    void putAuthCode(String authCode);

    /** Returns data stream emitting current access token once, if exists, before completion. */
    Observable<AccessToken> getAccessToken();

    /** Stores the given access token. */
    void putAccessToken(AccessToken accessToken);
}
