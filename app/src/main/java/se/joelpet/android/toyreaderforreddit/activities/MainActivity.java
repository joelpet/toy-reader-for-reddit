package se.joelpet.android.toyreaderforreddit.activities;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

import android.accounts.OperationCanceledException;
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
import android.widget.Toast;
import android.widget.ViewSwitcher;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;
import rx.android.observables.AndroidObservable;
import rx.functions.Action1;
import se.joelpet.android.toyreaderforreddit.AbstractObserver;
import se.joelpet.android.toyreaderforreddit.R;
import se.joelpet.android.toyreaderforreddit.accounts.AccountManagerHelper;
import se.joelpet.android.toyreaderforreddit.accounts.AddAccountResult;
import se.joelpet.android.toyreaderforreddit.domain.Me;
import se.joelpet.android.toyreaderforreddit.fragments.LinkListingFragment;
import se.joelpet.android.toyreaderforreddit.storage.LocalDataStore;
import timber.log.Timber;

public class MainActivity extends BaseActivity implements NavigationView
        .OnNavigationItemSelectedListener {

    public static final int DRAWER_GRAVITY = GravityCompat.START;
    public static final int ACCOUNT_TOGGLE_ARROW_CHILD_DROP_DOWN = 0;
    public static final int ACCOUNT_TOGGLE_ARROW_CHILD_DROP_UP = 1;

    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;

    @Bind(R.id.drawer_layout)
    protected DrawerLayout mDrawerLayout;

    @Bind(R.id.navigation_view)
    protected NavigationView mNavigationView;

    @Bind(R.id.user_name)
    protected TextView mUserNameView;

    @Bind(R.id.user_email)
    protected TextView mUserEmailView;

    @Bind(R.id.account_toggle_arrow)
    protected ViewSwitcher mAccountToggleArrowSwitcher;

    private ActionBarDrawerToggle mDrawerToggle;

    @Inject
    protected LocalDataStore mLocalDataStore;

    @Inject
    protected AccountManagerHelper mAccountManagerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // FIXME: This is not used, or at least should not be used.
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

    @OnClick(R.id.navigation_header_root)
    protected void onNavigationHeaderRootClick(View view) {
        View currentToggleArrowSwitcherView = mAccountToggleArrowSwitcher.getCurrentView();
        boolean accountDropDownArrowDisplayed = currentToggleArrowSwitcherView.getId() == R.id
                .account_drop_down_arrow;
        if (accountDropDownArrowDisplayed) {
            onAccountDropDownArrowClick(currentToggleArrowSwitcherView);
        } else {
            onAccountDropUpArrowClick(currentToggleArrowSwitcherView);
        }
    }

    @OnClick(R.id.account_drop_down_arrow)
    protected void onAccountDropDownArrowClick(View view) {
        switchToAccountMenuModeInDrawerWithOptionsHidden();
        addSubscription(bindToActivity(mAccountManagerHelper
                .getAccount()
                .isEmpty())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean isNoAccountAvailable) {
                        boolean signedIn = !isNoAccountAvailable;
                        switchToAccountMenuModeInDrawerAs(signedIn);
                    }
                }));
    }

    @OnClick(R.id.account_drop_up_arrow)
    protected void onAccountDropUpArrowClick(View view) {
        switchToDefaultMenuModeInDrawer();
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
            case R.id.navigation_sign_in:
                addAccountUsingAccountManager();
                return true;
            case R.id.navigation_sign_out:
                removeAccountUsingAccountManager();
                return false;
            default:
                return false;
        }
        mDrawerLayout.closeDrawers();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        menuItem.setChecked(true);
        return true;
    }

    private void switchToDefaultMenuModeInDrawer() {
        setDefaultGroupInDrawerMenuVisible(true);
        setAccountGroupInDrawerMenuVisible(false);
        displayAccountToggleDropDownArrow();
    }

    private void switchToAccountMenuModeInDrawerWithOptionsHidden() {
        setDefaultGroupInDrawerMenuVisible(false);
        setAccountGroupInDrawerMenuVisible(false);
        displayAccountToggleDropUpArrow();
    }

    private void switchToAccountMenuModeInDrawerAs(boolean signedIn) {
        setDefaultGroupInDrawerMenuVisible(false);
        setAccountGroupInDrawerMenuVisible(true);
        displayAccountToggleDropUpArrow();
        displayAccountMenuOptionsAs(signedIn);
    }

    private void displayAccountToggleDropDownArrow() {
        mAccountToggleArrowSwitcher.setDisplayedChild(ACCOUNT_TOGGLE_ARROW_CHILD_DROP_DOWN);
    }

    private void displayAccountToggleDropUpArrow() {
        mAccountToggleArrowSwitcher.setDisplayedChild(ACCOUNT_TOGGLE_ARROW_CHILD_DROP_UP);
    }

    private void setAccountGroupInDrawerMenuVisible(boolean visible) {
        mNavigationView.getMenu().setGroupVisible(R.id.account_group, visible);
    }

    private void setDefaultGroupInDrawerMenuVisible(boolean visible) {
        mNavigationView.getMenu().setGroupVisible(R.id.default_group, visible);
    }

    private void displayAccountMenuOptionsAs(boolean signedIn) {
        mNavigationView.getMenu().findItem(R.id.navigation_sign_in).setVisible(!signedIn);
        mNavigationView.getMenu().findItem(R.id.navigation_sign_out).setVisible(signedIn);
    }

    private boolean isAccountMenuOptionsDisplayed() {
        return mNavigationView.getMenu().findItem(R.id.navigation_sign_in).isVisible() ||
                mNavigationView.getMenu().findItem(R.id.navigation_sign_out).isVisible();
    }

    private void addAccountUsingAccountManager() {
        addSubscription(AndroidObservable.bindActivity(this,
                mAccountManagerHelper.addAccount(this)).subscribe(
                new Action1<AddAccountResult>() {
                    @Override
                    public void call(AddAccountResult result) {
                        Timber.d("Result: %s", result);
                        switchToDefaultMenuModeInDrawer();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof OperationCanceledException) {
                            Toast.makeText(MainActivity.this, R.string.toast_sign_in_canceled,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ));
    }

    private void removeAccountUsingAccountManager() {
        Toast.makeText(this, R.string.toast_signing_out, Toast.LENGTH_SHORT).show();

        addSubscription(AndroidObservable.bindActivity(this,
                mAccountManagerHelper.removeAccount())
                .subscribe());
    }

    // TODO: Move to text.format.MeFormat
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


    private class MeObserver extends AbstractObserver<Me> {

        @Override
        public void onNext(Me me) {
            Timber.d("Refreshing view with new Me object: %s", me);
            boolean signedIn = me != null;

            String userName = signedIn ? me.getName() : getString(R.string
                    .navigation_header_user_name_unauthenticated);
            CharSequence userEmail = signedIn ? getFormattedAccountAge(me) : getString(R.string
                    .navigation_header_user_email_unauthenticated);

            mUserNameView.setText(userName);
            mUserEmailView.setText(userEmail);

            if (isAccountMenuOptionsDisplayed()) {
                displayAccountMenuOptionsAs(signedIn);
            }
        }
    }
}
