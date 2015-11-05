package se.joelpet.android.toyreaderforreddit.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import se.joelpet.android.toyreaderforreddit.domain.AccessToken;
import timber.log.Timber;

public class RefreshTokenRequest extends BaseRequest<AccessToken> {

    public static final String REQUEST_URI_PATH = "api/v1/access_token";

    private final String mRefreshToken;
    private final RequestFuture<AccessToken> mResponseListener;

    public RefreshTokenRequest(String refreshToken, RequestFuture<AccessToken> future) {
        super(Method.POST, buildUrl(), future, null);
        mRefreshToken = refreshToken;
        mResponseListener = future;
    }

    private static String buildUrl() {
        return uriBuilderFromAccessToken(null).appendEncodedPath(REQUEST_URI_PATH).toString();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>(super.getHeaders());
        headers.put("Authorization", AUTHORIZATION_VALUE);
        return headers;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = super.getParams();
        if (params == null) {
            params = new HashMap<>(2);
        }
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", mRefreshToken);
        Timber.d("Using params: %s", params);
        return params;
    }

    @Override
    protected Response<AccessToken> parseNetworkResponse(NetworkResponse response) {
        try {
            JSONObject jsonObject = jsonObjectFromNetworkResponse(response);
            AccessToken accessToken = AccessToken.from(jsonObject);
            // "refresh_token" is absent in response from server, so add it back here
            accessToken.setRefreshToken(mRefreshToken);
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
