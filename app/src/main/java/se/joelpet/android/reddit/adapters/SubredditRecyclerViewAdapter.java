package se.joelpet.android.reddit.adapters;

import com.android.volley.toolbox.NetworkImageView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
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
import java.util.regex.Pattern;

import se.joelpet.android.reddit.R;
import se.joelpet.android.reddit.VolleySingleton;
import se.joelpet.android.reddit.domain.Subreddit;
import se.joelpet.android.reddit.fragments.SubredditListingFragment;

public class SubredditRecyclerViewAdapter
        extends RecyclerView.Adapter<SubredditRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = SubredditRecyclerViewAdapter.class.getSimpleName();

    public static final Pattern VALID_URL_PATTERN = Pattern.compile("^https?://.*$");

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
        Context context = vh.root.getContext();
        Subreddit subreddit = mSubreddits.get(i);
        VolleySingleton volleySingleton = VolleySingleton
                .getInstance(context.getApplicationContext());

        vh.domain.setText(subreddit.getDomain());
        vh.title.setText(subreddit.getTitle());

        if (VALID_URL_PATTERN.matcher(subreddit.getThumbnail()).matches()) {
            Log.d(TAG, "Setting thumbnail image: " + subreddit.getThumbnail());
            vh.thumbnail.setImageUrl(subreddit.getThumbnail(), volleySingleton.getImageLoader());
            vh.thumbnail.setVisibility(View.VISIBLE);
        } else {
            vh.thumbnail.setVisibility(View.GONE);
        }

        CharSequence relativeDateTimeString = DateUtils.getRelativeDateTimeString(context,
                subreddit.getCreatedUtc().longValue() * DateUtils.SECOND_IN_MILLIS,
                DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);
        String submittedInfoText = context
                .getString(R.string.submitted_info_text, relativeDateTimeString,
                        subreddit.getAuthor(), subreddit.getSubreddit());
        vh.submittedInfoText.setText(submittedInfoText);

        vh.numComments
                .setText(context.getString(R.string.num_comments, subreddit.getNumComments()));

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

        final TextView domain;

        final ImageButton overflowButton;

        final TextView title;

        final NetworkImageView thumbnail;

        final TextView submittedInfoText;

        final TextView numComments;

        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            domain = (TextView) itemView.findViewById(R.id.domain);
            overflowButton = (ImageButton) itemView.findViewById(R.id.overflow_button);
            title = (TextView) itemView.findViewById(R.id.title);
            thumbnail = (NetworkImageView) itemView.findViewById(R.id.thumbnail);
            submittedInfoText = (TextView) itemView.findViewById(R.id.submitted_info_text);
            numComments = (TextView) itemView.findViewById(R.id.num_comments);
        }
    }
}
