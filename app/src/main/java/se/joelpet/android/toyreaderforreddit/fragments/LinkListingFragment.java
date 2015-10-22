package se.joelpet.android.toyreaderforreddit.fragments;

import com.android.volley.toolbox.ImageLoader;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.functions.Action0;
import rx.functions.Action1;
import se.joelpet.android.toyreaderforreddit.R;
import se.joelpet.android.toyreaderforreddit.activities.WebActivity;
import se.joelpet.android.toyreaderforreddit.adapters.LinkListingRecyclerViewAdapter;
import se.joelpet.android.toyreaderforreddit.domain.Link;
import se.joelpet.android.toyreaderforreddit.domain.Listing;
import se.joelpet.android.toyreaderforreddit.net.RedditApi;
import timber.log.Timber;

public class LinkListingFragment extends BaseFragment implements SwipeRefreshLayout
        .OnRefreshListener, LinkListingRecyclerViewAdapter.ClickListener {

    public static final String TAG = LinkListingFragment.class.getName();

    public static final String ARGUMENT_LISTING = "argument_listing";
    public static final String ARG_LISTING_EVERYTHING = "r/all/";
    public static final String ARG_LISTING_SUBSCRIBED = "/";

    public static final String ARGUMENT_SORT = "argument_sort";
    public static final String ARG_SORT_HOT = "hot";
    public static final String ARG_SORT_NEW = "new";

    public static final int VIEW_SWITCHER_CHILD_LOAD_INDICATOR = 0;
    public static final int VIEW_SWITCHER_CHILD_RECYCLER_VIEW = 1;

    public static final String STATE_STRING_AFTER = "mAfter";

    @InjectView(R.id.root_view_switcher)
    protected ViewSwitcher mRootViewSwitcher;

    @InjectView(R.id.my_swipe_refresh_layout)
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @InjectView(R.id.my_recycler_view)
    protected RecyclerView mRecyclerView;

    @Inject
    protected RedditApi mRedditApi;

    @Inject
    protected ImageLoader mImageLoader;

    /** The path part of the URI pointing to the link listing of this fragment. */
    private String mListingPath;

    /** The "after" portion received in the response to the last made request. */
    private String mAfter;

    /** Flag indicating that a Listing request is in progress. */
    private boolean mRequestInProgress;

    /** Subscription to the current Listing request. */
    private Subscription mSubscription;

    private LinearLayoutManager mLinearLayoutManager;
    private LinkListingRecyclerViewAdapter mLinkListingRecyclerViewAdapter;

    public static LinkListingFragment newInstance(String listing, String sort) {
        LinkListingFragment fragment = new LinkListingFragment();
        Bundle arguments = new Bundle();
        fragment.setArguments(arguments);
        arguments.putString(ARGUMENT_LISTING, listing);
        arguments.putString(ARGUMENT_SORT, sort);
        return fragment;
    }

    public LinkListingFragment() {
    }

    //region Fragment lifecycle

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_STRING_AFTER, mAfter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            String listing = getArguments().getString(ARGUMENT_LISTING);
            String sort = getArguments().getString(ARGUMENT_SORT);
            mListingPath = listing + sort;
        }

        if (savedInstanceState != null) {
            if (mLinkListingRecyclerViewAdapter != null) {
                // Only restore the 'after' fragment if link listing data is still present
                mAfter = savedInstanceState.getString("mAfter");
                Timber.d("Restored mAfter state to '%s'", mAfter);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subreddit, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setOnScrollListener(new OnScrollListener());
    }

    @Override
    public void onStart() {
        super.onStart();
        queueListingRequest();
    }

    @Override
    public void onStop() {
        super.onStop();
        mRedditApi.cancelAll(TAG);
        mRequestInProgress = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    //endregion

    @Override
    public void onRefresh() {
        if (mRequestInProgress) {
            return;
        }
        mAfter = null;
        queueListingRequest();
    }

    private void queueListingRequest() {
        mRequestInProgress = true;
        mSubscription = AndroidObservable
                .bindFragment(this, mRedditApi.getLinkListing(mListingPath, mAfter, TAG))
                .subscribe(new Action1<Listing<Link>>() {
                    @Override
                    public void call(Listing<Link> linkListing) {
                        handleReceivedListing(linkListing);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mRequestInProgress = false;
                        handleListingRequestError(throwable);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mRequestInProgress = false;
                    }
                });
    }

    /**
     * Callback for successful Subreddit Listing GET request.
     */
    private void handleReceivedListing(Listing<Link> listing) {
        Timber.d("%s###handleReceivedListing(%s)", this, listing.getModhash());
        mAfter = listing.getAfter();

        if (mLinkListingRecyclerViewAdapter == null || TextUtils.isEmpty(mAfter)) {
            mLinkListingRecyclerViewAdapter = new LinkListingRecyclerViewAdapter(mImageLoader,
                    listing.getChildren(), this);
            mRecyclerView.setAdapter(mLinkListingRecyclerViewAdapter);
        } else {
            int position = mLinkListingRecyclerViewAdapter.getItemCount();
            mLinkListingRecyclerViewAdapter.addItems(listing.getChildren(), position);
        }

        mSwipeRefreshLayout.setRefreshing(false);

        if (mRootViewSwitcher.getDisplayedChild() == VIEW_SWITCHER_CHILD_LOAD_INDICATOR) {
            mRootViewSwitcher.showNext();
        }

        Timber.d("Fetched %d items with after={%s}.", listing.getChildren().size(), mAfter);
    }

    private void handleListingRequestError(Throwable throwable) {
        Timber.e(throwable, "Listing request failed");
        Toast.makeText(getActivity(), "Could not get new data", Toast.LENGTH_SHORT).show();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onClickCommentsButton(Link link) {
        Uri uri = Uri.parse("http://i.reddit.com" + link.getPermalink());
        Timber.d("Clicked comments button for %s", link);
        WebActivity.startActivity(getActivity(), uri);
    }

    @Override
    public void onClickMainContentArea(Link link) {
        Timber.d("Clicked main content area for %s", link.getUrl());
        WebActivity.startActivity(getActivity(), Uri.parse(link.getUrl()));
    }

    @Override
    public boolean onLongClickMainContentArea(Link link) {
        Timber.d("Long clicked %s", link.getUrl());
        return true;
    }

    private class OnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (mRequestInProgress) {
                Timber.d("Avoided queuing duplicate listing request for after={%s}", mAfter);
                return;
            }

            int lastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
            int itemCount = mLinearLayoutManager.getItemCount();
            int remainingItemsToShow = itemCount - (lastVisibleItemPosition + 1);

            if (remainingItemsToShow < 5) {
                Timber.d("Scrolled close to end of list. Remaining items to show is %d",
                        remainingItemsToShow);
                // start loading new items
                queueListingRequest();
            }
        }
    }
}
