package se.joelpet.android.toyredditreader.net;

import com.android.volley.Response;

import java.util.ArrayList;

import se.joelpet.android.toyredditreader.domain.Link;
import se.joelpet.android.toyredditreader.domain.Listing;
import se.joelpet.android.toyredditreader.gson.ListingRequest;

public class FakeRedditApi implements RedditApi {

    @Override
    public ListingRequest<Link> getLinkListing(String path, String after,
            Response.Listener<Listing<Link>> listener, Response.ErrorListener errorListener,
            Object tag) {

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

        listener.onResponse(linkListing);

        return new ListingRequest<>(null, null, null, null);
    }

    @Override
    public void cancelAll(Object tag) {
    }
}
