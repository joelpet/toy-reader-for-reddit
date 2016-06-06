package se.joelpet.android.toyreaderforreddit.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import timber.log.Timber;

public class Thing implements Serializable {

    public static final String NAME_KIND = "kind";

    public static final String NAME_DATA = "data";

    public static final String NAME_ID = "id";

    public static final String NAME_NAME = "name";

    /**
     * this item's identifier, e.g. "8xwlg"
     */
    String id;

    /**
     * Fullname of comment, e.g. "t1_c3v7f8u"
     */
    String name;

    /**
     * All things have a kind. The kind is a String identifier that denotes the object's type. Some
     * examples: Listing, more, t1, t2
     */
    String kind;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public static <T extends Thing> T fromJson(JSONObject jsonObject) throws JSONException {
        T thing;
        String kind = jsonObject.getString(NAME_KIND);
        JSONObject data = jsonObject.getJSONObject(NAME_DATA);

        if ("t3".equals(kind)) {
            thing = (T) Link.fromJson(data);
        } else {
            Timber.w("Unrecognized data kind: %s", kind);
            return null;
        }

        thing.setId(data.getString(NAME_ID));
        thing.setName(data.getString(NAME_NAME));
        thing.setKind(kind);

        return thing;
    }
}
