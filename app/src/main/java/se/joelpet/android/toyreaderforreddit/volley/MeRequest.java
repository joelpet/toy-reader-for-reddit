package se.joelpet.android.toyreaderforreddit.volley;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.toolbox.RequestFuture;

import org.json.JSONException;
import org.json.JSONObject;

import se.joelpet.android.toyreaderforreddit.domain.Me;

public class MeRequest extends BaseRequest<Me> {

    private static final String REQUEST_URI_PATH = "api/v1/me";

    public MeRequest(String accessToken, @Nullable RequestFuture<Me> future) {
        super(Method.GET, buildUrl(accessToken), future, accessToken);
    }

    private static String buildUrl(String token) {
        return uriBuilderFromAccessToken(token).appendEncodedPath(REQUEST_URI_PATH).toString();
    }

    @NonNull
    @Override
    protected Me mapJsonObjectToResult(JSONObject jsonObject) throws JSONException {
        return Me.from(jsonObject);
    }
}
