package se.joelpet.android.toyredditreader.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import se.joelpet.android.toyredditreader.domain.AccessToken;
import timber.log.Timber;

public class RefreshTokenRequest extends BaseRequest<AccessToken> {

    public static final String REQUEST_URI_PATH = "api/v1/access_token";

    private final AccessToken mAccessToken;
    private final RequestFuture<AccessToken> mResponseListener;

    public RefreshTokenRequest(AccessToken accessToken, RequestFuture<AccessToken> future) {
        super(Method.POST, buildUrl(), future);
        mAccessToken = accessToken;
        mResponseListener = future;
    }

    private static String buildUrl() {
        return uriBuilderFromAccessToken(null).appendEncodedPath(REQUEST_URI_PATH).toString();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        // TODO: Undup this (see AccessTokenRequest), possibly into util class
        Map<String, String> headers = new HashMap<>(super.getHeaders());
        String credentials = String.format("%s:%s", AccessTokenRequest.CLIENT_ID,
                AccessTokenRequest.CLIENT_PASSWORD);
        String credentialsBase64 = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        String authorizationHeaderValue = "Basic " + credentialsBase64;
        headers.put("Authorization", authorizationHeaderValue);
        return headers;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = super.getParams();
        if (params == null) {
            params = new HashMap<>(2);
        }
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", mAccessToken.getRefreshToken());
        Timber.d("Using params: %s", params);
        return params;
    }

    @Override
    protected Response<AccessToken> parseNetworkResponse(NetworkResponse response) {
        // TODO: Undup this (see AccessTokenRequest)
        try {
            String json = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            JSONObject jsonObject = (JSONObject) new JSONTokener(json).nextValue();
            AccessToken accessToken = AccessToken.from(jsonObject);
            // "refresh_token" is absent in response from server, so add it back here
            accessToken.setRefreshToken(mAccessToken.getRefreshToken());
            return Response.success(accessToken, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(AccessToken accessToken) {
        mResponseListener.onResponse(accessToken);
    }
}
