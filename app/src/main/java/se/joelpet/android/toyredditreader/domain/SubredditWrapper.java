package se.joelpet.android.toyredditreader.domain;

import java.io.Serializable;

public class SubredditWrapper implements Serializable {

    String kind;

    Subreddit data;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Subreddit getData() {
        return data;
    }

    public void setData(Subreddit data) {
        this.data = data;
    }
}
