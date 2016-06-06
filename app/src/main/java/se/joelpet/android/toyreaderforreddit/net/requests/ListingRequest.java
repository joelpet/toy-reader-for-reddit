package se.joelpet.android.toyreaderforreddit.net.requests;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.android.volley.toolbox.RequestFuture;

import org.json.JSONException;
import org.json.JSONObject;

import se.joelpet.android.toyreaderforreddit.model.Listing;
import se.joelpet.android.toyreaderforreddit.model.RedditObject;
import se.joelpet.android.toyreaderforreddit.model.Thing;

public class ListingRequest<T extends Thing> extends BaseRequest<Listing<T>> {

    public ListingRequest(String path, String after, @Nullable String accessToken,
                          RequestFuture<Listing<T>> future) {
        super(Method.GET, buildUrl(path, after, accessToken), future, accessToken);
    }

    private static String buildUrl(String path, String after, String token) {
        Uri.Builder uriBuilder = uriBuilderFromAccessToken(token).appendEncodedPath(path + ".json");
        if (!TextUtils.isEmpty(after)) {
            uriBuilder.appendQueryParameter("after", after);
        }
        return uriBuilder.toString();
    }

    @NonNull
    @Override
    protected Listing<T> mapJsonObjectToResult(JSONObject jsonObject) throws JSONException {
        return RedditObject.listingFromJson(jsonObject);
    }
}