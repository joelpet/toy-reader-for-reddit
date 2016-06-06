package se.joelpet.android.toyreaderforreddit.ui.linklisting;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsService;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action1;
import se.joelpet.android.toyreaderforreddit.R;
import se.joelpet.android.toyreaderforreddit.accounts.AccountManagerHelper;
import se.joelpet.android.toyreaderforreddit.customtabs.CustomTabActivityHelper;
import se.joelpet.android.toyreaderforreddit.model.Link;
import se.joelpet.android.toyreaderforreddit.ui.BaseFragment;
import se.joelpet.android.toyreaderforreddit.util.BitmapUtils;
import se.joelpet.android.toyreaderforreddit.util.LinkUtils;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

public class LinkListingFragment extends BaseFragment implements LinkListingContract.View,
        LinkListingRecyclerViewAdapter.ClickListener {

    private static final String ARGUMENT_LISTING = "argument_listing";
    public static final String ARG_LISTING_EVERYTHING = "r/all/";
    public static final String ARG_LISTING_SUBSCRIBED = "/";

    private static final String ARGUMENT_SORT = "argument_sort";
    public static final String ARG_SORT_HOT = "hot";
    public static final String ARG_SORT_NEW = "new";

    private static final int VIEW_SWITCHER_CHILD_LOAD_INDICATOR = 0;
    private static final int VIEW_SWITCHER_CHILD_RECYCLER_VIEW = 1;

    @BindView(R.id.root_view_switcher)
    protected ViewSwitcher rootViewSwitcher;

    @BindView(R.id.my_swipe_refresh_layout)
    protected SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.my_recycler_view)
    protected RecyclerView recyclerView;

    @Inject
    protected ImageLoader imageLoader;

    @Inject
    protected AccountManagerHelper accountManagerHelper;

    @Inject
    protected CustomTabActivityHelper customTabActivityHelper;

    @Inject
    protected CustomTabActivityHelper.CustomTabFallback customTabFallback;

    private Bitmap customTabCloseButton;
    private LinearLayoutManager linearLayoutManager;
    private LinkListingRecyclerViewAdapter linkListingRecyclerViewAdapter;
    private LinkListingContract.Presenter presenter;

    @NonNull
    public static LinkListingFragment newInstance(String listing, String sort) {
        LinkListingFragment fragment = new LinkListingFragment();
        Bundle arguments = new Bundle();
        fragment.setArguments(arguments);
        arguments.putString(ARGUMENT_LISTING, listing);
        arguments.putString(ARGUMENT_SORT, sort);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSubscription(BitmapUtils
                .decodeBitmapResource(getResources(), R.drawable.ic_arrow_back_black_24dp)
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        customTabCloseButton = bitmap;
                    }
                }));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subreddit, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.reloadLinks();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(new LoadMoreOnScrollListener());
        recyclerView.addOnScrollListener(new MayLaunchOnScrollListener());
    }

    @Override
    public void onStart() {
        super.onStart();
        customTabActivityHelper.bindCustomTabsService(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        customTabActivityHelper.unbindCustomTabsService(getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView.clearOnScrollListeners();
    }

    @Override
    public void showLinks(List<Link> links) {
        linkListingRecyclerViewAdapter = new LinkListingRecyclerViewAdapter(imageLoader, links, this);
        recyclerView.setAdapter(linkListingRecyclerViewAdapter);
    }

    @Override
    public void showMoreLinks(List<Link> links) {
        int position = linkListingRecyclerViewAdapter.getItemCount();
        linkListingRecyclerViewAdapter.addItems(links, position);
    }

    @Override
    public void showWebUi(@NonNull Uri uri) {
        checkNotNull(uri);
        CustomTabActivityHelper
                .openCustomTab(getActivity(), getCustomTabsIntent(), uri, customTabFallback);
    }

    @NonNull
    private CustomTabsIntent getCustomTabsIntent() {
        CustomTabsIntent.Builder builder = new CustomTabsIntent
                .Builder(customTabActivityHelper.getSession())
                .enableUrlBarHiding()
                .setShowTitle(true)
                .setToolbarColor(getResources().getColor(R.color.primary))
                .setStartAnimations(getContext(), R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(getContext(), R.anim.slide_in_left, R.anim.slide_out_right);

        if (customTabCloseButton != null)
            builder.setCloseButtonIcon(customTabCloseButton);

        return builder.build();
    }

    @Override
    public void setRefreshingIndicator(boolean active) {
        swipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        int displayedChild = rootViewSwitcher.getDisplayedChild();

        if (active && displayedChild == VIEW_SWITCHER_CHILD_RECYCLER_VIEW ||
                !active && displayedChild == VIEW_SWITCHER_CHILD_LOAD_INDICATOR) {
            rootViewSwitcher.showNext();
        }
    }

    @Override
    public void showAuthFailureError() {
        makeSnackbar(R.string.snackbar_account_credentials_expired, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.sign_in_again, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        presenter.renewCredentials(getActivity());
                    }
                }).show();
    }

    private Snackbar makeSnackbar(@StringRes int resId, int duration) {
        return Snackbar.make(rootViewSwitcher, resId, duration);
    }

    @Override
    public void showLoadFailureError() {
        makeSnackbar(R.string.snackbar_could_not_get_new_data, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        presenter.loadLinks();
                    }
                }).show();
    }

    @Override
    public void setPresenter(@NonNull LinkListingContract.Presenter presenter) {
        this.presenter = checkNotNull(presenter);
    }

    @Override
    public void onClickCommentsButton(@NonNull Link link) {
        presenter.openLinkComments(checkNotNull(link));
    }

    @Override
    public void onClickMainContentArea(@NonNull Link link) {
        presenter.openLink(checkNotNull(link));
    }

    @Override
    public boolean onLongClickMainContentArea(@NonNull Link link) {
        // no-op for now
        return false;
    }


    private class LoadMoreOnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
            int itemCount = linearLayoutManager.getItemCount();
            int remainingItemsToShow = itemCount - (lastVisibleItemPosition + 1);

            if (remainingItemsToShow < 5) {
                Timber.d("Scrolled close to end of list. Remaining items to show is %d",
                        remainingItemsToShow);
                presenter.loadLinks();
            }
        }
    }

    private class MayLaunchOnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (RecyclerView.SCROLL_STATE_IDLE != newState) return;

            List<Integer> likelyAdapterPositions = findLikelyAdapterPositions();
            Uri[] mayLaunchUrls = getMayLaunchUrlsFrom(likelyAdapterPositions);

            hintUrlsMayLaunch(Arrays.asList(mayLaunchUrls));
        }

        @NonNull
        private List<Integer> findLikelyAdapterPositions() {
            int firstVisible = linearLayoutManager.findFirstVisibleItemPosition();
            int lastVisible = linearLayoutManager.findLastVisibleItemPosition();

            if (firstVisible == RecyclerView.NO_POSITION || lastVisible == RecyclerView.NO_POSITION)
                return Collections.emptyList();

            int visibleItemsCount = lastVisible - firstVisible + 1;
            List<Integer> likelyLayoutPositions = new ArrayList<>(visibleItemsCount);

            int firstCompletelyVisible = linearLayoutManager
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
                Link link = linkListingRecyclerViewAdapter.getItem(adapterPositions.get(i));

                mayLaunchUrls[2 * i] = LinkUtils.getCommentsUri(link);
                mayLaunchUrls[2 * i + 1] = LinkUtils.getLinkUri(link);
            }

            return mayLaunchUrls;
        }

        private void hintUrlsMayLaunch(@NonNull List<Uri> mayLaunchUrls) {
            if (mayLaunchUrls.isEmpty()) return;
            Timber.d("Hinting URLs may launch: %s", mayLaunchUrls);

            Uri mostLikelyUrl = getMostLikelyUrlFrom(mayLaunchUrls);

            if (mostLikelyUrl != null) {
                List<Uri> otherLikelyUrls = getOtherLikelyUrlsFrom(mayLaunchUrls);
                List<Bundle> otherLikelyUrlBundles = bundleUrlsForCustomTabsService(otherLikelyUrls);
                customTabActivityHelper.mayLaunchUrl(mostLikelyUrl, null, otherLikelyUrlBundles);
            }
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
