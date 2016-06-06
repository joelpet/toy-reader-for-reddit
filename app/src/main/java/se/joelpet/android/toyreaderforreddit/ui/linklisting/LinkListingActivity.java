package se.joelpet.android.toyreaderforreddit.ui.linklisting;

import android.os.Bundle;

import javax.inject.Inject;

import se.joelpet.android.toyreaderforreddit.R;
import se.joelpet.android.toyreaderforreddit.ui.BaseActivity;
import se.joelpet.android.toyreaderforreddit.net.OAuthRedditApi;

public class LinkListingActivity extends BaseActivity {

    private static final String TAG_LINK_LISTING = "LINK_LISTING";

    @Inject
    OAuthRedditApi oAuthRedditApi;

    private LinkListingContract.Presenter presenter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_listing);

        LinkListingFragment linkListingFragment = (LinkListingFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_LINK_LISTING);

        if (linkListingFragment == null) {
            linkListingFragment = new LinkListingFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, linkListingFragment, TAG_LINK_LISTING)
                    .commit();
        }

        // TODO: Inject Presenter with Dagger
        presenter = new LinkListingPresenter(linkListingFragment, oAuthRedditApi);
    }
}
