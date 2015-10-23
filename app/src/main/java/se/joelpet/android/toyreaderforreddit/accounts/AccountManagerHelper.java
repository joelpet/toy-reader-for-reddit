package se.joelpet.android.toyreaderforreddit.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.io.IOException;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import se.joelpet.android.toyreaderforreddit.dagger.ForApplication;
import se.joelpet.android.toyreaderforreddit.domain.AccessToken;

public class AccountManagerHelper {

    @Inject
    @ForApplication
    protected Context mContext;

    @Inject
    protected AccountManager mAccountManager;

    private final String mAccountType;
    private final String mAuthTokenType;

    public AccountManagerHelper(Context context, AccountManager accountManager) {
        mContext = context;
        mAccountManager = accountManager;
        mAccountType = AccountAuthenticator.getAccountType(context);
        mAuthTokenType = AccountAuthenticator.AUTH_TOKEN_TYPE_DEFAULT;
    }

    public Observable<AddAccountResult> addAccount(final Activity activity) {
        return Observable.create(new Observable.OnSubscribe<AddAccountResult>() {
            @Override
            public void call(final Subscriber<? super AddAccountResult> subscriber) {
                mAccountManager.addAccount(mAccountType, mAuthTokenType, null, null, activity, new
                        AccountManagerCallback<Bundle>() {
                            @Override
                            public void run(AccountManagerFuture<Bundle> future) {
                                try {
                                    Bundle result = future.getResult();
                                    subscriber.onNext(new AddAccountResult(result));
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

    public Intent createAddAccountResultIntent(AccessToken accessToken, String accountName) {
        Intent result = new Intent();

        result.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName);
        result.putExtra(AccountManager.KEY_ACCOUNT_TYPE, mAccountType);
        result.putExtra(AccountManager.KEY_AUTHTOKEN, accessToken.getAccessToken());

        Bundle userData = new Bundle();
        userData.putSerializable("access_token", accessToken);

        result.putExtra(AccountManager.KEY_USERDATA, userData);

        return result;
    }

    public static class AddAccountResult {
        private final Bundle result;

        public AddAccountResult(@NonNull Bundle result) {
            this.result = result;
        }

        public AccessToken getAccessToken() {
            Bundle userdata = result.getBundle(AccountManager.KEY_USERDATA);
            return (AccessToken) userdata.getSerializable("access_token");
        }
    }

}
