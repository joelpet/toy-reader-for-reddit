package se.joelpet.android.toyredditreader.domain;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Me implements Serializable {

    public static final String NAME_NAME = "name";

    public static final String NAME_CREATED = "created";

    public static final String NAME_HIDE_FROM_ROBOTS = "hide_from_robots";

    public static final String NAME_GOLD_CREDDITS = "gold_creddits";

    public static final String NAME_CREATED_UTC = "created_utc";

    public static final String NAME_LINK_KARMA = "link_karma";

    public static final String NAME_COMMENT_KARMA = "comment_karma";

    public static final String NAME_OVER_18 = "over_18";

    public static final String NAME_IS_GOLD = "is_gold";

    public static final String NAME_IS_MOD = "is_mod";

    public static final String NAME_GOLD_EXPIRATION = "gold_expiration";

    public static final String NAME_HAS_VERIFIED_EMAIL = "has_verified_email";

    public static final String NAME_ID = "id";

    String name;

    Double created;

    Boolean hideFromRobots;

    Integer goldCreddits;

    Double createdUtc;

    Integer linkKarma;

    Integer commentKarma;

    Boolean over18;

    Boolean isGold;

    Boolean isMod;

    Long goldExpiration;

    Boolean hasVerifiedEmail;

    String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCreated() {
        return created;
    }

    public void setCreated(Double created) {
        this.created = created;
    }

    public Boolean getHideFromRobots() {
        return hideFromRobots;
    }

    public void setHideFromRobots(Boolean hideFromRobots) {
        this.hideFromRobots = hideFromRobots;
    }

    public Integer getGoldCreddits() {
        return goldCreddits;
    }

    public void setGoldCreddits(Integer goldCreddits) {
        this.goldCreddits = goldCreddits;
    }

    public Double getCreatedUtc() {
        return createdUtc;
    }

    public void setCreatedUtc(Double createdUtc) {
        this.createdUtc = createdUtc;
    }

    public Integer getLinkKarma() {
        return linkKarma;
    }

    public void setLinkKarma(Integer linkKarma) {
        this.linkKarma = linkKarma;
    }

    public Integer getCommentKarma() {
        return commentKarma;
    }

    public void setCommentKarma(Integer commentKarma) {
        this.commentKarma = commentKarma;
    }

    public Boolean getOver18() {
        return over18;
    }

    public void setOver18(Boolean over18) {
        this.over18 = over18;
    }

    public Boolean getIsGold() {
        return isGold;
    }

    public void setIsGold(Boolean isGold) {
        this.isGold = isGold;
    }

    public Boolean getIsMod() {
        return isMod;
    }

    public void setIsMod(Boolean isMod) {
        this.isMod = isMod;
    }

    public Long getGoldExpiration() {
        return goldExpiration;
    }

    public void setGoldExpiration(Long goldExpiration) {
        this.goldExpiration = goldExpiration;
    }

    public Boolean getHasVerifiedEmail() {
        return hasVerifiedEmail;
    }

    public void setHasVerifiedEmail(Boolean hasVerifiedEmail) {
        this.hasVerifiedEmail = hasVerifiedEmail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Me{" +
                "name='" + name + '\'' +
                ", created=" + created +
                ", hideFromRobots=" + hideFromRobots +
                ", goldCreddits=" + goldCreddits +
                ", createdUtc=" + createdUtc +
                ", linkKarma=" + linkKarma +
                ", commentKarma=" + commentKarma +
                ", over18=" + over18 +
                ", isGold=" + isGold +
                ", isMod=" + isMod +
                ", goldExpiration=" + goldExpiration +
                ", hasVerifiedEmail=" + hasVerifiedEmail +
                ", id='" + id + '\'' +
                '}';
    }

    public static Me from(JSONObject jsonObject) throws JSONException {
        Me me = new Me();

        me.setName(jsonObject.getString(NAME_NAME));
        me.setCreated(jsonObject.getDouble(NAME_CREATED));
        me.setHideFromRobots(jsonObject.getBoolean(NAME_HIDE_FROM_ROBOTS));
        me.setGoldCreddits(jsonObject.getInt(NAME_GOLD_CREDDITS));
        me.setCreatedUtc(jsonObject.getDouble(NAME_CREATED_UTC));
        me.setLinkKarma(jsonObject.getInt(NAME_LINK_KARMA));
        me.setCommentKarma(jsonObject.getInt(NAME_COMMENT_KARMA));
        me.setOver18(jsonObject.getBoolean(NAME_OVER_18));
        me.setIsGold(jsonObject.getBoolean(NAME_IS_GOLD));
        me.setIsMod(jsonObject.getBoolean(NAME_IS_MOD));
        me.setGoldExpiration(jsonObject.optLong(NAME_GOLD_EXPIRATION));
        me.setHasVerifiedEmail(jsonObject.getBoolean(NAME_HAS_VERIFIED_EMAIL));
        me.setId(jsonObject.getString(NAME_ID));

        return me;
    }
}
