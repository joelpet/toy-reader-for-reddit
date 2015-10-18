package se.joelpet.android.toyreaderforreddit;

import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;

import se.joelpet.android.toyreaderforreddit.domain.AccessToken;
import se.joelpet.android.toyreaderforreddit.domain.Me;

public class Preferences {

    private static final String PREFERENCES_NAME = "preferences";

    public static final String KEY_AUTH_CODE = "auth_code";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_ME = "KEY_ME";

    private Context mContext;
    private Gson mGson;

    public Preferences(Context context) {
        mContext = context;
        mGson = new Gson();
    }

    public String getAuthCode() {
        return getSharedPreferences().getString(KEY_AUTH_CODE, null);
    }

    public void putAuthCode(String authCode) {
        getEditor().putString(KEY_AUTH_CODE, authCode).commit();
    }

    public AccessToken getAccessToken() {
        String tokenString = getSharedPreferences().getString(KEY_ACCESS_TOKEN, null);
        return tokenString != null ? mGson.fromJson(tokenString, AccessToken.class) : null;
    }

    public void putAccessToken(AccessToken accessToken) {
        getEditor().putString(KEY_ACCESS_TOKEN, mGson.toJson(accessToken, AccessToken.class))
                .commit();
    }

    public void deleteAccessToken() {
        getEditor().remove(KEY_ACCESS_TOKEN).commit();
    }

    public Me getMe() {
        String meString = getSharedPreferences().getString(KEY_ME, null);
        return meString != null ? mGson.fromJson(meString, Me.class) : null;
    }

    public void putMe(Me me) {
        getEditor().putString(KEY_ME, mGson.toJson(me)).commit();
    }

    public void deleteMe() {
        getEditor().remove(KEY_ME).commit();
    }

    private SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getEditor() {
        return getSharedPreferences().edit();
    }
}
