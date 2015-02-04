package se.joelpet.android.toyredditreader.fragments;

import com.android.volley.Response;
import com.android.volley.VolleyError;
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

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import se.joelpet.android.toyredditreader.R;
import se.joelpet.android.toyredditreader.activities.WebActivity;
import se.joelpet.android.toyredditreader.adapters.LinkListingRecyclerViewAdapter;
import se.joelpet.android.toyredditreader.domain.Link;
import se.joelpet.android.toyredditreader.domain.Listing;
import se.joelpet.android.toyredditreader.gson.ListingRequest;
import se.joelpet.android.toyredditreader.net.RedditApi;
import timber.log.Timber;

public class LinkListingFragment extends BaseFragment
        implements SwipeRefreshLayout.OnRefreshListener, Response.ErrorListener,
        LinkListingRecyclerViewAdapter.ClickListener, Response.Listener<Listing<Link>> {

    public static final String TAG = LinkListingFragment.class.getName();

    @InjectView(R.id.my_swipe_refresh_layout)
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @InjectView(R.id.my_recycler_view)
    protected RecyclerView mRecyclerView;

    @Inject
    protected RedditApi mRedditApi;

    @Inject
    protected ImageLoader mImageLoader;

    /** The currently (only) ongoing listing request, if any. */
    private ListingRequest<Link> mListingRequest;

    /** The "after" portion received in the response to the last made request. */
    private String mAfter;

    private LinearLayoutManager mLinearLayoutManager;

    private LinkListingRecyclerViewAdapter mLinkListingRecyclerViewAdapter;

    public LinkListingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inject(this);
        queueListingRequest();
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
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mRedditApi.cancelAll(TAG);
    }

    @Override
    public void onRefresh() {
        mAfter = null;
        queueListingRequest();
    }

    private void queueListingRequest() {
        mListingRequest = mRedditApi.getListing(mAfter, this, this);
        mListingRequest.setTag(TAG);
    }

    /**
     * Callback for successful Subreddit Listing GET request.
     */
    @Override
    public void onResponse(Listing<Link> listing) {
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
        mListingRequest = null;

        Timber.d("Fetched %d items with after={%s}.", listing.getChildren().size(), mAfter);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        Timber.e(volleyError, "Listing request failed");
        Toast.makeText(getActivity(), "Could not get new data", Toast.LENGTH_SHORT).show();
        mSwipeRefreshLayout.setRefreshing(false);
        mListingRequest = null;
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

            if (mListingRequest != null) {
                Timber.i("Avoided queuing duplicate listing request for after={%s}", mAfter);
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
