package se.joelpet.android.toyredditreader.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import se.joelpet.android.toyredditreader.domain.AccessToken;
import timber.log.Timber;

public class AccessTokenRequest extends Request<AccessToken> {

    public static final String AUTH_REDIRECT_URI = "toyredditreader://redirect";

    public static final String CLIENT_ID = "a9zsngGTh8DYyw";

    /** The "password" for non-confidential clients (installed apps) is an empty string. */
    public static final String CLIENT_PASSWORD = "";

    private final String mCode;

    private final Response.Listener<AccessToken> mListener;

    public AccessTokenRequest(String code, Response.Listener<AccessToken> listener,
            Response.ErrorListener errorListener) {
        super(Method.POST, "https://www.reddit.com/api/v1/access_token", errorListener);
        mCode = code;
        mListener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>(super.getHeaders());
        String credentials = String.format("%s:%s", CLIENT_ID, CLIENT_PASSWORD);
        String credentialsBase64 = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        String authorizationHeaderValue = "Basic " + credentialsBase64;
        headers.put("Authorization", authorizationHeaderValue);
        return headers;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = super.getParams();
        // Indicates that you're using the "standard" code based flow. Other values not relevant to
        // this flow are refresh_token (for renewing an access token) and password (for script apps only)
        if (params == null) {
            params = new HashMap<>(3);
        }
        params.put("grant_type", "authorization_code");
        params.put("code", mCode);
        params.put("redirect_uri", AUTH_REDIRECT_URI);
        Timber.d("Using params: %s", params);
        return params;
    }

    @Override
    protected Response<AccessToken> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            JSONObject jsonObject = (JSONObject) new JSONTokener(json).nextValue();
            AccessToken accessToken = AccessToken.from(jsonObject);
            return Response.success(accessToken, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(AccessToken accessToken) {
        Timber.i("Delivering response: %s", accessToken);
        mListener.onResponse(accessToken);
    }
}
