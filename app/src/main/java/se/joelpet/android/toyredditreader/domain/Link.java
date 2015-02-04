package se.joelpet.android.toyredditreader.domain;

import org.json.JSONException;
import org.json.JSONObject;

public class Link extends Thing {

    public static final String NAME_DOMAIN = "domain";

    public static final String NAME_SUBREDDIT = "subreddit";

    public static final String NAME_SELFTEXT = "selftext";

    public static final String NAME_ID = "id";

    public static final String NAME_AUTHOR = "author";

    public static final String NAME_SCORE = "score";

    public static final String NAME_THUMBNAIL = "thumbnail";

    public static final String NAME_SUBREDDIT_ID = "subreddit_id";

    public static final String NAME_NAME = "name";

    public static final String NAME_PERMALINK = "permalink";

    public static final String NAME_CREATED = "created";

    public static final String NAME_URL = "url";

    public static final String NAME_TITLE = "title";

    public static final String NAME_CREATED_UTC = "created_utc";

    public static final String NAME_UPS = "ups";

    public static final String NAME_NUM_COMMENTS = "num_comments";

    String domain;

    String subreddit;

    String selftext;

    String id;

    String author;

    String score;

    String thumbnail;

    String subredditId;

    String name;

    String permalink;

    Integer created;

    String url;

    String title;

    Double createdUtc;

    String ups;

    Integer numComments;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getSelftext() {
        return selftext;
    }

    public void setSelftext(String selftext) {
        this.selftext = selftext;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getSubredditId() {
        return subredditId;
    }

    public void setSubredditId(String subredditId) {
        this.subredditId = subredditId;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public Integer getCreated() {
        return created;
    }

    public void setCreated(Integer created) {
        this.created = created;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getCreatedUtc() {
        return createdUtc;
    }

    public void setCreatedUtc(Double createdUtc) {
        this.createdUtc = createdUtc;
    }

    public String getUps() {
        return ups;
    }

    public void setUps(String ups) {
        this.ups = ups;
    }

    public Integer getNumComments() {
        return numComments;
    }

    public void setNumComments(Integer numComments) {
        this.numComments = numComments;
    }

    public static Link fromJson(JSONObject jsonObject) throws JSONException {
        Link link = new Link();

        link.setDomain(jsonObject.getString(NAME_DOMAIN));
        link.setSubreddit(jsonObject.getString(NAME_SUBREDDIT));
        link.setSelftext(jsonObject.getString(NAME_SELFTEXT));
        link.setId(jsonObject.getString(NAME_ID));
        link.setAuthor(jsonObject.getString(NAME_AUTHOR));
        link.setScore(jsonObject.getString(NAME_SCORE));
        link.setThumbnail(jsonObject.getString(NAME_THUMBNAIL));
        link.setSubredditId(jsonObject.getString(NAME_SUBREDDIT_ID));
        link.setName(jsonObject.getString(NAME_NAME));
        link.setPermalink(jsonObject.getString(NAME_PERMALINK));
        link.setCreated(jsonObject.getInt(NAME_CREATED));
        link.setUrl(jsonObject.getString(NAME_URL));
        link.setTitle(jsonObject.getString(NAME_TITLE));
        link.setCreatedUtc(jsonObject.getDouble(NAME_CREATED_UTC));
        link.setUps(jsonObject.getString(NAME_UPS));
        link.setNumComments(jsonObject.getInt(NAME_NUM_COMMENTS));

        return link;
    }

    @Override
    public String toString() {
        return "Subreddit{" +
                "domain='" + domain + '\'' +
                ", subreddit='" + subreddit + '\'' +
                ", selftext='" + selftext + '\'' +
                ", id='" + id + '\'' +
                ", author='" + author + '\'' +
                ", score='" + score + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", subredditId='" + subredditId + '\'' +
                ", name='" + name + '\'' +
                ", permalink='" + permalink + '\'' +
                ", created=" + created +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", createdUtc=" + createdUtc +
                ", ups='" + ups + '\'' +
                ", numComments=" + numComments +
                '}';
    }
}
