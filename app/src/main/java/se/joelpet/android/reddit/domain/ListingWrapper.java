package se.joelpet.android.reddit.domain;

import java.io.Serializable;

public class ListingWrapper implements Serializable {

    String kind;

    Listing<SubredditWrapper> data;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Listing<SubredditWrapper> getData() {
        return data;
    }

    public void setData(Listing<SubredditWrapper> data) {
        this.data = data;
    }
}
