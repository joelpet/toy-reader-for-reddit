package se.joelpet.android.toyreaderforreddit.volley;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.RequestFuture;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import se.joelpet.android.toyreaderforreddit.BuildConfig;
import se.joelpet.android.toyreaderforreddit.net.ratelimit.RedditRateLimit;
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

    private static final String USER_AGENT = "android:" + BuildConfig.APPLICATION_ID + ":" +
            BuildConfig.VERSION_NAME + " (by /u/iMoM)";

    @Nullable
    private final Response.Listener<T> mResponseListener;
    @Nullable
    private String mAccessToken;

    private static String getAuthorizationValue() {
        String credentials = String.format("%s:%s", CLIENT_ID, CLIENT_PASSWORD);
        String credentialsBase64 = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        return "Basic " + credentialsBase64;
    }

    public BaseRequest(int method, String url, @Nullable RequestFuture<T> future,
                       @Nullable String accessToken) {
        super(method, url, future);
        mResponseListener = future;
        mAccessToken = accessToken;
    }

    protected static Uri.Builder uriBuilderFromAccessToken(String token) {
        return token != null ? BASE_OAUTH_URI.buildUpon() : BASE_URI.buildUpon();
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        Timber.d("Response headers: %s", response.headers);
        RedditRateLimit.GLOBAL.update(response.headers);

        try {
            String charset = HttpHeaderParser.parseCharset(response.headers);
            String json = new String(response.data, charset);
            T result = mapJsonObjectToResult(new JSONObject(json));
            return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }
    }

    @NonNull
    protected abstract T mapJsonObjectToResult(JSONObject jsonObject) throws JSONException;

    @Override
    protected void deliverResponse(T response) {
        if (mResponseListener != null) {
            Timber.v("Delivering response: %s", response);
            mResponseListener.onResponse(response);
        }
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (TextUtils.isEmpty(mAccessToken)) {
            return super.getHeaders();
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "bearer " + mAccessToken);
        headers.put("User-Agent", USER_AGENT);
        Timber.d("Headers: " + headers);
        return headers;
    }
}
