package se.joelpet.android.toyreaderforreddit.ui.linklisting;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import se.joelpet.android.toyreaderforreddit.accounts.AccountManagerHelper;
import se.joelpet.android.toyreaderforreddit.accounts.AddAccountResult;
import se.joelpet.android.toyreaderforreddit.model.Link;
import se.joelpet.android.toyreaderforreddit.model.Listing;
import se.joelpet.android.toyreaderforreddit.net.OAuthRedditApi;
import se.joelpet.android.toyreaderforreddit.util.LinkUtils;
import se.joelpet.android.toyreaderforreddit.util.TextUtils;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

public class LinkListingPresenter implements LinkListingContract.Presenter {

    private final LinkListingContract.View linkListingView;
    private final OAuthRedditApi oAuthRedditApi;
    private final AccountManagerHelper accountManagerHelper;
    private final CompositeSubscription compositeSubscription;

    /** The "after" portion received in the response to the last made request. */
    private String after;

    /** The path part of the URI pointing to the link listing of this fragment. */
    private String listingPath = "r/all/";

    /** Flag indicating that a Listing request is in progress. */
    private boolean requestInProgress;

    public LinkListingPresenter(@NonNull LinkListingContract.View linkListingView,
                                @NonNull OAuthRedditApi oAuthRedditApi,
                                @NonNull AccountManagerHelper accountManagerHelper) {
        this.linkListingView = checkNotNull(linkListingView);
        this.oAuthRedditApi = checkNotNull(oAuthRedditApi);
        this.accountManagerHelper = checkNotNull(accountManagerHelper);
        this.compositeSubscription = new CompositeSubscription();

        this.linkListingView.setPresenter(this);
    }

    @Override
    public void start() {
        loadLinks();
    }

    @Override
    public void loadLinks() {
        if (requestInProgress) return;

        compositeSubscription.add(oAuthRedditApi
                .getLinkListing(listingPath, after, this)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        requestInProgress = true;
                    }
                })
                .subscribe(new Action1<Listing<Link>>() {
                    @Override
                    public void call(Listing<Link> linkListing) {
                        handleReceivedListing(linkListing);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        requestInProgress = false;
                        handleListingRequestError(throwable);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        requestInProgress = false;
                    }
                }));
    }

    @Override
    public void reloadLinks() {
        after = null;
        loadLinks();
    }

    /** Callback for successful Subreddit Listing GET request. */
    private void handleReceivedListing(Listing<Link> listing) {
        Timber.d("%s###handleReceivedListing(%s)", this, listing.getModhash());

        if (TextUtils.isEmpty(after)) {
            linkListingView.showLinks(listing.getChildren());
        } else {
            linkListingView.showMoreLinks(listing.getChildren());
        }

        after = listing.getAfter();

        linkListingView.setRefreshingIndicator(false);
        linkListingView.setLoadingIndicator(false);

        Timber.d("Fetched %d items after={%s}.", listing.getChildren().size(), after);
    }

    private void handleListingRequestError(Throwable throwable) {
        Timber.e(throwable, "Listing request failed");

        if (throwable.getCause() instanceof AuthFailureError) {
            linkListingView.showAuthFailureError();
        } else {
            linkListingView.showLoadFailureError();
        }

        linkListingView.setRefreshingIndicator(false);
        linkListingView.setLoadingIndicator(false);
    }

    @Override
    public void openLink(@NonNull Link link) {
        linkListingView.showWebUi(LinkUtils.getLinkUri(link));
    }

    @Override
    public void openLinkComments(@NonNull Link link) {
        linkListingView.showWebUi(LinkUtils.getCommentsUri(link));
    }

    @Override
    public void renewCredentials(Activity activity) {
        compositeSubscription.add(accountManagerHelper
                .addAccount(activity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<AddAccountResult>() {
                    @Override
                    public void call(AddAccountResult result) {
                        Timber.d("Account added; queueing new listing request.");
                        loadLinks();
                    }
                }));
    }
}
