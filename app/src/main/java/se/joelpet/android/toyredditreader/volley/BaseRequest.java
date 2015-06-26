package se.joelpet.android.toyredditreader.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public abstract class BaseRequest<T> extends Request<T> {

    private String mAccessToken;

    public BaseRequest(int method, String url, Response.ErrorListener listener) {
        super(method, url, listener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        // TODO: If requires authentication, and access token is null, then throw error
        if (mAccessToken == null) {
            return super.getHeaders();
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "bearer " + mAccessToken);
        Timber.d("Set Authorization header using access token: %s", mAccessToken);
        return headers;
    }

    public void setAccessToken(String accessToken) {
        mAccessToken = accessToken;
    }
}
