package se.joelpet.android.toyreaderforreddit.services;

import android.app.IntentService;
import android.content.Intent;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import se.joelpet.android.toyreaderforreddit.RedditApplication;
import se.joelpet.android.toyreaderforreddit.accounts.AccountManagerHelper;
import se.joelpet.android.toyreaderforreddit.storage.LocalDataStore;

public class LoginAccountsUpdatedIntentService extends IntentService {

    @Inject
    protected AccountManagerHelper mAccountManagerHelper;

    @Inject
    protected LocalDataStore mLocalDataStore;

    public LoginAccountsUpdatedIntentService() {
        super(LoginAccountsUpdatedIntentService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((RedditApplication) getApplication()).getApplicationGraph().inject(this);
    }

    @Override
    public void onDestroy() {
        mAccountManagerHelper = null;
        mLocalDataStore = null;
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        deleteUserDetailsIfAccountIsMissing();
    }

    private void deleteUserDetailsIfAccountIsMissing() {
        mAccountManagerHelper.getAccount()
                .isEmpty()
                .flatMap(new Func1<Boolean, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(Boolean noAccountAvailable) {
                        if (noAccountAvailable) {
                            return mLocalDataStore.deleteMe();
                        } else {
                            return Observable.empty();
                        }
                    }
                }).subscribe();
    }
}
