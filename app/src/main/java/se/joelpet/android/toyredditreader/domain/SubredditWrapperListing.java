package se.joelpet.android.toyredditreader.domain;

import java.io.Serializable;
import java.util.List;

public class SubredditWrapperListing implements Serializable {

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
    List<SubredditWrapper> children;

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

    public List<SubredditWrapper> getChildren() {
        return children;
    }

    public void setChildren(List<SubredditWrapper> children) {
        this.children = children;
    }
}
