package se.joelpet.android.reddit.fragments;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

public class SubredditListingFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener, Response.Listener<SubredditListingWrapper>,
        Response.ErrorListener {

    public static final String TAG = SubredditActivity.class.getSimpleName();

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;

    private ListingRequest<SubredditListingWrapper> mListingRequest;

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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onRefresh() {
        queueListingRequest();
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

        Log.d(TAG, String.format("Fetched %d items", subreddits.size()));

        mRecyclerView.setAdapter(new SubredditRecyclerViewAdapter(subreddits));

        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        Log.e(TAG, "Listing request failed", volleyError);
        Toast.makeText(getActivity(), "Could not get new data", Toast.LENGTH_SHORT).show();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void queueListingRequest() {
        if (mListingRequest == null) {
            mListingRequest = new ListingRequest<>("http://www.reddit.com/hot.json",
                    SubredditListingWrapper.class, null, this, this);
        }

        VolleySingleton.getInstance(getActivity()).addToRequestQueue(mListingRequest);
        Log.d(TAG, "Added listing request to queue: " + mListingRequest);
    }
}
