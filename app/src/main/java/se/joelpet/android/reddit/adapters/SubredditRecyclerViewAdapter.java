package se.joelpet.android.reddit.adapters;

import com.android.volley.toolbox.NetworkImageView;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import se.joelpet.android.reddit.R;
import se.joelpet.android.reddit.VolleySingleton;
import se.joelpet.android.reddit.domain.Subreddit;
import se.joelpet.android.reddit.fragments.SubredditListingFragment;

public class SubredditRecyclerViewAdapter
        extends RecyclerView.Adapter<SubredditRecyclerViewAdapter.ViewHolder> {

    private final List<Subreddit> mSubreddits;

    public SubredditRecyclerViewAdapter(List<Subreddit> subreddits) {
        mSubreddits = subreddits;

    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, final int i) {
        Subreddit subreddit = mSubreddits.get(i);
        VolleySingleton volleySingleton = VolleySingleton
                .getInstance(vh.root.getContext().getApplicationContext());

        vh.domain.setText(subreddit.getDomain());
        vh.title.setText(subreddit.getTitle());
        vh.thumbnail.setImageUrl(subreddit.getThumbnail(), volleySingleton.getImageLoader());

        vh.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Clicked " + i, Toast.LENGTH_SHORT).show();
            }
        });
        vh.root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(v.getContext(), "Long clicked " + i, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        vh.overflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(SubredditListingFragment.TAG, "clicked context menu");
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.subreddit, popup.getMenu());
                popup.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mSubreddits.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final View root;

        final TextView title;

        final ImageButton overflowButton;

        final TextView domain;

        final NetworkImageView thumbnail;

        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            title = (TextView) itemView.findViewById(R.id.title);
            overflowButton = (ImageButton) itemView.findViewById(R.id.overflow_button);
            domain = (TextView) itemView.findViewById(R.id.domain);
            thumbnail = (NetworkImageView) itemView.findViewById(R.id.thumbnail);
        }
    }
}
