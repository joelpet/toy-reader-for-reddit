package se.joelpet.android.toyredditreader;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static final String PREFERENCES_NAME = "preferences";

    public static final String KEY_AUTH_CODE = "auth_code";

    public static final String KEY_ACCESS_TOKEN = "access_token";

    public static final String KEY_REFRESH_TOKEN = "refresh_token";

    private Context mContext;

    public Preferences(Context context) {
        mContext = context;
    }

    public String getAuthCode() {
        return getSharedPreferences().getString(KEY_AUTH_CODE, null);
    }

    public void putAuthCode(String authCode) {
        getEditor(Context.MODE_PRIVATE).putString(KEY_AUTH_CODE, authCode).commit();
    }

    public String getAccessToken() {
        return getSharedPreferences().getString(KEY_ACCESS_TOKEN, null);
    }

    public void putAccessToken(String accessToken) {
        getEditor(Context.MODE_PRIVATE).putString(KEY_ACCESS_TOKEN, accessToken).commit();
    }

    public String getRefreshToken() {
        return getSharedPreferences().getString(KEY_ACCESS_TOKEN, null);
    }

    public void putRefreshToken(String refreshToken) {
        getEditor(Context.MODE_PRIVATE).putString(KEY_REFRESH_TOKEN, refreshToken).commit();
    }

    private SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getEditor(int mode) {
        return getSharedPreferences().edit();
    }
}
