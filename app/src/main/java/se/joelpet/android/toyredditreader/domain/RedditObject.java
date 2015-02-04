package se.joelpet.android.toyredditreader.domain;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class RedditObject implements Serializable {

    public static final String NAME_KIND = "kind";

    public static final String NAME_DATA = "data";

    /** The kind of data, e.g. "Listing". */
    String kind;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public static <T extends Thing> Listing<T> listingFromJson(JSONObject jsonObject)
            throws JSONException {
        Listing<T> listing = Listing.fromJson(jsonObject.getJSONObject(NAME_DATA));
        listing.setKind(jsonObject.getString(NAME_KIND));
        return listing;
    }
}
