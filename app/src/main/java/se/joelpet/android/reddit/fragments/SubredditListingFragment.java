package se.joelpet.android.reddit.fragments;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import se.joelpet.android.reddit.R;
import se.joelpet.android.reddit.VolleySingleton;
import se.joelpet.android.reddit.activities.SubredditActivity;
import se.joelpet.android.reddit.adapters.SubredditRecyclerViewAdapter;
import se.joelpet.android.reddit.domain.Subreddit;
import se.joelpet.android.reddit.domain.SubredditListingWrapper;
import se.joelpet.android.reddit.domain.SubredditWrapper;
import se.joelpet.android.reddit.domain.SubredditWrapperListing;
import se.joelpet.android.reddit.gson.ListingRequest;

public class SubredditListingFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = SubredditActivity.class.getSimpleName();

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;

    private ListingRequest<SubredditListingWrapper> mListingRequest;

    private LinearLayoutManager mLinearLayoutManager;

    private String mAfter;

    private SubredditRecyclerViewAdapter
            mSubredditRecyclerViewAdapter;

    public SubredditListingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queueListingRequest();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subreddit, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.my_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setOnScrollListener(new OnScrollListener());
    }

    @Override
    public void onRefresh() {
        mAfter = null;
        queueListingRequest();
    }

    private void queueListingRequest() {
        String url = "http://www.reddit.com/hot.json";

        if (!TextUtils.isEmpty(mAfter)) {
            if (mListingRequest != null && mListingRequest.getUrl().endsWith(mAfter)) {
                Log.d(TAG, String.format("Avoided queuing duplicate listing request for after={%s}",
                        mAfter));
                return;
            }
            url += "?after=" + mAfter;
        }

        ResponseListener listener = new ResponseListener(url);
        mListingRequest = new ListingRequest<>(url, SubredditListingWrapper.class, null,
                listener, listener);

        VolleySingleton.getInstance(getActivity()).addToRequestQueue(mListingRequest);
        Log.d(TAG, "Added listing request to queue: " + mListingRequest);
    }

    private class ResponseListener implements Response.Listener<SubredditListingWrapper>,
            Response.ErrorListener {

        private final String mRequestedUrl;

        private ResponseListener(String requestedUrl) {
            mRequestedUrl = requestedUrl;
        }

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

            if (mSubredditRecyclerViewAdapter == null || !mRequestedUrl.contains("?after=")) {
                mSubredditRecyclerViewAdapter = new SubredditRecyclerViewAdapter(subreddits);
                mRecyclerView.setAdapter(mSubredditRecyclerViewAdapter);
            } else {
                int position = mSubredditRecyclerViewAdapter.getItemCount();
                mSubredditRecyclerViewAdapter.addItems(subreddits, position);
            }

            mSwipeRefreshLayout.setRefreshing(false);

            Log.d(TAG,
                    String.format("Fetched %d items with after={%s}.", subreddits.size(), mAfter));
        }

        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Log.e(TAG, "Listing request failed", volleyError);
            Toast.makeText(getActivity(), "Could not get new data", Toast.LENGTH_SHORT).show();
            mSwipeRefreshLayout.setRefreshing(false);
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
                Log.d(TAG, String.format(
                        "Scrolled close to end of list. Remaining items to show is %d",
                        remainingItemsToShow));
                // start loading new items
                queueListingRequest();
            }
        }
    }
}
