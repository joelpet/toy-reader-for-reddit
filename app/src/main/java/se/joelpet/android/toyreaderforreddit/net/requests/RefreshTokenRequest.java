package se.joelpet.android.toyreaderforreddit.net.requests;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import se.joelpet.android.toyreaderforreddit.model.AccessToken;
import timber.log.Timber;

public class RefreshTokenRequest extends BaseRequest<AccessToken> {

    public static final String REQUEST_URI_PATH = "api/v1/access_token";

    private final String mRefreshToken;

    public RefreshTokenRequest(String refreshToken, @Nullable RequestFuture<AccessToken> future) {
        super(Method.POST, buildUrl(), future, null);
        mRefreshToken = refreshToken;
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

    @NonNull
    @Override
    protected AccessToken mapJsonObjectToResult(JSONObject jsonObject) throws JSONException {
        AccessToken accessToken = AccessToken.from(jsonObject);
        // "refresh_token" is absent in response from server, so add it back here
        accessToken.setRefreshToken(mRefreshToken);
        return accessToken;
    }
}
