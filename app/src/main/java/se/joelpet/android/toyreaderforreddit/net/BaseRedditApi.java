package se.joelpet.android.toyreaderforreddit.net;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Observable;
import se.joelpet.android.toyreaderforreddit.domain.AccessToken;
import se.joelpet.android.toyreaderforreddit.domain.Link;
import se.joelpet.android.toyreaderforreddit.domain.Listing;
import se.joelpet.android.toyreaderforreddit.domain.Me;

public interface BaseRedditApi {

    Observable<AccessToken> getApplicationAccessToken(Object tag);

    Observable<AccessToken> getUserAccessToken(String code, Object tag);

    Observable<AccessToken> refreshAccessToken(String refreshToken, Object tag);

    Observable<Me> getMe(@NonNull String accessToken, Object tag);

    Observable<Listing<Link>> getLinkListing(@Nullable String accessToken, String path,
                                             String after, Object tag);

    void cancelAll(Object tag);
}
