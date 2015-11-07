package se.joelpet.android.toyreaderforreddit.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import se.joelpet.android.toyreaderforreddit.domain.AccessToken;
import timber.log.Timber;

abstract class AccessTokenRequest extends BaseRequest<AccessToken> {

    private final Response.Listener<AccessToken> mListener;

    public AccessTokenRequest(Response.Listener<AccessToken> listener,
                              Response.ErrorListener errorListener) {
        super(Method.POST, "https://www.reddit.com/api/v1/access_token", errorListener, null);
        mListener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>(super.getHeaders());
        headers.put("Authorization", AUTHORIZATION_VALUE);
        return headers;
    }

    @Override
    protected Response<AccessToken> parseNetworkResponse(NetworkResponse response) {
        try {
            JSONObject jsonObject = jsonObjectFromNetworkResponse(response);
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
        Timber.d("Delivering access token response to %s", mListener);
        mListener.onResponse(accessToken);
    }
}
