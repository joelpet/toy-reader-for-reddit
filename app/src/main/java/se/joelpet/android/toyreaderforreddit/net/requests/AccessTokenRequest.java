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

abstract class AccessTokenRequest extends BaseRequest<AccessToken> {

    public AccessTokenRequest(@Nullable RequestFuture<AccessToken> future) {
        super(Method.POST, "https://www.reddit.com/api/v1/access_token", future, null);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>(super.getHeaders());
        headers.put("Authorization", AUTHORIZATION_VALUE);
        return headers;
    }

    @NonNull
    @Override
    protected AccessToken mapJsonObjectToResult(JSONObject jsonObject) throws JSONException {
        return AccessToken.from(jsonObject);
    }
}
