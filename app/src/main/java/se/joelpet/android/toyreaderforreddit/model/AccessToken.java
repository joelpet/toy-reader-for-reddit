package se.joelpet.android.toyreaderforreddit.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class AccessToken implements Serializable {

    public static final String NAME_ACCESS_TOKEN = "access_token";

    public static final String NAME_TOKEN_TYPE = "token_type";

    public static final String NAME_EXPIRES_IN = "expires_in";

    public static final String NAME_SCOPE = "scope";

    public static final String NAME_REFRESH_TOKEN = "refresh_token";

    /** Timestamp indicating when this token was created */
    Date created;

    /** Your access token */
    String accessToken;

    /** "bearer" */
    String tokenType;

    /** Unix Epoch Seconds */
    Long expiresIn;

    /** A scope string */
    String scope;

    /** Your refresh token */
    String refreshToken;

    public boolean isExpired() {
        if (created == null) return true;
        return created.getTime() + (1000 * expiresIn) < new Date().getTime();
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "AccessToken{" +
                "created=" + created +
                ", accessToken='" + accessToken + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", scope='" + scope + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }

    public static AccessToken from(JSONObject jsonObject) throws JSONException {
        AccessToken accessToken = new AccessToken();

        accessToken.setCreated(new Date());
        accessToken.setAccessToken(jsonObject.getString(NAME_ACCESS_TOKEN));
        accessToken.setTokenType(jsonObject.getString(NAME_TOKEN_TYPE));
        accessToken.setExpiresIn(jsonObject.getLong(NAME_EXPIRES_IN));
        accessToken.setScope(jsonObject.getString(NAME_SCOPE));
        // "refresh_token" is absent (optional) in responses to token-refresh-requests
        accessToken.setRefreshToken(jsonObject.optString(NAME_REFRESH_TOKEN));

        return accessToken;
    }
}
