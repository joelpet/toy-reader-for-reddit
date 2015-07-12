package se.joelpet.android.toyredditreader.activities;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.functions.Action1;
import se.joelpet.android.toyredditreader.R;
import se.joelpet.android.toyredditreader.domain.Me;
import se.joelpet.android.toyredditreader.fragments.LinkListingFragment;
import se.joelpet.android.toyredditreader.storage.LocalDataStore;

public class MainActivity extends BaseActivity implements NavigationView
        .OnNavigationItemSelectedListener {

    public static final int DRAWER_GRAVITY = GravityCompat.START;
    public static final int REQUEST_CODE_LOGIN = 1;

    @InjectView(R.id.toolbar)
    protected Toolbar mToolbar;

    @InjectView(R.id.drawer_layout)
    protected DrawerLayout mDrawerLayout;

    @InjectView(R.id.navigation_view)
    protected NavigationView mNavigationView;

    @InjectView(R.id.user_name)
    protected TextView mUserNameView;

    @InjectView(R.id.user_email)
    protected TextView mUserEmailView;

    private ActionBarDrawerToggle mDrawerToggle;

    @Inject
    protected LocalDataStore mLocalDataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inject(this);
        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, DRAWER_GRAVITY);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mNavigationView.setNavigationItemSelectedListener(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, LinkListingFragment.newInstance(
                            LinkListingFragment.ARG_LISTING_EVERYTHING,
                            LinkListingFragment.ARG_SORT_HOT)).commit();
        }

        addSubscription(bind(mLocalDataStore.observeMe()).subscribe(new Action1<Me>() {
            @Override
            public void call(Me me) {
                mUserNameView.setText(me.getName());

                Period redditorPeriod = new Period(me.getCreationDateTime(),
                        DateTime.now(DateTimeZone.UTC)).normalizedStandard();

                int redditorPeriodYears = redditorPeriod.getYears();
                int redditorPeriodMonths = redditorPeriod.getMonths();

                String years = getResources().getQuantityString(R.plurals.years,
                        redditorPeriodYears, redditorPeriodYears);
                String months = getResources().getQuantityString(R.plurals.months,
                        redditorPeriodMonths, redditorPeriodMonths);

                CharSequence memberSinceText = getResources().getString(R.string
                        .redditor_for_years_months, years, months);
                mUserEmailView.setText(memberSinceText);
            }
        }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsubscribeFromAll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.subreddit, menu);
        return false;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(DRAWER_GRAVITY)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    @OnClick(R.id.user_name)
    protected void onUserNameViewClick(View view) {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_LOGIN) {
            if (resultCode == Activity.RESULT_OK) {
                Me me = (Me) data.getSerializableExtra("me");
                mLocalDataStore.putMe(me);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        Fragment fragment;
        switch (menuItem.getItemId()) {
            case R.id.navigation_everything:
                mToolbar.setTitle("Everything");
                mToolbar.setSubtitle("from all subreddits");
                fragment = LinkListingFragment
                        .newInstance(LinkListingFragment.ARG_LISTING_EVERYTHING,
                                LinkListingFragment.ARG_SORT_HOT);
                break;
            case R.id.navigation_subscribed:
                mToolbar.setTitle("Subscribed");
                mToolbar.setSubtitle("your frontpage");
                fragment = LinkListingFragment
                        .newInstance(LinkListingFragment.ARG_LISTING_SUBSCRIBED,
                                LinkListingFragment.ARG_SORT_HOT);
                break;
            default:
                return false;
        }
        mDrawerLayout.closeDrawers();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        menuItem.setChecked(true);
        return true;
    }
}
