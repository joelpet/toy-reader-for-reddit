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
import android.widget.ViewSwitcher;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.functions.Action1;
import rx.functions.Func1;
import se.joelpet.android.toyredditreader.AbstractObserver;
import se.joelpet.android.toyredditreader.R;
import se.joelpet.android.toyredditreader.domain.AccessToken;
import se.joelpet.android.toyredditreader.domain.Me;
import se.joelpet.android.toyredditreader.fragments.LinkListingFragment;
import se.joelpet.android.toyredditreader.storage.LocalDataStore;
import timber.log.Timber;

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

    @InjectView(R.id.account_toggle_arrow)
    protected ViewSwitcher mAccountToggleArrowSwitcher;

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

        addSubscription(bindToActivity(mLocalDataStore.observeMe()).subscribe(new MeObserver()));
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

    @OnClick(R.id.account_drop_down_arrow)
    protected void onAccountDropDownArrowClick(View view) {
        mNavigationView.getMenu().setGroupVisible(R.id.main_group, false);
        mNavigationView.getMenu().setGroupVisible(R.id.account_group, false);
        mAccountToggleArrowSwitcher.showNext();

        addSubscription(bindToActivity(mLocalDataStore.getAccessToken()).first().map(new Func1<AccessToken, Boolean>() {
            @Override
            public Boolean call(AccessToken accessToken) {
                return true;
            }
        }).onErrorReturn(new Func1<Throwable, Boolean>() {
            @Override
            public Boolean call(Throwable throwable) {
                return false;
            }
        }).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean loggedIn) {
                mNavigationView.getMenu().setGroupVisible(R.id.account_group, true);
                mNavigationView.getMenu().findItem(R.id.navigation_log_in).setVisible
                        (!loggedIn);
                mNavigationView.getMenu().findItem(R.id.navigation_log_out).setVisible
                        (loggedIn);
            }
        }));
    }

    @OnClick(R.id.account_drop_up_arrow)
    protected void onAccountDropUpArrowClick(View view) {
        mNavigationView.getMenu().setGroupVisible(R.id.main_group, true);
        mNavigationView.getMenu().setGroupVisible(R.id.account_group, false);
        mAccountToggleArrowSwitcher.showNext();
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
            case R.id.navigation_log_in:
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
            default:
                return false;
        }
        mDrawerLayout.closeDrawers();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        menuItem.setChecked(true);
        return true;
    }

    private CharSequence getFormattedAccountAge(Me me) {
        Period accountPeriod = new Period(me.getCreationDateTime(),
                DateTime.now(DateTimeZone.UTC)).normalizedStandard();
        int accountPeriodYears = accountPeriod.getYears();
        int accountPeriodMonths = accountPeriod.getMonths();

        String years = getResources().getQuantityString(R.plurals.years,
                accountPeriodYears, accountPeriodYears);
        String months = getResources().getQuantityString(R.plurals.months,
                accountPeriodMonths, accountPeriodMonths);

        return getResources().getString(R.string.redditor_for_years_months, years, months);
    }

    /**
     * MeObserver handles updates to the locally stored Me object.
     */
    private class MeObserver extends AbstractObserver<Me> {

        @Override
        public void onNext(Me me) {
            Timber.d("Refreshing view with new Me object: %s", me);
            mUserNameView.setText(me.getName());
            mUserEmailView.setText(getFormattedAccountAge(me));
        }
    }
}
