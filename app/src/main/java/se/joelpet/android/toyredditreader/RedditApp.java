package se.joelpet.android.toyredditreader;

import android.app.Application;

import timber.log.Timber;

public class RedditApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
