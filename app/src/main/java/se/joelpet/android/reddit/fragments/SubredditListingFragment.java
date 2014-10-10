package se.joelpet.android.reddit.fragments;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import android.app.ListFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import se.joelpet.android.reddit.R;
import se.joelpet.android.reddit.activities.SubredditActivity;
import se.joelpet.android.reddit.domain.Subreddit;
import se.joelpet.android.reddit.domain.SubredditListingWrapper;
import se.joelpet.android.reddit.domain.SubredditWrapper;
import se.joelpet.android.reddit.domain.SubredditWrapperListing;
import se.joelpet.android.reddit.gson.ListingRequest;

public class SubredditListingFragment extends ListFragment {

    public static final String TAG = SubredditActivity.class.getSimpleName();

    private RequestQueue mQueue;

    public SubredditListingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        mQueue = Volley.newRequestQueue(getActivity());

        ListingRequest<SubredditListingWrapper> listingRequest
                = new ListingRequest<SubredditListingWrapper>(
                "http://www.reddit.com/hot.json", SubredditListingWrapper.class, null,
                new Response.Listener<SubredditListingWrapper>() {
                    @Override
                    public void onResponse(SubredditListingWrapper listingWrapper) {
                        SubredditWrapperListing subredditWrapperListing = listingWrapper.getData();
                        List<Subreddit> subreddits = new ArrayList<Subreddit>(
                                subredditWrapperListing.getChildren().size());

                        for (SubredditWrapper subredditWrapper : subredditWrapperListing
                                .getChildren()) {
                            subreddits.add(subredditWrapper.getData());
                        }

                        setListAdapter(new SubredditListAdapter(getActivity(), subreddits));

                        Log.d(TAG, String.format("Fetched %d items", subreddits.size()));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
            }
        });

        mQueue.add(listingRequest);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private class SubredditListAdapter extends ArrayAdapter<Subreddit> {

        private final LayoutInflater mInflater;

        public SubredditListAdapter(Context context, List<Subreddit> subreddits) {
            super(context, 0, subreddits);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            getListView().setDividerHeight(0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(R.layout.view_card, parent, false);
            } else {
                view = convertView;
            }

            TextView selftext = (TextView) view.findViewById(R.id.title);
            ImageButton overflowButton = (ImageButton) view.findViewById(R.id.overflow_button);
            TextView domain = (TextView) view.findViewById(R.id.domain);
            NetworkImageView thumbnail = (NetworkImageView) view.findViewById(R.id.thumbnail);

            registerForContextMenu(overflowButton);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "Clicked", Toast.LENGTH_SHORT).show();
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(getContext(), "Long clicked", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            overflowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "clicked context menu");
                    PopupMenu popup = new PopupMenu(getContext(), v);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.subreddit, popup.getMenu());
                    popup.show();
                }
            });

            Subreddit subreddit = getItem(position);
            selftext.setText(subreddit.getTitle());
            domain.setText(subreddit.getDomain());

            thumbnail.setImageUrl(subreddit.getThumbnail(),
                    new ImageLoader(mQueue, new ImageLoader.ImageCache() {

                        private final LruCache<String, Bitmap> mCache
                                = new LruCache<String, Bitmap>(20);

                        @Override
                        public Bitmap getBitmap(String url) {
                            return mCache.get(url);
                        }

                        @Override
                        public void putBitmap(String url, Bitmap bitmap) {
                            mCache.put(url, bitmap);
                        }
                    }));

            return view;
        }
    }
}
