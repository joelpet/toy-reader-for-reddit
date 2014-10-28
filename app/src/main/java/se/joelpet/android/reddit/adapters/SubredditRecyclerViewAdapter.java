package se.joelpet.android.reddit.adapters;

import com.android.volley.toolbox.NetworkImageView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.regex.Pattern;

import se.joelpet.android.reddit.R;
import se.joelpet.android.reddit.VolleySingleton;
import se.joelpet.android.reddit.domain.Subreddit;

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
        vh.subreddit.setText("/r/" + subreddit.getSubreddit());
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
                        subreddit.getAuthor());
        vh.submittedInfoText.setText(submittedInfoText);

        vh.commentsButton
                .setText(context.getString(R.string.num_comments, subreddit.getNumComments()));

        vh.commentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Subreddit subreddit = mSubreddits.get(i);
                Uri uri = Uri.parse("http://i.reddit.com" + subreddit.getPermalink());
                Log.d(TAG, "Opening " + uri);
                v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });

        vh.mainContentArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Subreddit subreddit = mSubreddits.get(i);
                Log.d(TAG, "Clicked " + subreddit.getUrl());
                v.getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(subreddit.getUrl())));
            }
        });

        vh.mainContentArea.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Subreddit subreddit = mSubreddits.get(i);
                Log.d(TAG, "Long clicked " + subreddit.getUrl());
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSubreddits.size();
    }

    public void addItems(List<Subreddit> subreddits, int position) {
        mSubreddits.addAll(position, subreddits);
        notifyItemRangeInserted(position, subreddits.size());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final View root;

        final TextView domain;

        final TextView subreddit;

        final View mainContentArea;

        final TextView title;

        final NetworkImageView thumbnail;

        final TextView submittedInfoText;

        final TextView commentsButton;

        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            domain = (TextView) itemView.findViewById(R.id.domain);
            subreddit = (TextView) itemView.findViewById(R.id.subreddit);
            mainContentArea = itemView.findViewById(R.id.main_content_area);
            title = (TextView) itemView.findViewById(R.id.title);
            thumbnail = (NetworkImageView) itemView.findViewById(R.id.thumbnail);
            submittedInfoText = (TextView) itemView.findViewById(R.id.submitted_info_text);
            commentsButton = (TextView) itemView.findViewById(R.id.comments_button);
        }
    }
}
