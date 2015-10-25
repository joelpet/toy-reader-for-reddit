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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import se.joelpet.android.toyreaderforreddit.dagger.ForApplication;
import se.joelpet.android.toyreaderforreddit.domain.AccessToken;
import timber.log.Timber;

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
        }).observeOn(Schedulers.io()); // TODO: Really?
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

    public Observable<Account> getAccount() {
        return Observable.from(mAccountManager.getAccountsByType(mAccountType)).single();
    }

    public Observable<String> getAuthToken() {
        return Observable.create(new Observable.OnSubscribe<Bundle>() {
            @Override
            public void call(final Subscriber<? super Bundle> subscriber) {
                // TODO: Reuse getAccount() method
                Account[] accountsByType = mAccountManager.getAccountsByType(mAccountType);
                mAccountManager.getAuthToken(accountsByType[0], mAuthTokenType, null, true, new
                        AccountManagerCallback<Bundle>() {
                            @Override
                            public void run(AccountManagerFuture<Bundle> future) {
                                try {
                                    // FIXME: Too much wrapping going on here (or perhaps
                                    // when adding, which might be the reason I have to add
                                    // explicitly)
                                    Bundle result = future.getResult();
                                    subscriber.onNext(result);
                                } catch (OperationCanceledException | IOException |
                                        AuthenticatorException e) {
                                    subscriber.onError(e);
                                }
                            }
                        }, null);
            }
        }).doOnNext(new Action1<Bundle>() {
            @Override
            public void call(Bundle bundle) {
                Timber.d("Got bundle from AccountManager: %s", bundle);
            }
        }).map(new Func1<Bundle, String>() {
            @Override
            public String call(Bundle bundle) {
                return bundle.getString(AccountManager.KEY_AUTHTOKEN);
            }
        }).doOnNext(new Action1<String>() {
            @Override
            public void call(String authToken) {
                Timber.d("Extracted auth token: %s", authToken);
            }
        });
    }

    public boolean addAccountExplicitly(Account account, String password, Bundle userdata) {
        return mAccountManager.addAccountExplicitly(account, password, userdata);
    }

    public void setAuthToken(Account account, String authToken) {
        mAccountManager.setAuthToken(account, mAuthTokenType, authToken);
    }

    public Observable<Account> removeAccount() {
        return getAccount().doOnNext(new Action1<Account>() {
            @Override
            public void call(Account account) {
                removeAccount(account);
            }
        });
    }

    public void removeAccount(Account account) {
        // TODO: Add callback and create Observable result
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mAccountManager.removeAccount(account, null, null, null);
        } else {
            mAccountManager.removeAccount(account, null, null);
        }
    }

    public static class AddAccountResult {
        private final Bundle result;

        public AddAccountResult(@NonNull Bundle result) {
            this.result = result;
        }

        @Nullable
        public String getName() {
            return result.getString(AccountManager.KEY_ACCOUNT_NAME);
        }

        @Nullable
        public String getAccountType() {
            return result.getString(AccountManager.KEY_ACCOUNT_TYPE);
        }

        @Nullable
        public String getAuthToken() {
            return result.getString(AccountManager.KEY_AUTHTOKEN);
        }

        @Nullable
        public AccessToken getAccessToken() {
            Bundle userdata = result.getBundle(AccountManager.KEY_USERDATA);
            return (AccessToken) userdata.getSerializable("access_token");
        }

        @Override
        public String toString() {
            return "AddAccountResult{" +
                    "result=" + result +
                    '}';
        }
    }

}
