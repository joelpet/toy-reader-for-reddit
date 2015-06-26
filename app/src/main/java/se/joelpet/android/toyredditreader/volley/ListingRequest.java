package se.joelpet.android.toyredditreader.volley;

import com.google.gson.JsonSyntaxException;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;

import se.joelpet.android.toyredditreader.domain.Listing;
import se.joelpet.android.toyredditreader.domain.RedditObject;
import se.joelpet.android.toyredditreader.domain.Thing;
import timber.log.Timber;

public class ListingRequest<T extends Thing> extends BaseRequest<Listing<T>> {

    private final Response.Listener<Listing<T>> mResponseListener;

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     */
    public ListingRequest(String url, Response.Listener<Listing<T>> listener, Response
            .ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mResponseListener = listener;
    }

    @Override
    protected void deliverResponse(Listing<T> response) {
        Timber.v("Delivering listing response: %s", response);
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