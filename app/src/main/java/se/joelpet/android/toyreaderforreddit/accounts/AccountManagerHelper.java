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
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import se.joelpet.android.toyreaderforreddit.dagger.ForApplication;
import se.joelpet.android.toyreaderforreddit.domain.AccessToken;
import se.joelpet.android.toyreaderforreddit.rx.transformers.CacheAndSubscribeTransformer;
import se.joelpet.android.toyreaderforreddit.rx.transformers.WorkOnIoAndOnNotifyOnMainTransformer;
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
        return Observable.from(mAccountManager.getAccountsByType(mAccountType)).take(1);
    }

    public Observable<String> getAuthToken() {
        return getAccount().flatMap(new Func1<Account, Observable<String>>() {
            @Override
            public Observable<String> call(Account account) {
                return getAuthTokenForAccount(account);
            }
        });
    }

    private Observable<String> getAuthTokenForAccount(final Account account) {
        return Observable.create(new Observable.OnSubscribe<Bundle>() {
            @Override
            public void call(final Subscriber<? super Bundle> subscriber) {
                mAccountManager.getAuthToken(account, mAuthTokenType, null, true, new
                        AccountManagerCallback<Bundle>() {
                            @Override
                            public void run(AccountManagerFuture<Bundle> future) {
                                try {
                                    Bundle result = future.getResult();
                                    subscriber.onNext(result);
                                    subscriber.onCompleted();
                                } catch (OperationCanceledException | IOException |
                                        AuthenticatorException e) {
                                    subscriber.onError(e);
                                }
                            }
                        }, null);
            }
        }).map(new Func1<Bundle, String>() {
            @Override
            public String call(Bundle bundle) {
                return bundle.getString(AccountManager.KEY_AUTHTOKEN);
            }
        }).compose(WorkOnIoAndOnNotifyOnMainTransformer.<String>getInstance());
    }

    public boolean addAccountExplicitly(Account account, String password, Bundle userdata) {
        return mAccountManager.addAccountExplicitly(account, password, userdata);
    }

    // TODO: Rename to match Reddit API naming, i.e. setAccessToken(Value/String/...)
    public Observable<Void> setAuthToken(final String authToken) {
        return getAccount()
                .single()
                .flatMap(new Func1<Account, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(Account account) {
                        setAuthToken(account, authToken);
                        return Observable.empty();
                    }
                }).compose(CacheAndSubscribeTransformer.<Void>getInstance());
    }

    public void setAuthToken(Account account, String authToken) {
        mAccountManager.setAuthToken(account, mAuthTokenType, authToken);
    }

    public Observable<Void> invalidateAuthToken() {
        return getAuthToken()
                .flatMap(new Func1<String, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(String authToken) {
                        Timber.d("Invalidating auth token: %s", authToken);
                        mAccountManager.invalidateAuthToken(mAccountType, authToken);
                        return Observable.empty();
                    }
                }).compose(CacheAndSubscribeTransformer.<Void>getInstance());
    }

    public Observable<Account> removeAccount() {
        return getAccount().flatMap(new Func1<Account, Observable<Account>>() {
            @Override
            public Observable<Account> call(Account account) {
                return removeAccount(account);
            }
        });
    }

    public Observable<Account> removeAccount(final Account account) {
        return Observable.create(new Observable.OnSubscribe<Account>() {
            @Override
            public void call(final Subscriber<? super Account> subscriber) {
                AccountManagerCallback callback = new AccountManagerCallback() {
                    @Override
                    public void run(AccountManagerFuture future) {
                        try {
                            future.getResult();
                            subscriber.onNext(account);
                            subscriber.onCompleted();
                        } catch (OperationCanceledException | IOException |
                                AuthenticatorException e) {
                            subscriber.onError(e);
                        }
                    }
                };

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    mAccountManager.removeAccount(account, null, callback, null);
                } else {
                    mAccountManager.removeAccount(account, callback, null);
                }
            }
        }).compose(CacheAndSubscribeTransformer.<Account>getInstance());
    }

    public void setRefreshToken(Account account, String refreshToken) {
        mAccountManager.setPassword(account, refreshToken);
    }

    public String getRefreshToken(Account account) {
        return mAccountManager.getPassword(account);
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
