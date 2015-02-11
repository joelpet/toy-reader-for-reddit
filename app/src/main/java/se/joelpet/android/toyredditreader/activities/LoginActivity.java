package se.joelpet.android.toyredditreader.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;
import se.joelpet.android.toyredditreader.AppConnectWebViewClient;
import se.joelpet.android.toyredditreader.Preferences;
import se.joelpet.android.toyredditreader.R;
import timber.log.Timber;

public class LoginActivity extends BaseActivity
        implements AppConnectWebViewClient.OnAppConnectListener {

    @InjectView(R.id.toolbar)
    protected Toolbar mToolbar;

    @InjectView(R.id.web_view)
    protected WebView mWebView;

    private String mUniqueAuthState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);

        // TODO: Inject web view client
        AppConnectWebViewClient webViewClient = new AppConnectWebViewClient();
        webViewClient.setOnAppConnectListener(this);
        mWebView.setWebViewClient(webViewClient);

        if (savedInstanceState == null) {
            Uri.Builder uri = Uri.parse("https://ssl.reddit.com/api/v1/authorize").buildUpon();
            mUniqueAuthState = UUID.randomUUID().toString();
            uri.appendQueryParameter("client_id", "a9zsngGTh8DYyw");
            uri.appendQueryParameter("response_type", "code");
            uri.appendQueryParameter("state", mUniqueAuthState);
            uri.appendQueryParameter("redirect_uri", "toyredditreader://redirect");
            uri.appendQueryParameter("duration", "permanent");
            uri.appendQueryParameter("scope", "read");

            mWebView.loadUrl(uri.toString());
        }
    }

    @Override
    public void onAllowConnect(String token, String state) {
        if (!mUniqueAuthState.equals(state)) {
            Timber.w("Aborting unrecognized auth request with state: %s", state);
            return;
        }

        new Preferences(this).putToken(token);
        Timber.d("Stored auth token: %s", token);
        finish();
    }

    @Override
    public void onError(String error) {
        // TODO: Improve error handling
        Timber.d("onError(%s)", error);
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
