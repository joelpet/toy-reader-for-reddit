package se.joelpet.android.toyredditreader.activities;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;

import se.joelpet.android.toyredditreader.RedditApp;

public class BaseActivity extends ActionBarActivity {

    /**
     * Injects any dependencies into the given activity.
     */
    protected static void inject(Activity activity) {
        ((RedditApp) activity.getApplication()).inject(activity);
    }

}