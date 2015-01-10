package se.joelpet.android.toyredditreader.domain;

import java.io.Serializable;

public class Thing implements Serializable {

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

    /**
     * A custom data structure used to hold valuable information. This object's format will follow
     * the data structure respective of its kind. See below for specific structures.
     */
    Object data;

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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
