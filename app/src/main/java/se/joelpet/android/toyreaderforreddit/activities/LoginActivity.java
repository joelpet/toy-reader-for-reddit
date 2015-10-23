package se.joelpet.android.toyreaderforreddit.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

import java.util.UUID;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import se.joelpet.android.toyreaderforreddit.AppConnectWebViewClient;
import se.joelpet.android.toyreaderforreddit.R;
import se.joelpet.android.toyreaderforreddit.accounts.AccountManagerHelper;
import se.joelpet.android.toyreaderforreddit.domain.AccessToken;
import se.joelpet.android.toyreaderforreddit.domain.Me;
import se.joelpet.android.toyreaderforreddit.net.RedditApi;
import se.joelpet.android.toyreaderforreddit.storage.LocalDataStore;
import se.joelpet.android.toyreaderforreddit.volley.AccessTokenRequest;
import se.joelpet.android.toyreaderforreddit.volley.BaseRequest;
import timber.log.Timber;

public class LoginActivity extends AppCompatAccountAuthenticatorActivity
        implements AppConnectWebViewClient.OnAppConnectListener {

    public static final String TAG = LoginActivity.class.getName();

    public static final String BASE_URL_AUTH = "https://www.reddit.com/api/v1/authorize.compact";

    @InjectView(R.id.toolbar)
    protected Toolbar mToolbar;

    @InjectView(R.id.web_view)
    protected WebView mWebView;

    @Inject
    AccountManagerHelper mAccountManagerHelper;

    @Inject
    protected RedditApi mRedditApi;

    @Inject
    protected AppConnectWebViewClient mAppConnectWebViewClient;

    @Inject
    protected LocalDataStore mLocalDataStore;

    private String mUniqueAuthState;

    private Subscription mSubscription;

    public static Intent createIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);

        mAppConnectWebViewClient.setOnAppConnectListener(this);
        mWebView.setWebViewClient(mAppConnectWebViewClient);
        mWebView.getSettings().setJavaScriptEnabled(true);

        if (savedInstanceState == null) {
            Uri.Builder uri = Uri.parse(BASE_URL_AUTH).buildUpon();
            mUniqueAuthState = UUID.randomUUID().toString();
            uri.appendQueryParameter("client_id", BaseRequest.CLIENT_ID);
            uri.appendQueryParameter("response_type", "code");
            uri.appendQueryParameter("state", mUniqueAuthState);
            uri.appendQueryParameter("redirect_uri", AccessTokenRequest.AUTH_REDIRECT_URI);
            uri.appendQueryParameter("duration", "permanent");
            uri.appendQueryParameter("scope", "read,identity");

            mWebView.loadUrl(uri.toString());
        }
    }

    @Override
    public void onDestroy() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        super.onDestroy();
    }

    @Override
    public void onAllowConnect(final String authCode, String state) {
        if (!mUniqueAuthState.equals(state)) {
            Timber.w("Aborting unrecognized auth request with state: %s", state);
            return;
        }

        // store auth code
        mLocalDataStore.putAuthCode(authCode)
                // request Me object from Reddit API
                .flatMap(new Func1<String, Observable<AccessToken>>() {
                    @Override
                    public Observable<AccessToken> call(String authCode) {
                        return mRedditApi.getAccessToken(authCode, TAG);
                    }
                }) // cache token to avoid multiple API requests
                .compose(new Observable.Transformer<AccessToken, AccessToken>() {
                    @Override
                    public Observable<AccessToken> call(Observable<AccessToken> observable) {
                        return observable.cache(1);
                    }
                }) // store access token
                .flatMap(new Func1<AccessToken, Observable<AccessToken>>() {
                    @Override
                    public Observable<AccessToken> call(AccessToken accessToken) {
                        Timber.d("Storing access token: %s", accessToken);
                        return mLocalDataStore.putAccessToken(accessToken);
                    }
                })
                .compose(new Observable.Transformer<AccessToken, Intent>() {
                    @Override
                    public Observable<Intent> call(Observable<AccessToken> tokenObservable) {
                        return Observable.zip(
                                tokenObservable,
                                // get Me object from Reddit API and store it
                                tokenObservable.flatMap(new Func1<AccessToken, Observable<Me>>() {
                                    @Override
                                    public Observable<Me> call(AccessToken accessToken) {
                                        return mRedditApi.getMe(TAG);
                                    }
                                }).flatMap(new Func1<Me, Observable<Me>>() {
                                    @Override
                                    public Observable<Me> call(Me me) {
                                        return mLocalDataStore.putMe(me);
                                    }
                                }),
                                // create result intent
                                new Func2<AccessToken, Me, Intent>() {
                                    @Override
                                    public Intent call(AccessToken accessToken, Me me) {
                                        return mAccountManagerHelper
                                                .createAddAccountResultIntent
                                                        (accessToken, me.getName());
                                    }
                                });
                    }
                }) // finish with result intent
                .subscribe(new Action1<Intent>() {
                    @Override
                    public void call(Intent intent) {
                        setAccountAuthenticatorResult(intent.getExtras());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.e(throwable, "Request failed");
                        finish();
                    }
                });
    }

    @Override
    public void onErrorConnect(String error) {
        // TODO: Improve error handling
        Timber.d("onErrorConnect(%s)", error);
        switch (error) {
            case "access_denied":
                // Fail gracefully - let the user know you cannot continue, and be respectful of
                // their choice to decline to use your app
                Timber.d("User chose not to grant your app permissions");
                break;
            case "unsupported_response_type":
                // Ensure that the response_type parameter is one of the allowed values
                Timber.d("Invalid response_type parameter in initial Authorization");
                break;
            case "invalid_scope":
                // Ensure that the scope parameter is a comma-separated list of valid scopes
                Timber.d("Invalid scope parameter in initial Authorization");
                break;
            case "invalid_request":
                // Double check the parameters being sent during the request to /api/v1/authorize
                // above.
                Timber.d("There was an issue with the request sent to /api/v1/authorize ");
                break;
        }
        finish();
    }
}
