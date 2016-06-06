package se.joelpet.android.toyreaderforreddit.ui.main;

import android.app.Activity;

import org.joda.time.Period;

import se.joelpet.android.toyreaderforreddit.ui.BasePresenter;
import se.joelpet.android.toyreaderforreddit.ui.BaseView;

public interface MainContract {

    interface View extends BaseView<Presenter> {

        void showUserDetails(String name, Period accountPeriod);

        void showUnauthenticatedUserDetails();

        void showEverythingLinkListingUi();

        void showSubscribedLinkListingUi();

        void setToolbarTitle(CharSequence title);

        void setToolbarSubtitle(CharSequence subtitle);

        void showSignInCanceledMessage();

        void showSigningOutMessage();

        void showDefaultMenu();

        void showAccountMenu(boolean signedIn);

        // FIXME: This is probably something the presenter should know itself
        boolean isAccountMenuShown();

        void hideMenu();
    }

    interface Presenter extends BasePresenter {

        void openEverythingLinkListing();

        void openSubscribedLinkListing();

        void addAccount(Activity activity);

        void removeAccount();

        void openAccountsMenu();

        void openDefaultMenu();
    }
}
