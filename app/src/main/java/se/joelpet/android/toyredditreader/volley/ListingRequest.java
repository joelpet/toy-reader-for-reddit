package se.joelpet.android.toyredditreader.volley;

import com.google.gson.JsonSyntaxException;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;

import se.joelpet.android.toyredditreader.domain.Listing;
import se.joelpet.android.toyredditreader.domain.RedditObject;
import se.joelpet.android.toyredditreader.domain.Thing;
import timber.log.Timber;

public class ListingRequest<T extends Thing> extends BaseRequest<Listing<T>> {

    private final Response.Listener<Listing<T>> mResponseListener;

    public ListingRequest(String path, String after, @Nullable String accessToken,
                          RequestFuture<Listing<T>> future) {
        super(Method.GET, buildUrl(path, after, accessToken), future, accessToken);
        mResponseListener = future;
    }

    private static String buildUrl(String path, String after, String token) {
        Uri.Builder uriBuilder = uriBuilderFromAccessToken(token).appendEncodedPath(path + ".json");
        if (!TextUtils.isEmpty(after)) {
            uriBuilder.appendQueryParameter("after", after);
        }
        return uriBuilder.toString();
    }

    @Override
    protected void deliverResponse(Listing<T> response) {
        Timber.v("Delivering listing response: %s", response);
        mResponseListener.onResponse(response);
    }

    @Override
    protected Response<Listing<T>> parseNetworkResponse(NetworkResponse response) {
        try {
            JSONObject jsonObject = jsonObjectFromNetworkResponse(response);
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