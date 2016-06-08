package se.joelpet.android.toyreaderforreddit;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import se.joelpet.android.toyreaderforreddit.model.Me;

public class Preferences {

    private static final String PREFERENCES_NAME = "preferences";

    public static final String KEY_ME = "KEY_ME";

    private Context mContext;
    private Gson mGson;

    public Preferences(Context context) {
        mContext = context;
        mGson = new Gson();
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
