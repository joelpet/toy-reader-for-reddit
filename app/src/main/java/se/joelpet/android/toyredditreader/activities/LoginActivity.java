package se.joelpet.android.toyredditreader.activities;

import com.android.volley.VolleyError;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.widget.Toast;

import java.util.UUID;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import se.joelpet.android.toyredditreader.AppConnectWebViewClient;
import se.joelpet.android.toyredditreader.Preferences;
import se.joelpet.android.toyredditreader.R;
import se.joelpet.android.toyredditreader.domain.AccessToken;
import se.joelpet.android.toyredditreader.domain.Me;
import se.joelpet.android.toyredditreader.net.RedditApi;
import se.joelpet.android.toyredditreader.volley.AccessTokenRequest;
import se.joelpet.android.toyredditreader.volley.ResponseListener;
import timber.log.Timber;

public class LoginActivity extends BaseActivity
        implements AppConnectWebViewClient.OnAppConnectListener {

    public static final String TAG = LoginActivity.class.getName();

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

        if (savedInstanceState == null) {
            Uri.Builder uri = Uri.parse("https://ssl.reddit.com/api/v1/authorize").buildUpon();
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
        mRedditApi.getAccessToken(authCode, TAG, new ResponseListener<AccessToken>() {
            @Override
            public void onResponse(AccessToken accessToken) {
                super.onResponse(accessToken);
                mPreferences.putAccessToken(accessToken.getAccessToken());
                mPreferences.putRefreshToken(accessToken.getRefreshToken());
                mRedditApi.getMe(TAG, new ResponseListener<Me>() {
                    @Override
                    public void onResponse(Me me) {
                        super.onResponse(me);
                        Toast.makeText(getApplicationContext(), "Signed in as " + me.getName(),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        super.onErrorResponse(volleyError);
                    }
                });
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
