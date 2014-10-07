package se.joelpet.android.reddit.domain;

import java.io.Serializable;

public class SubredditListingWrapper implements Serializable {

    String kind;

    SubredditWrapperListing data;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public SubredditWrapperListing getData() {
        return data;
    }

    public void setData(SubredditWrapperListing data) {
        this.data = data;
    }
}
