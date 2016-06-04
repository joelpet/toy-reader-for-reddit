package se.joelpet.android.toyreaderforreddit.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsService;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import se.joelpet.android.toyreaderforreddit.R;
import se.joelpet.android.toyreaderforreddit.accounts.AccountManagerHelper;
import se.joelpet.android.toyreaderforreddit.accounts.AddAccountResult;
import se.joelpet.android.toyreaderforreddit.adapters.LinkListingRecyclerViewAdapter;
import se.joelpet.android.toyreaderforreddit.customtabs.CustomTabActivityHelper;
import se.joelpet.android.toyreaderforreddit.domain.Link;
import se.joelpet.android.toyreaderforreddit.domain.Listing;
import se.joelpet.android.toyreaderforreddit.net.OAuthRedditApi;
import se.joelpet.android.toyreaderforreddit.rx.transformers.WorkOnIoAndOnNotifyOnMainTransformer;
import timber.log.Timber;

public class LinkListingFragment extends BaseFragment implements SwipeRefreshLayout
        .OnRefreshListener, LinkListingRecyclerViewAdapter.ClickListener {

    public static final String TAG = LinkListingFragment.class.getName();

    public static final String ARGUMENT_LISTING = "argument_listing";
    public static final String ARG_LISTING_EVERYTHING = "r/all/";
    public static final String ARG_LISTING_SUBSCRIBED = "/";

    public static final String ARGUMENT_SORT = "argument_sort";
    public static final String ARG_SORT_HOT = "hot";
    public static final String ARG_SORT_NEW = "new";

    public static final int VIEW_SWITCHER_CHILD_LOAD_INDICATOR = 0;
    public static final int VIEW_SWITCHER_CHILD_RECYCLER_VIEW = 1;

    public static final String STATE_STRING_AFTER = "mAfter";

    public static final String BASE_URL_COMMENTS = "http://m.reddit.com";

    @Bind(R.id.root_view_switcher)
    protected ViewSwitcher mRootViewSwitcher;

    @Bind(R.id.my_swipe_refresh_layout)
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.my_recycler_view)
    protected RecyclerView mRecyclerView;

    @Inject
    protected OAuthRedditApi mRedditApi;

    @Inject
    protected ImageLoader mImageLoader;

    @Inject
    protected AccountManagerHelper mAccountManagerHelper;

    @Inject
    protected CustomTabActivityHelper mCustomTabActivityHelper;

    @Inject
    protected CustomTabActivityHelper.CustomTabFallback customTabFallback;

    /** The path part of the URI pointing to the link listing of this fragment. */
    private String mListingPath;

    /** The "after" portion received in the response to the last made request. */
    private String mAfter;

    /** Flag indicating that a Listing request is in progress. */
    private boolean mRequestInProgress;

    private LinearLayoutManager mLinearLayoutManager;
    private LinkListingRecyclerViewAdapter mLinkListingRecyclerViewAdapter;
    private MayLaunchOnScrollListener mayLaunchOnScrollListener;
    private LoadMoreOnScrollListener loadMoreOnScrollListener;

    private Bitmap mCustomTabCloseButton;

    public static LinkListingFragment newInstance(String listing, String sort) {
        LinkListingFragment fragment = new LinkListingFragment();
        Bundle arguments = new Bundle();
        fragment.setArguments(arguments);
        arguments.putString(ARGUMENT_LISTING, listing);
        arguments.putString(ARGUMENT_SORT, sort);
        return fragment;
    }

    //region Fragment lifecycle

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_STRING_AFTER, mAfter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            String listing = getArguments().getString(ARGUMENT_LISTING);
            String sort = getArguments().getString(ARGUMENT_SORT);
            mListingPath = listing + sort;
        }

        if (savedInstanceState != null) {
            if (mLinkListingRecyclerViewAdapter != null) {
                // Only restore the 'after' fragment if link listing data is still present
                mAfter = savedInstanceState.getString("mAfter");
                Timber.d("Restored mAfter state to '%s'", mAfter);
            }
        }

        addSubscription(decodeBitmapResource(R.drawable.ic_arrow_back_black_24dp)
                .compose(WorkOnIoAndOnNotifyOnMainTransformer.<Bitmap>getInstance())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        mCustomTabCloseButton = bitmap;
                    }
                }));
    }

    @NonNull
    private Observable<Bitmap> decodeBitmapResource(@DrawableRes final int id) {
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
                if (bitmap != null) subscriber.onNext(bitmap);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subreddit, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        loadMoreOnScrollListener = new LoadMoreOnScrollListener();
        mRecyclerView.addOnScrollListener(loadMoreOnScrollListener);

        mayLaunchOnScrollListener = new MayLaunchOnScrollListener();
        mRecyclerView.addOnScrollListener(mayLaunchOnScrollListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        mCustomTabActivityHelper.bindCustomTabsService(getActivity());
        if (mAfter == null) {
            queueListingRequest();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mRedditApi.cancelAll(TAG);
        mRequestInProgress = false;
        mCustomTabActivityHelper.unbindCustomTabsService(getActivity());
    }

    @Override
    public void onDestroyView() {
        mRecyclerView.removeOnScrollListener(loadMoreOnScrollListener);
        mRecyclerView.removeOnScrollListener(mayLaunchOnScrollListener);
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unsubscribeFromAll();
    }

    //endregion

    @Override
    public void onRefresh() {
        if (mRequestInProgress) {
            return;
        }
        mAfter = null;
        queueListingRequest();
    }

    private void queueListingRequest() {
        mRequestInProgress = true;
        addSubscription(mRedditApi.getLinkListing(mListingPath, mAfter, TAG)
                .subscribe(new Action1<Listing<Link>>() {
                    @Override
                    public void call(Listing<Link> linkListing) {
                        handleReceivedListing(linkListing);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mRequestInProgress = false;
                        handleListingRequestError(throwable);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mRequestInProgress = false;
                    }
                }));
    }

    /**
     * Callback for successful Subreddit Listing GET request.
     */
    private void handleReceivedListing(Listing<Link> listing) {
        Timber.d("%s###handleReceivedListing(%s)", this, listing.getModhash());
        mAfter = listing.getAfter();

        if (mLinkListingRecyclerViewAdapter == null || TextUtils.isEmpty(mAfter)) {
            mLinkListingRecyclerViewAdapter = new LinkListingRecyclerViewAdapter(mImageLoader,
                    listing.getChildren(), this);
            mRecyclerView.setAdapter(mLinkListingRecyclerViewAdapter);
        } else {
            int position = mLinkListingRecyclerViewAdapter.getItemCount();
            mLinkListingRecyclerViewAdapter.addItems(listing.getChildren(), position);
        }

        mSwipeRefreshLayout.setRefreshing(false);

        if (mRootViewSwitcher.getDisplayedChild() == VIEW_SWITCHER_CHILD_LOAD_INDICATOR) {
            mRootViewSwitcher.showNext();
        }

        Timber.d("Fetched %d items with after={%s}.", listing.getChildren().size(), mAfter);
    }

    private void handleListingRequestError(Throwable throwable) {
        Timber.e(throwable, "Listing request failed");

        Snackbar.make(mRootViewSwitcher, R.string.snackbar_could_not_get_new_data,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        queueListingRequest();
                    }
                }).show();

        mSwipeRefreshLayout.setRefreshing(false);

        if (mRootViewSwitcher.getDisplayedChild() == VIEW_SWITCHER_CHILD_LOAD_INDICATOR) {
            mRootViewSwitcher.showNext();
        }

        if (throwable.getCause() instanceof AuthFailureError) {
            showCredentialsExpiredSnackbar();
        }
    }

    private void showCredentialsExpiredSnackbar() {
        // TODO: Change to LENGTH_INDEFINITE when available in Design Library
        Snackbar.make(mRootViewSwitcher, R.string.snackbar_account_credentials_expired,
                Snackbar.LENGTH_LONG).setAction(R.string.sign_in_again, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAccountManagerHelper
                        .addAccount(getActivity())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<AddAccountResult>() {
                            @Override
                            public void call(AddAccountResult result) {
                                Timber.d("Account added; queueing new listing request.");
                                queueListingRequest();
                            }
                        });
            }
        }).show();
    }

    @Override
    public void onClickCommentsButton(Link link) {
        Timber.d("Clicked comments button for %s", link);
        openUri(getCommentsUri(link));
    }

    @NonNull
    private static Uri getCommentsUri(@NonNull Link link) {
        return Uri.parse(BASE_URL_COMMENTS + link.getPermalink());
    }

    private void openUri(@NonNull Uri uri) {
        CustomTabActivityHelper
                .openCustomTab(getActivity(), getCustomTabsIntent(), uri, customTabFallback);
    }

    @NonNull
    private CustomTabsIntent getCustomTabsIntent() {
        CustomTabsIntent.Builder builder = new CustomTabsIntent
                .Builder(mCustomTabActivityHelper.getSession())
                .enableUrlBarHiding()
                .setShowTitle(true)
                .setToolbarColor(getResources().getColor(R.color.primary))
                .setStartAnimations(getContext(), R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(getContext(), R.anim.slide_in_left, R.anim.slide_out_right);

        if (mCustomTabCloseButton != null)
            builder.setCloseButtonIcon(mCustomTabCloseButton);

        return builder.build();
    }

    @Override
    public void onClickMainContentArea(Link link) {
        Timber.d("Clicked main content area for %s", link.getUrl());
        openUri(getLinkUri(link));
    }

    @NonNull
    private static Uri getLinkUri(Link link) {
        return Uri.parse(link.getUrl());
    }

    @Override
    public boolean onLongClickMainContentArea(Link link) {
        Timber.d("Long clicked %s", link.getUrl());
        return true;
    }

    private class LoadMoreOnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (mRequestInProgress) {
                Timber.d("Avoided queuing duplicate listing request for after={%s}", mAfter);
                return;
            }

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

    private class MayLaunchOnScrollListener<T> extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (RecyclerView.SCROLL_STATE_IDLE != newState) return;

            List<Integer> likelyAdapterPositions = findLikelyAdapterPositions();
            Uri[] mayLaunchUrls = getMayLaunchUrlsFrom(likelyAdapterPositions);

            hintUrlsMayLaunch(Arrays.asList(mayLaunchUrls));
        }

        @NonNull
        private List<Integer> findLikelyAdapterPositions() {
            int firstVisible = mLinearLayoutManager.findFirstVisibleItemPosition();
            int lastVisible = mLinearLayoutManager.findLastVisibleItemPosition();

            if (firstVisible == RecyclerView.NO_POSITION || lastVisible == RecyclerView.NO_POSITION)
                return Collections.emptyList();

            int visibleItemsCount = lastVisible - firstVisible + 1;
            List<Integer> likelyLayoutPositions = new ArrayList<>(visibleItemsCount);

            int firstCompletelyVisible = mLinearLayoutManager
                    .findFirstCompletelyVisibleItemPosition();

            if (firstCompletelyVisible != RecyclerView.NO_POSITION)
                likelyLayoutPositions.add(firstCompletelyVisible);

            for (int position = firstVisible; position <= lastVisible; position++)
                if (position != firstCompletelyVisible)
                    likelyLayoutPositions.add(position);

            return likelyLayoutPositions;
        }

        @NonNull
        private Uri[] getMayLaunchUrlsFrom(@NonNull List<Integer> adapterPositions) {
            int mayLaunchUrlsCount = 2 * adapterPositions.size();
            Uri[] mayLaunchUrls = new Uri[mayLaunchUrlsCount];

            for (int i = 0, length = adapterPositions.size(); i < length; i++) {
                Link link = mLinkListingRecyclerViewAdapter.getItem(adapterPositions.get(i));

                mayLaunchUrls[2 * i] = getCommentsUri(link);
                mayLaunchUrls[2 * i + 1] = getLinkUri(link);
            }

            return mayLaunchUrls;
        }

        private void hintUrlsMayLaunch(@NonNull List<Uri> mayLaunchUrls) {
            if (mayLaunchUrls.isEmpty()) return;
            Timber.d("Hinting URLs may launch: %s", mayLaunchUrls);

            Uri mostLikelyUrl = getMostLikelyUrlFrom(mayLaunchUrls);
            List<Uri> otherLikelyUrls = getOtherLikelyUrlsFrom(mayLaunchUrls);
            List<Bundle> otherLikelyUrlBundles = bundleUrlsForCustomTabsService(otherLikelyUrls);

            if (mostLikelyUrl != null)
                mCustomTabActivityHelper.mayLaunchUrl(mostLikelyUrl, null, otherLikelyUrlBundles);
        }

        @Nullable
        private Uri getMostLikelyUrlFrom(@NonNull List<Uri> mayLaunchUrls) {
            return mayLaunchUrls.isEmpty() ? null : mayLaunchUrls.get(0);
        }

        private List<Uri> getOtherLikelyUrlsFrom(@NonNull List<Uri> mayLaunchUrls) {
            return mayLaunchUrls.size() <= 1 ? Collections.<Uri>emptyList() :
                    mayLaunchUrls.subList(1, mayLaunchUrls.size());
        }

        @NonNull
        private List<Bundle> bundleUrlsForCustomTabsService(@NonNull List<Uri> urls) {
            if (urls.isEmpty()) return Collections.emptyList();

            List<Bundle> bundles = new ArrayList<>(urls.size());

            for (Uri url : urls) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(CustomTabsService.KEY_URL, url);
                bundles.add(bundle);
            }

            return bundles;
        }
    }
}
