package se.joelpet.android.toyreaderforreddit.storage;

import rx.Observable;
import se.joelpet.android.toyreaderforreddit.domain.AccessToken;
import se.joelpet.android.toyreaderforreddit.domain.Me;

public interface LocalDataStore {

    /** Returns a hot observable source of Me values. */
    Observable<Me> observeMe();

    /** Stores the given Me object. */
    Observable<Me> putMe(Me me);

    /** Deletes the current Me object, if exists, before completion. */
    Observable<Void> deleteMe();

    /** Stores the given auth code. */
    Observable<String> putAuthCode(String authCode);

    /** Returns data stream emitting current access token once, if exists, before completion. */
    Observable<AccessToken> getAccessToken();

    /** Stores the given access token. */
    Observable<AccessToken> putAccessToken(AccessToken accessToken);

    /** Deletes the current access token, if exists, before completion. */
    Observable<Void> deleteAccessToken();
}
