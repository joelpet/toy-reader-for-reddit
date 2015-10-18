package se.joelpet.android.toyreaderforreddit.domain;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Listing<T extends Thing> extends RedditObject implements Serializable {

    public static final String NAME_BEFORE = "before";

    public static final String NAME_AFTER = "after";

    public static final String NAME_MODHASH = "modhash";

    public static final String NAME_CHILDREN = "children";

    /**
     * The fullname of the listing that follows before this page. null if there is no previous
     * page.
     */
    String before;

    /**
     * The fullname of the listing that follows after this page. null if there is no next page.
     */
    String after;

    /**
     * This modhash is not the same modhash provided upon login. You do not need to update your
     * user's modhash everytime you get a new modhash. You can reuse the modhash given upon login.
     */
    String modhash;

    /**
     * A list of things that this Listing wraps.
     */
    List<T> children;

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getModhash() {
        return modhash;
    }

    public void setModhash(String modhash) {
        this.modhash = modhash;
    }

    public List<T> getChildren() {
        return children;
    }

    public void setChildren(List<T> children) {
        this.children = children;
    }

    public static <T extends Thing> Listing<T> fromJson(JSONObject jsonObject)
            throws JSONException {
        Listing<T> listing = new Listing<>();

        listing.setBefore(jsonObject.getString(NAME_BEFORE));
        listing.setAfter(jsonObject.getString(NAME_AFTER));
        listing.setModhash(jsonObject.getString(NAME_MODHASH));
        listing.setChildren(new ArrayList<T>());

        JSONArray childrenJsonArray = jsonObject.getJSONArray(NAME_CHILDREN);

        for (int i = 0; childrenJsonArray != null && i < childrenJsonArray.length(); i++) {
            T thing = Thing.fromJson(childrenJsonArray.getJSONObject(i));
            listing.getChildren().add(thing);
        }

        return listing;
    }

    @Override
    public String toString() {
        return "Listing{" +
                "before='" + before + '\'' +
                ", after='" + after + '\'' +
                ", modhash='" + modhash + '\'' +
                ", children=" + children +
                '}';
    }
}
