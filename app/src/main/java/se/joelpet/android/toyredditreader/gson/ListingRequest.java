package se.joelpet.android.toyredditreader.gson;

import com.google.gson.JsonSyntaxException;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import se.joelpet.android.toyredditreader.domain.Listing;
import se.joelpet.android.toyredditreader.domain.RedditObject;
import se.joelpet.android.toyredditreader.domain.Thing;

public class ListingRequest<T extends Thing> extends Request<Listing<T>> {

    private final Map<String, String> mHeaders;

    private final Response.Listener<Listing<T>> mResponseListener;

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url     URL of the request to make
     * @param headers Map of request headers
     */
    public ListingRequest(String url, Map<String, String> headers,
            Response.Listener<Listing<T>> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mHeaders = headers;
        mResponseListener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders != null ? mHeaders : super.getHeaders();
    }

    @Override
    protected void deliverResponse(Listing<T> response) {
        mResponseListener.onResponse(response);
    }

    @Override
    protected Response<Listing<T>> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            JSONObject jsonObject = (JSONObject) new JSONTokener(json).nextValue();
            Listing<T> listing = RedditObject.listingFromJson(jsonObject);
            return Response.success(listing, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }
    }

}