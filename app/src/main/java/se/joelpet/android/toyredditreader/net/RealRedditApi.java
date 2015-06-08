package se.joelpet.android.toyredditreader.net;

import android.net.Uri;
import android.text.TextUtils;

import com.android.volley.toolbox.RequestFuture;

import javax.inject.Inject;

import rx.Observable;
import rx.schedulers.Schedulers;
import se.joelpet.android.toyredditreader.VolleySingleton;
import se.joelpet.android.toyredditreader.domain.Link;
import se.joelpet.android.toyredditreader.domain.Listing;
import se.joelpet.android.toyredditreader.gson.ListingRequest;
import timber.log.Timber;

public class RealRedditApi implements RedditApi {

    private static final Uri BASE_URI = Uri.parse("https://www.reddit.com/");

    private VolleySingleton mVolleySingleton;

    @Inject
    public RealRedditApi(VolleySingleton volleySingleton) {
        mVolleySingleton = volleySingleton;
        Timber.i("Constructing new RealRedditApi with VolleySingleton: %s", mVolleySingleton);
    }

    @Override
    public Observable<Listing<Link>> getLinkListing(String path, String after, Object tag) {
        Uri.Builder uriBuilder = BASE_URI.buildUpon().appendEncodedPath(path + ".json");

        if (!TextUtils.isEmpty(after)) {
            uriBuilder.appendQueryParameter("after", after);
        }

        RequestFuture<Listing<Link>> future = RequestFuture.newFuture();
        ListingRequest<Link> request = new ListingRequest<>(uriBuilder.toString(), null, future,
                future);
        request.setTag(tag);

        mVolleySingleton.addToRequestQueue(request);
        Timber.d("Added listing request to queue: %s", request);

        return Observable.from(future, Schedulers.io());
    }

    @Override
    public void cancelAll(Object tag) {
        mVolleySingleton.getRequestQueue().cancelAll(tag);
    }
}
