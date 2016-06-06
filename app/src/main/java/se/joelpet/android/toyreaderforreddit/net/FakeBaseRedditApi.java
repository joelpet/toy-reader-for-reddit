package se.joelpet.android.toyreaderforreddit.net;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import rx.Observable;
import se.joelpet.android.toyreaderforreddit.model.AccessToken;
import se.joelpet.android.toyreaderforreddit.model.Link;
import se.joelpet.android.toyreaderforreddit.model.Listing;
import se.joelpet.android.toyreaderforreddit.model.Me;

public class FakeBaseRedditApi implements BaseRedditApi {

    @Override
    public Observable<AccessToken> getApplicationAccessToken(Object tag) {
        return null;
    }

    @Override
    public Observable<AccessToken> getUserAccessToken(String code, Object tag) {
        return Observable.error(new Exception("Not yet implemented"));
    }

    @Override
    public Observable<AccessToken> refreshAccessToken(String refreshToken, Object tag) {
        return Observable.error(new Exception("Not yet implemented"));
    }

    @Override
    public Observable<Me> getMe(@NonNull String accessToken, Object tag) {
        return Observable.error(new Exception("Not yet implemented"));
    }

    @Override
    public Observable<Listing<Link>> getLinkListing(@Nullable String accessToken, String path,
                                                    String after, Object tag) {
        Listing<Link> linkListing = new Listing<>();
        linkListing.setChildren(new ArrayList<Link>());
        linkListing.setAfter("t3_2vfezo");

        Link link = new Link();
        link.setDomain("imgur.com");
        link.setSubreddit("AdviceAnimals");
        link.setAuthor("VITW");
        link.setScore(4844);
        link.setTitle("That box you checked on your drivers license...");
        link.setCreatedUtc(1423588391.D);
        link.setNumComments(1080);
        link.setUrl("http://imgur.com/tqQCQeD");
        link.setThumbnail(
                "http://b.thumbs.redditmedia.com/qM5POrHY99MdHav-dv0hCA_Sok_gYFd9Fi3-FbiMZ5E.jpg");
        link.setOver18(true);

        for (int i = 0; i < 10; i++) {
            linkListing.getChildren().add(link);
        }

        return Observable.just(linkListing);
    }

    @Override
    public void cancelAll(Object tag) {
    }
}
