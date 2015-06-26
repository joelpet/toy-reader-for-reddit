package se.joelpet.android.toyredditreader.volley;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;

import se.joelpet.android.toyredditreader.domain.Me;
import timber.log.Timber;

public class MeRequest extends BaseRequest<Me> {

    public static final String REQUEST_URL = "https://oauth.reddit.com/api/v1/me";

    private final Response.Listener<Me> mListener;

    public MeRequest(String accessToken, Response.Listener<Me> listener, Response.ErrorListener
            errorListener) {
        super(Method.GET, REQUEST_URL, errorListener);
        setAccessToken(accessToken);
        mListener = listener;
    }

    @Override
    protected Response<Me> parseNetworkResponse(NetworkResponse response) {
        Timber.d("parseNetworkResponse(%s)", response);
        try {
            String json = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            JSONObject jsonObject = (JSONObject) new JSONTokener(json).nextValue();
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
