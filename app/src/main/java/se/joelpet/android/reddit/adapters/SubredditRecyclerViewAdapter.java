package se.joelpet.android.reddit.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.List;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import se.joelpet.android.reddit.R;
import se.joelpet.android.reddit.VolleySingleton;
import se.joelpet.android.reddit.domain.Subreddit;
import timber.log.Timber;

public class SubredditRecyclerViewAdapter
        extends RecyclerView.Adapter<SubredditRecyclerViewAdapter.ViewHolder> {

    public static final Pattern VALID_URL_PATTERN = Pattern.compile("^https?://.*$");

    private final List<Subreddit> mSubreddits;

    private ClickListener mClickListener;

    public SubredditRecyclerViewAdapter(List<Subreddit> subreddits, ClickListener clickListener) {
        mSubreddits = subreddits;
        mClickListener = clickListener;
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
            Timber.d("Settings thumbnail image: %s", subreddit.getThumbnail());
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
                mClickListener.onClickCommentsButton(mSubreddits.get(i));
            }
        });

        vh.mainContentArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onClickMainContentArea(mSubreddits.get(i));
            }
        });

        vh.mainContentArea.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return mClickListener.onLongClickMainContentArea(mSubreddits.get(i));
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

    public static interface ClickListener {
        void onClickCommentsButton(Subreddit subreddit);

        void onClickMainContentArea(Subreddit subreddit);

        boolean onLongClickMainContentArea(Subreddit subreddit);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.card_view)
        View root;

        @InjectView(R.id.domain)
        TextView domain;

        @InjectView(R.id.subreddit)
        TextView subreddit;

        @InjectView(R.id.main_content_area)
        View mainContentArea;

        @InjectView(R.id.title)
        TextView title;

        @InjectView(R.id.thumbnail)
        NetworkImageView thumbnail;

        @InjectView(R.id.submitted_info_text)
        TextView submittedInfoText;

        @InjectView(R.id.comments_button)
        TextView commentsButton;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, root = itemView);
        }
    }
}
