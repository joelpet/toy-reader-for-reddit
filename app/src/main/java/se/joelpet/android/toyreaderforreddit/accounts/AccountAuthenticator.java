package se.joelpet.android.toyreaderforreddit.accounts;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import javax.inject.Inject;

import rx.functions.Action1;
import se.joelpet.android.toyreaderforreddit.R;
import se.joelpet.android.toyreaderforreddit.activities.LoginActivity;
import se.joelpet.android.toyreaderforreddit.domain.AccessToken;
import se.joelpet.android.toyreaderforreddit.net.RedditApi;
import timber.log.Timber;

public class AccountAuthenticator extends AbstractAccountAuthenticator {

    public static final String AUTH_TOKEN_TYPE_DEFAULT = "default";

    private Context mContext;
    private AccountManagerHelper mAccountManagerHelper;
    private RedditApi mRedditApi;

    public static String getAccountType(Context context) {
        return context.getString(R.string.authenticator_account_type);
    }

    @Inject
    public AccountAuthenticator(Context context, AccountManagerHelper accountManagerHelper,
                                RedditApi redditApi) {
        super(context);
        mContext = context;
        mAccountManagerHelper = accountManagerHelper;
        mRedditApi = redditApi;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
                             String authTokenType, String[] requiredFeatures, Bundle options)
            throws NetworkErrorException {
        Timber.d("addAccount(_, %s, %s, _, _)", accountType, authTokenType);

        Intent intent = LoginActivity.createIntent(mContext);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);

        return bundle;
    }

    @Override
    public Bundle getAuthToken(final AccountAuthenticatorResponse response, final Account account,
                               String authTokenType, Bundle options) throws NetworkErrorException {
        Timber.d("getAuthToken(_, %s, %s, _)", account, authTokenType);

        // TODO: Throw this out? When would it ever work?
        String authToken = mAccountManagerHelper.peekAuthToken(account, authTokenType);
        if (!TextUtils.isEmpty(authToken)) {
            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            Timber.d("Returning peeked auth token: %s", result);
            return result;
        }

        String refreshToken = mAccountManagerHelper.getRefreshToken(account);

        if (refreshToken != null) {
            Timber.d("Attempt to refresh token using: %s", refreshToken);
            mRedditApi.refreshAccessToken(refreshToken, this).first()
                    .subscribe(new Action1<AccessToken>() {
                        @Override
                        public void call(AccessToken accessToken) {
                            Timber.d("Refreshed access token: %s", accessToken);
                            Bundle result = mAccountManagerHelper.createAddAccountResultIntent(
                                    accessToken, account.name).getExtras();
                            Timber.v("Result bundle handed to response callback: %s", result);
                            response.onResult(result);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            response.onError(0, throwable.getMessage());
                        }
                    });
            return null;
        }

        Intent intent = LoginActivity.createIntent(mContext);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);

        Timber.d("Fall back to returning intent: %s", intent);

        return bundle;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
                                     Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
                                    String authTokenType, Bundle options) throws
            NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
                              String[] features) throws NetworkErrorException {
        return null;
    }
}
