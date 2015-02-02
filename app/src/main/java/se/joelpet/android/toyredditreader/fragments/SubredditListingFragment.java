package se.joelpet.android.toyredditreader.fragments;

import com.android.volley.Response;
import com.android.volley.VolleyError;

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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import se.joelpet.android.toyredditreader.R;
import se.joelpet.android.toyredditreader.VolleySingleton;
import se.joelpet.android.toyredditreader.activities.WebActivity;
import se.joelpet.android.toyredditreader.adapters.SubredditRecyclerViewAdapter;
import se.joelpet.android.toyredditreader.domain.Subreddit;
import se.joelpet.android.toyredditreader.domain.SubredditListingWrapper;
import se.joelpet.android.toyredditreader.domain.SubredditWrapper;
import se.joelpet.android.toyredditreader.domain.SubredditWrapperListing;
import se.joelpet.android.toyredditreader.gson.ListingRequest;
import se.joelpet.android.toyredditreader.net.RedditApi;
import timber.log.Timber;

public class SubredditListingFragment extends BaseFragment
        implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = SubredditListingFragment.class.getName();

    @InjectView(R.id.my_swipe_refresh_layout)
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @InjectView(R.id.my_recycler_view)
    protected RecyclerView mRecyclerView;

    @Inject
    protected RedditApi mRedditApi;

    @Inject
    protected VolleySingleton mVolleySingleton;

    private ListingRequest<SubredditListingWrapper> mListingRequest;

    private LinearLayoutManager mLinearLayoutManager;

    private String mAfter;

    private SubredditRecyclerViewAdapter mSubredditRecyclerViewAdapter;

    private SubredditRecyclerViewAdapter.ClickListener mSubredditViewClickListener;

    public SubredditListingFragment() {
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
        // TODO Offer from API
        mVolleySingleton.getRequestQueue().cancelAll(TAG);
    }

    @Override
    public void onRefresh() {
        mAfter = null;
        queueListingRequest();
    }

    private void queueListingRequest() {

        if (!TextUtils.isEmpty(mAfter)) {
            if (mListingRequest != null && mListingRequest.getUrl().endsWith(mAfter)) {
                Timber.d("Avoided queuing duplicate listing request for after={%s}", mAfter);
                return;
            }
        }

        ResponseListener listener = new ResponseListener();
        mListingRequest = mRedditApi.getSubredditListing(mAfter, listener, listener);
    }

    private SubredditRecyclerViewAdapter.ClickListener getSubredditViewClickListener() {
        if (mSubredditViewClickListener == null) {
            mSubredditViewClickListener = new SubredditViewClickListener();
        }
        return mSubredditViewClickListener;
    }

    private class ResponseListener implements Response.Listener<SubredditListingWrapper>,
            Response.ErrorListener {

        @Override
        public void onResponse(SubredditListingWrapper subredditListingWrapper) {
            SubredditWrapperListing subredditWrapperListing = subredditListingWrapper.getData();
            List<Subreddit> subreddits = new ArrayList<>(
                    subredditWrapperListing.getChildren().size());

            for (SubredditWrapper subredditWrapper : subredditWrapperListing
                    .getChildren()) {
                subreddits.add(subredditWrapper.getData());
            }

            mAfter = subredditWrapperListing.getAfter();

            if (mSubredditRecyclerViewAdapter == null || TextUtils.isEmpty(mAfter)) {
                mSubredditRecyclerViewAdapter = new SubredditRecyclerViewAdapter(mVolleySingleton,
                        subreddits, getSubredditViewClickListener());
                mRecyclerView.setAdapter(mSubredditRecyclerViewAdapter);
            } else {
                int position = mSubredditRecyclerViewAdapter.getItemCount();
                mSubredditRecyclerViewAdapter.addItems(subreddits, position);
            }

            mSwipeRefreshLayout.setRefreshing(false);

            Timber.d("Fetched %d items with after={%s}.", subreddits.size(), mAfter);
        }

        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Timber.e(volleyError, "Listing request failed");
            Toast.makeText(getActivity(), "Could not get new data", Toast.LENGTH_SHORT).show();
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private class SubredditViewClickListener implements SubredditRecyclerViewAdapter.ClickListener {

        @Override
        public void onClickCommentsButton(Subreddit subreddit) {
            Uri uri = Uri.parse("http://i.reddit.com" + subreddit.getPermalink());
            Timber.d("Clicked comments button for %s", subreddit);
            WebActivity.startActivity(getActivity(), uri);
        }

        @Override
        public void onClickMainContentArea(Subreddit subreddit) {
            Timber.d("Clicked main content area for %s", subreddit.getUrl());
            WebActivity.startActivity(getActivity(), Uri.parse(subreddit.getUrl()));
        }

        @Override
        public boolean onLongClickMainContentArea(Subreddit subreddit) {
            Timber.d("Long clicked %s", subreddit.getUrl());
            return true;
        }
    }

    private class OnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

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
