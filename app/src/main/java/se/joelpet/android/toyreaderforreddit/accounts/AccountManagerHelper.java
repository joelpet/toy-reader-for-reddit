package se.joelpet.android.toyreaderforreddit.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;

import java.io.IOException;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

public class AccountManagerHelper {

    @Inject
    protected AccountManager mAccountManager;

    public AccountManagerHelper(AccountManager accountManager) {
        mAccountManager = accountManager;
    }

    public Observable<Bundle> addAccount(final Activity activity) {
        return Observable.create(new Observable.OnSubscribe<Bundle>() {
            @Override
            public void call(final Subscriber<? super Bundle> subscriber) {
                String accountType = AccountAuthenticator.getAccountType(activity);
                String authTokenType = AccountAuthenticator.AUTH_TOKEN_TYPE_DEFAULT;

                mAccountManager.addAccount(accountType, authTokenType, null, null, activity, new
                        AccountManagerCallback<Bundle>() {
                            @Override
                            public void run(AccountManagerFuture<Bundle> future) {
                                try {
                                    Bundle result = future.getResult();
                                    subscriber.onNext(result);
                                } catch (OperationCanceledException | IOException |
                                        AuthenticatorException e) {
                                    subscriber.onError(e);
                                }
                            }
                        }, null);
            }
        });
    }

    public String peekAuthToken(Account account, String authTokenType) {
        return mAccountManager.peekAuthToken(account, authTokenType);
    }
}
