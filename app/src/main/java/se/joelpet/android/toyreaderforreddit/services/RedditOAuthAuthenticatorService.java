package se.joelpet.android.toyreaderforreddit.services;

import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import se.joelpet.android.toyreaderforreddit.AccountAuthenticator;

public class RedditOAuthAuthenticatorService extends Service {

    private AccountAuthenticator mAccountAuthenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        mAccountAuthenticator = new AccountAuthenticator(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (AccountManager.ACTION_AUTHENTICATOR_INTENT.equals(intent.getAction())) {
            return mAccountAuthenticator.getIBinder();
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAccountAuthenticator = null;
    }
}
