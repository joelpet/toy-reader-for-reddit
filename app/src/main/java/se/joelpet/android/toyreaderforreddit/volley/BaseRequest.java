package se.joelpet.android.toyreaderforreddit.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public abstract class BaseRequest<T> extends Request<T> {

    /** Base URI for unauthorized requests. */
    private static final Uri BASE_URI = Uri.parse("https://www.reddit.com/");
    /** Base URI for authorized requests. */
    private static final Uri BASE_OAUTH_URI = Uri.parse("https://oauth.reddit.com/");

    /** The Client ID generated during app registration. */
    public static final String CLIENT_ID = "a9zsngGTh8DYyw";
    /** The "password" for non-confidential clients (installed apps) is an empty string. */
    public static final String CLIENT_PASSWORD = "";
    /** The Authorization HTTP header value that identifies this client. */
    public static final String AUTHORIZATION_VALUE = getAuthorizationValue();

    @Nullable
    private String mAccessToken;

    private static String getAuthorizationValue() {
        String credentials = String.format("%s:%s", CLIENT_ID, CLIENT_PASSWORD);
        String credentialsBase64 = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        return "Basic " + credentialsBase64;
    }

    public BaseRequest(int method, String url, Response.ErrorListener listener,
                       @Nullable String accessToken) {
        super(method, url, listener);
        mAccessToken = accessToken;
    }

    protected static Uri.Builder uriBuilderFromAccessToken(String token) {
        return token != null ? BASE_OAUTH_URI.buildUpon() : BASE_URI.buildUpon();
    }

    protected static JSONObject jsonObjectFromNetworkResponse(NetworkResponse response) throws
            UnsupportedEncodingException, JSONException {
        String json = new String(response.data,
                HttpHeaderParser.parseCharset(response.headers));
        return (JSONObject) new JSONTokener(json).nextValue();
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
