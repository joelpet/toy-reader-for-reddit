package se.joelpet.android.toyredditreader.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.widget.Toast;

import java.util.UUID;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.observables.AndroidObservable;
import rx.functions.Action1;
import rx.functions.Func1;
import se.joelpet.android.toyredditreader.AppConnectWebViewClient;
import se.joelpet.android.toyredditreader.Preferences;
import se.joelpet.android.toyredditreader.R;
import se.joelpet.android.toyredditreader.domain.AccessToken;
import se.joelpet.android.toyredditreader.domain.Me;
import se.joelpet.android.toyredditreader.net.RedditApi;
import se.joelpet.android.toyredditreader.volley.AccessTokenRequest;
import timber.log.Timber;

public class LoginActivity extends BaseActivity
        implements AppConnectWebViewClient.OnAppConnectListener {

    public static final String TAG = LoginActivity.class.getName();

    public static final String BASE_URL_AUTH = "https://www.reddit.com/api/v1/authorize.compact";

    @InjectView(R.id.toolbar)
    protected Toolbar mToolbar;

    @InjectView(R.id.web_view)
    protected WebView mWebView;

    private String mUniqueAuthState;

    @Inject
    protected RedditApi mRedditApi;

    @Inject
    protected AppConnectWebViewClient mAppConnectWebViewClient;

    @Inject
    protected Preferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inject(this);
        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);

        mAppConnectWebViewClient.setOnAppConnectListener(this);
        mWebView.setWebViewClient(mAppConnectWebViewClient);
        mWebView.getSettings().setJavaScriptEnabled(true);

        if (savedInstanceState == null) {
            Uri.Builder uri = Uri.parse(BASE_URL_AUTH).buildUpon();
            mUniqueAuthState = UUID.randomUUID().toString();
            uri.appendQueryParameter("client_id", AccessTokenRequest.CLIENT_ID);
            uri.appendQueryParameter("response_type", "code");
            uri.appendQueryParameter("state", mUniqueAuthState);
            uri.appendQueryParameter("redirect_uri", AccessTokenRequest.AUTH_REDIRECT_URI);
            uri.appendQueryParameter("duration", "permanent");
            uri.appendQueryParameter("scope", "read,identity");

            mWebView.loadUrl(uri.toString());
        }
    }

    @Override
    public void onAllowConnect(String authCode, String state) {
        if (!mUniqueAuthState.equals(state)) {
            Timber.w("Aborting unrecognized auth request with state: %s", state);
            return;
        }

        mPreferences.putAuthCode(authCode);

        AndroidObservable
                .bindActivity(this, mRedditApi.getAccessToken(authCode, TAG))
                .flatMap(new Func1<AccessToken, Observable<Me>>() {
                    @Override
                    public Observable<Me> call(AccessToken accessToken) {
                        Timber.d("Acting on access token: %s", accessToken);
                        mPreferences.putAccessToken(accessToken.getAccessToken());
                        mPreferences.putRefreshToken(accessToken.getRefreshToken());
                        return mRedditApi.getMe(TAG);
                    }
                })
                .subscribe(new Action1<Me>() {
                    @Override
                    public void call(Me me) {
                        Toast.makeText(getApplicationContext(), "Signed in as " + me.getName(),
                                Toast.LENGTH_SHORT).show();
                        Intent data = new Intent();
                        data.putExtra("me", me);
                        // TODO: Replace with event emitting (?!)
                        setResult(RESULT_OK, data);
                        finish();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.e(throwable, "Request failed");
                    }
                });
    }

    @Override
    public void onErrorConnect(String error) {
        // TODO: Improve error handling
        Timber.d("onErrorConnect(%s)", error);
        switch (error) {
            case "access_denied":
                // Fail gracefully - let the user know you cannot continue, and be respectful of their
                // choice to decline to use your app
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
                // Double check the parameters being sent during the request to /api/v1/authorize above.
                Timber.d("There was an issue with the request sent to /api/v1/authorize ");
                break;
        }
    }

}
