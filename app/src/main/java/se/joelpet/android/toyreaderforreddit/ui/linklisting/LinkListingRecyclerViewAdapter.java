package se.joelpet.android.toyreaderforreddit.ui.linklisting;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import se.joelpet.android.toyreaderforreddit.R;
import se.joelpet.android.toyreaderforreddit.model.Link;
import timber.log.Timber;

public class LinkListingRecyclerViewAdapter
        extends RecyclerView.Adapter<LinkListingRecyclerViewAdapter.ViewHolder> {

    public static final Pattern VALID_URL_PATTERN = Pattern.compile("^https?://.*$");

    private final ImageLoader mImageLoader;

    private final List<Link> mLinks;

    private ClickListener mClickListener;

    public LinkListingRecyclerViewAdapter(ImageLoader imageLoader, List<Link> links,
                                          ClickListener clickListener) {
        mImageLoader = imageLoader;
        mLinks = links;
        mClickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, final int i) {
        Context context = vh.root.getContext();
        Link link = mLinks.get(i);

        vh.domain.setText(link.getDomain());
        vh.subreddit.setText("/r/" + link.getSubreddit());
        vh.title.setText(Html.fromHtml(link.getTitle()));

        if (VALID_URL_PATTERN.matcher(link.getThumbnail()).matches()) {
            Timber.v("Settings thumbnail image: %s", link.getThumbnail());
            vh.thumbnail.setImageUrl(link.getThumbnail(), mImageLoader);
            vh.thumbnail.setVisibility(View.VISIBLE);
        } else {
            vh.thumbnail.setVisibility(View.GONE);
        }

        CharSequence relativeDateTimeString = DateUtils.getRelativeDateTimeString(context,
                link.getCreatedUtc().longValue() * DateUtils.SECOND_IN_MILLIS,
                DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);
        String submittedInfoText = context
                .getString(R.string.submitted_info_text, relativeDateTimeString,
                        link.getAuthor());
        vh.submittedInfoText.setText(submittedInfoText);

        vh.over18tag.setVisibility(link.getOver18() ? View.VISIBLE : View.GONE);

        vh.score.setText(String.valueOf(link.getScore()));

        vh.starButton.setVisibility(link.getGilded() > 0 ? View.VISIBLE : View.GONE);

        vh.commentsButton
                .setText(context.getString(R.string.num_comments, link.getNumComments()));

        vh.commentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onClickCommentsButton(mLinks.get(i));
            }
        });

        vh.mainContentArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onClickMainContentArea(mLinks.get(i));
            }
        });

        vh.mainContentArea.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return mClickListener.onLongClickMainContentArea(mLinks.get(i));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLinks.size();
    }

    public void addItems(List<Link> links, int position) {
        mLinks.addAll(position, links);
        notifyItemRangeInserted(position, links.size());
    }

    @NonNull
    public Link getItem(int position) {
        return mLinks.get(position);
    }

    public interface ClickListener {

        void onClickCommentsButton(@NonNull Link link);

        void onClickMainContentArea(@NonNull Link link);

        boolean onLongClickMainContentArea(@NonNull Link link);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.card_view)
        View root;

        @BindView(R.id.domain)
        TextView domain;

        @BindView(R.id.subreddit)
        TextView subreddit;

        @BindView(R.id.main_content_area)
        View mainContentArea;

        @BindView(R.id.title)
        TextView title;

        @BindView(R.id.thumbnail)
        NetworkImageView thumbnail;

        @BindView(R.id.submitted_info_text)
        TextView submittedInfoText;

        @BindView(R.id.over_18_tag)
        TextView over18tag;

        @BindView(R.id.score)
        TextView score;

        @BindView(R.id.star_button)
        TextView starButton;

        @BindView(R.id.comments_button)
        TextView commentsButton;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, root = itemView);
        }
    }
}
