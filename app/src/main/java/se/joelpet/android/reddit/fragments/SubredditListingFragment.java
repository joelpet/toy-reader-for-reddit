package se.joelpet.android.reddit.fragments;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class SubredditListingFragment extends Fragment {

    public static final String TAG = SubredditActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;

    public SubredditListingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subreddit, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onResume() {
        super.onResume();

        ListingRequest<SubredditListingWrapper> listingRequest = new ListingRequest<>(
                "http://www.reddit.com/hot.json", SubredditListingWrapper.class, null,
                new Response.Listener<SubredditListingWrapper>() {
                    @Override
                    public void onResponse(SubredditListingWrapper listingWrapper) {
                        SubredditWrapperListing subredditWrapperListing = listingWrapper.getData();
                        List<Subreddit> subreddits = new ArrayList<>(
                                subredditWrapperListing.getChildren().size());

                        for (SubredditWrapper subredditWrapper : subredditWrapperListing
                                .getChildren()) {
                            subreddits.add(subredditWrapper.getData());
                        }

                        Log.d(TAG, String.format("Fetched %d items", subreddits.size()));
                        mRecyclerView.setAdapter(new SubredditRecyclerViewAdapter(subreddits));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
            }
        });

        VolleySingleton.getInstance(getActivity().getApplicationContext()).addToRequestQueue(
                listingRequest);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
