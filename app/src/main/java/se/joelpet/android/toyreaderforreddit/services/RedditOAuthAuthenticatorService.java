package se.joelpet.android.toyreaderforreddit.services;

import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import javax.inject.Inject;

import se.joelpet.android.toyreaderforreddit.RedditApplication;
import se.joelpet.android.toyreaderforreddit.accounts.AccountAuthenticator;

public class RedditOAuthAuthenticatorService extends Service {

    @Inject
    protected AccountAuthenticator mAccountAuthenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        ((RedditApplication) getApplication()).getApplicationGraph().inject(this);
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
