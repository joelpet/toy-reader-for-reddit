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
import se.joelpet.android.toyreaderforreddit.model.AccessToken;
import se.joelpet.android.toyreaderforreddit.net.BaseRedditApi;
import se.joelpet.android.toyreaderforreddit.ui.login.LoginActivity;
import timber.log.Timber;

import static se.joelpet.android.toyreaderforreddit.accounts.AccountManagerHelper.sanitizeResult;

public class AccountAuthenticator extends AbstractAccountAuthenticator {

    public static final String AUTH_TOKEN_TYPE_DEFAULT = "default";

    private Context mContext;
    private AccountManagerHelper mAccountManagerHelper;
    private BaseRedditApi mRedditApi;

    public static String getAccountType(Context context) {
        return context.getString(R.string.authenticator_account_type);
    }

    @Inject
    public AccountAuthenticator(Context context, AccountManagerHelper accountManagerHelper,
                                BaseRedditApi baseRedditApi) {
        super(context);
        mContext = context;
        mAccountManagerHelper = accountManagerHelper;
        mRedditApi = baseRedditApi;
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

        String authToken = mAccountManagerHelper.peekAuthToken(account, authTokenType);

        if (!TextUtils.isEmpty(authToken)) {
            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            Timber.d("Cached auth token result: %s", sanitizeResult(result));
            return result;
        }

        String refreshToken = mAccountManagerHelper.getRefreshToken(account);

        if (refreshToken != null) {
            Timber.d("Attempt to refresh token for %s", account);
            mRedditApi.refreshAccessToken(refreshToken, this).first()
                    .subscribe(new Action1<AccessToken>() {
                        @Override
                        public void call(AccessToken accessToken) {
                            Bundle result = mAccountManagerHelper.createAddAccountResultIntent(
                                    accessToken, account.name).getExtras();
                            Timber.d("Access token refresh result: %s", sanitizeResult(result));
                            response.onResult(result);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Timber.i(throwable, "Access token refresh failed");
                            response.onError(0, throwable.getMessage());
                        }
                    });
            return null;
        }

        Timber.d("Falling back to adding account");
        return addAccount(response, getAccountType(mContext), authTokenType, null, null);
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
