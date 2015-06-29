package se.joelpet.android.toyredditreader.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;

import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class BaseRequest<T> extends Request<T> {

    /** Base URI for unauthorized requests. */
    private static final Uri BASE_URI = Uri.parse("https://www.reddit.com/");
    /** Base URI for authorized requests. */
    private static final Uri BASE_OAUTH_URI = Uri.parse("https://oauth.reddit.com/");

    private String mAccessToken;

    public BaseRequest(int method, String url, Response.ErrorListener listener) {
        super(method, url, listener);
    }

    public BaseRequest(int method, String url, Response.ErrorListener listener, String
            accessToken) {
        this(method, url, listener);
        mAccessToken = checkNotNull(accessToken);
    }

    protected static Uri.Builder uriBuilderFromAccessToken(String token) {
        return token != null ? BASE_OAUTH_URI.buildUpon() : BASE_URI.buildUpon();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (TextUtils.isEmpty(mAccessToken)) {
            return super.getHeaders();
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "bearer " + mAccessToken);
        Timber.d("Set Authorization header using access token: %s", mAccessToken);
        return headers;
    }
}
