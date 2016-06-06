package se.joelpet.android.toyreaderforreddit.ui.main;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.joda.time.Period;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import se.joelpet.android.toyreaderforreddit.R;
import se.joelpet.android.toyreaderforreddit.accounts.AccountManagerHelper;
import se.joelpet.android.toyreaderforreddit.ui.BaseActivity;
import se.joelpet.android.toyreaderforreddit.net.OAuthRedditApi;
import se.joelpet.android.toyreaderforreddit.storage.LocalDataStore;
import se.joelpet.android.toyreaderforreddit.ui.linklisting.LinkListingFragment;
import se.joelpet.android.toyreaderforreddit.ui.linklisting.LinkListingPresenter;

public class MainActivity extends BaseActivity implements MainContract.View,
        NavigationView.OnNavigationItemSelectedListener {

    private static final int DRAWER_GRAVITY = GravityCompat.START;
    private static final int ACCOUNT_TOGGLE_ARROW_CHILD_DROP_DOWN = 0;
    private static final int ACCOUNT_TOGGLE_ARROW_CHILD_DROP_UP = 1;

    private static final String TAG_EVERYTHING = "everything";
    private static final String TAG_SUBSCRIBED = "subscribed";

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.drawer_layout)
    protected DrawerLayout drawerLayout;

    @BindView(R.id.navigation_view)
    protected NavigationView navigationView;

    protected class NavigationHeaderViews {
        @BindView(R.id.user_name)
        protected TextView userNameView;

        @BindView(R.id.user_email)
        protected TextView userEmailView;

        @BindView(R.id.account_toggle_arrow)
        protected ViewSwitcher accountToggleArrowSwitcher;
    }

    protected NavigationHeaderHolder navigationHeaderHolder = new NavigationHeaderHolder();

    private ActionBarDrawerToggle drawerToggle;

    @Inject
    protected LocalDataStore localDataStore;

    @Inject
    protected AccountManagerHelper accountManagerHelper;

    @Inject
    protected OAuthRedditApi oAuthRedditApi;

    private MainContract.Presenter mainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        ButterKnife.bind(navigationHeaderHolder, navigationView.getHeaderView(0));
        setSupportActionBar(toolbar);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, DRAWER_GRAVITY);
        drawerLayout.addDrawerListener(drawerToggle);
        navigationView.setNavigationItemSelectedListener(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        new MainPresenter(this, accountManagerHelper, localDataStore);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainPresenter.start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(DRAWER_GRAVITY)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        drawerLayout.removeDrawerListener(drawerToggle);
    }

    protected class NavigationHeaderHolder extends NavigationHeaderListeners {
    }

    protected class NavigationHeaderListeners extends NavigationHeaderViews {

        @OnClick(R.id.navigation_header_root)
        protected void onNavigationHeaderRootClick(View view) {
            View currentToggleArrowSwitcherView = accountToggleArrowSwitcher.getCurrentView();
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
            mainPresenter.openAccountsMenu();
        }

        @OnClick(R.id.account_drop_up_arrow)
        protected void onAccountDropUpArrowClick(View view) {
            mainPresenter.openDefaultMenu();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.navigation_everything:
                mainPresenter.openEverythingLinkListing();
                break;

            case R.id.navigation_subscribed:
                mainPresenter.openSubscribedLinkListing();
                break;

            case R.id.navigation_sign_in:
                mainPresenter.addAccount(this);
                return false;

            case R.id.navigation_sign_out:
                mainPresenter.removeAccount();
                return false;

            default:
                return false;
        }

        menuItem.setChecked(true);
        drawerLayout.closeDrawers();
        return true;
    }

    @Override
    public void showSignInCanceledMessage() {
        Toast.makeText(this, R.string.toast_sign_in_canceled, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSigningOutMessage() {
        Toast.makeText(this, R.string.toast_signing_out, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showDefaultMenu() {
        setDefaultGroupInDrawerMenuVisible(true);
        setAccountGroupInDrawerMenuVisible(false);
        displayAccountToggleDropDownArrow();
    }

    private void setDefaultGroupInDrawerMenuVisible(boolean visible) {
        navigationView.getMenu().setGroupVisible(R.id.default_group, visible);
    }

    private void setAccountGroupInDrawerMenuVisible(boolean visible) {
        navigationView.getMenu().setGroupVisible(R.id.account_group, visible);
    }

    private void displayAccountToggleDropDownArrow() {
        navigationHeaderHolder.accountToggleArrowSwitcher
                .setDisplayedChild(ACCOUNT_TOGGLE_ARROW_CHILD_DROP_DOWN);
    }

    @Override
    public void showAccountMenu(boolean signedIn) {
        setDefaultGroupInDrawerMenuVisible(false);
        setAccountGroupInDrawerMenuVisible(true);
        displayAccountToggleDropUpArrow();
        displayAccountMenuOptionsAs(signedIn);
    }

    private void displayAccountToggleDropUpArrow() {
        navigationHeaderHolder.accountToggleArrowSwitcher
                .setDisplayedChild(ACCOUNT_TOGGLE_ARROW_CHILD_DROP_UP);
    }

    private void displayAccountMenuOptionsAs(boolean signedIn) {
        navigationView.getMenu().findItem(R.id.navigation_sign_in).setVisible(!signedIn);
        navigationView.getMenu().findItem(R.id.navigation_sign_out).setVisible(signedIn);
    }

    @Override
    public boolean isAccountMenuShown() {
        return navigationView.getMenu().findItem(R.id.navigation_sign_in).isVisible() ||
                navigationView.getMenu().findItem(R.id.navigation_sign_out).isVisible();
    }

    @Override
    public void hideMenu() {
        setDefaultGroupInDrawerMenuVisible(false);
        setAccountGroupInDrawerMenuVisible(false);
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        this.mainPresenter = presenter;
    }

    @Override
    public void showUserDetails(String userName, Period accountPeriod) {
        navigationHeaderHolder.userNameView.setText(userName);
        navigationHeaderHolder.userEmailView.setText(getFormattedAccountAge(accountPeriod));
    }

    private CharSequence getFormattedAccountAge(@NonNull Period accountPeriod) {
        int accountPeriodYears = accountPeriod.getYears();
        int accountPeriodMonths = accountPeriod.getMonths();

        String years = getResources().getQuantityString(R.plurals.years,
                accountPeriodYears, accountPeriodYears);
        String months = getResources().getQuantityString(R.plurals.months,
                accountPeriodMonths, accountPeriodMonths);

        return getResources().getString(R.string.redditor_for_years_months, years, months);
    }

    @Override
    public void showUnauthenticatedUserDetails() {
        navigationHeaderHolder.userNameView.setText(
                R.string.navigation_header_user_name_unauthenticated);
        navigationHeaderHolder.userEmailView.setText(
                R.string.navigation_header_user_email_unauthenticated);
    }

    @Override
    public void showEverythingLinkListingUi() {
        LinkListingFragment fragment = (LinkListingFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_EVERYTHING);

        if (fragment == null) {
            fragment = LinkListingFragment.newInstance(
                    LinkListingFragment.ARG_LISTING_EVERYTHING,
                    LinkListingFragment.ARG_SORT_HOT);
        }

        new LinkListingPresenter(fragment, oAuthRedditApi, accountManagerHelper);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, TAG_EVERYTHING).commit();
    }

    @Override
    public void showSubscribedLinkListingUi() {
        LinkListingFragment fragment = (LinkListingFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_SUBSCRIBED);

        if (fragment == null) {
            fragment = LinkListingFragment.newInstance(
                    LinkListingFragment.ARG_LISTING_SUBSCRIBED,
                    LinkListingFragment.ARG_SORT_HOT);
        }

        new LinkListingPresenter(fragment, oAuthRedditApi, accountManagerHelper);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, TAG_SUBSCRIBED).commit();
    }

    @Override
    public void setToolbarTitle(CharSequence title) {
        toolbar.setTitle(title);
    }

    @Override
    public void setToolbarSubtitle(CharSequence subtitle) {
        toolbar.setSubtitle(subtitle);
    }
}
