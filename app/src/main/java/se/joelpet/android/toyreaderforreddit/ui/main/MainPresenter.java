package se.joelpet.android.toyreaderforreddit.ui.main;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import se.joelpet.android.toyreaderforreddit.AbstractObserver;
import se.joelpet.android.toyreaderforreddit.accounts.AccountManagerHelper;
import se.joelpet.android.toyreaderforreddit.accounts.AddAccountResult;
import se.joelpet.android.toyreaderforreddit.domain.Me;
import se.joelpet.android.toyreaderforreddit.rx.transformers.WorkOnIoAndOnNotifyOnMainTransformer;
import se.joelpet.android.toyreaderforreddit.storage.LocalDataStore;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

public class MainPresenter implements MainContract.Presenter {

    private final MainContract.View mainView;
    private final AccountManagerHelper accountManagerHelper;
    private final LocalDataStore localDataStore;
    private final CompositeSubscription subscriptions;

    private boolean firstRun = true;

    public MainPresenter(@NonNull MainContract.View mainView,
                         @NonNull AccountManagerHelper accountManagerHelper,
                         @NonNull LocalDataStore localDataStore) {
        this.mainView = checkNotNull(mainView);
        this.accountManagerHelper = checkNotNull(accountManagerHelper);
        this.localDataStore = checkNotNull(localDataStore);
        this.subscriptions = new CompositeSubscription();
        this.mainView.setPresenter(this);
    }

    @Override
    public void start() {
        if (firstRun) {
            mainView.showEverythingLinkListingUi();
            firstRun = false;
        }

        subscriptions.add(localDataStore
                .observeMe()
                .compose(WorkOnIoAndOnNotifyOnMainTransformer.<Me>getInstance())
                .subscribe(new MeObserver()));
    }

    @Override
    public void openEverythingLinkListing() {
        mainView.setToolbarTitle("Everything");
        mainView.setToolbarSubtitle("from all subreddits");
        mainView.showEverythingLinkListingUi();
    }

    @Override
    public void openSubscribedLinkListing() {
        mainView.setToolbarTitle("Subscribed");
        mainView.setToolbarSubtitle("your frontpage");
        mainView.showSubscribedLinkListingUi();
    }

    @Override
    public void addAccount(Activity activity) {
        subscriptions.add(accountManagerHelper.addAccount(activity).subscribe(
                new Action1<AddAccountResult>() {
                    @Override
                    public void call(AddAccountResult result) {
                        Timber.d("Result: %s", result);
                        mainView.showDefaultMenu();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof OperationCanceledException) {
                            mainView.showSignInCanceledMessage();
                        }
                    }
                }
        ));
    }

    @Override
    public void removeAccount() {
        subscriptions.add(accountManagerHelper.removeAccount().subscribe());
        mainView.showSigningOutMessage();
    }

    @Override
    public void openAccountsMenu() {
        mainView.hideMenu();

        subscriptions.add(accountManagerHelper
                .getAccount()
                .isEmpty()
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean isNoAccountAvailable) {
                        boolean signedIn = !isNoAccountAvailable;
                        mainView.showAccountMenu(signedIn);
                    }
                }));
    }

    @Override
    public void openDefaultMenu() {
        mainView.showDefaultMenu();
    }


    private class MeObserver extends AbstractObserver<Me> {
        @Override
        public void onNext(Me me) {
            Timber.d("Refreshing view with new Me object: %s", me);
            boolean signedIn = me != null;

            if (signedIn) {
                Period accountPeriod = new Period(me.getCreationDateTime(),
                        DateTime.now(DateTimeZone.UTC)).normalizedStandard();
                mainView.showUserDetails(me.getName(), accountPeriod);
            } else {
                mainView.showUnauthenticatedUserDetails();
            }

            if (mainView.isAccountMenuShown()) {
                mainView.showAccountMenu(signedIn);
            }
        }
    }
}
