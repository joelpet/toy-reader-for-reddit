package se.joelpet.android.toyreaderforreddit.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import java.util.HashMap;
import java.util.Map;

import se.joelpet.android.toyreaderforreddit.domain.AccessToken;
import timber.log.Timber;

public class UserAccessTokenRequest extends AccessTokenRequest {

    public static final String AUTH_REDIRECT_URI = "toyreaderforreddit://redirect";

    private final String mCode;

    public UserAccessTokenRequest(String code, Response.Listener<AccessToken> listener,
                                  Response.ErrorListener errorListener) {
        super(listener, errorListener);
        mCode = code;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = new HashMap<>(3);
        // Indicates that you're using the "standard" code based flow. Other values not relevant to
        // this flow are refresh_token (for renewing an access token) and password (for script
        // apps only)
        params.put("grant_type", "authorization_code");
        params.put("code", mCode);
        params.put("redirect_uri", AUTH_REDIRECT_URI);
        Timber.d("Request body parameters: %s", params);
        return params;
    }
}
