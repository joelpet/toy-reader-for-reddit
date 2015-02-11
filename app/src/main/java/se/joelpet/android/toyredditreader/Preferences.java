package se.joelpet.android.toyredditreader;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static final String PREFERENCES_NAME = "preferences";

    private Context mContext;

    public Preferences(Context context) {
        mContext = context;
    }

    public String getToken() {
        return getSharedPreferences(Context.MODE_PRIVATE).getString("token", null);
    }

    public void putToken(String token) {
        getEditor(Context.MODE_PRIVATE).putString("token", token).commit();
    }

    private SharedPreferences getSharedPreferences(int mode) {
        return mContext.getSharedPreferences(PREFERENCES_NAME, mode);
    }

    private SharedPreferences.Editor getEditor(int mode) {
        return getSharedPreferences(mode).edit();
    }
}
