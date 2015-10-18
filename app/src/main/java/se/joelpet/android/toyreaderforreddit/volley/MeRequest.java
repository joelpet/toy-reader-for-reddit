package se.joelpet.android.toyreaderforreddit.volley;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import se.joelpet.android.toyreaderforreddit.domain.Me;
import timber.log.Timber;

public class MeRequest extends BaseRequest<Me> {

    private static final String REQUEST_URI_PATH = "api/v1/me";

    private final Response.Listener<Me> mListener;

    public MeRequest(String accessToken, RequestFuture<Me> future) {
        super(Method.GET, buildUrl(accessToken), future, accessToken);
        mListener = future;
    }

    private static String buildUrl(String token) {
        return uriBuilderFromAccessToken(token).appendEncodedPath(REQUEST_URI_PATH).toString();
    }

    @Override
    protected Response<Me> parseNetworkResponse(NetworkResponse response) {
        Timber.d("parseNetworkResponse(%s)", response);
        try {
            JSONObject jsonObject = jsonObjectFromNetworkResponse(response);
            Me me = Me.from(jsonObject);
            return Response.success(me, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(Me o) {
        Timber.i("Delivering response: %s", o);
        mListener.onResponse(o);
    }
}
