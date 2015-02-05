package se.joelpet.android.toyredditreader;

import android.app.Application;

import dagger.ObjectGraph;
import se.joelpet.android.toyredditreader.dagger.RedditModule;
import timber.log.Timber;

public class RedditApp extends Application {

    private ObjectGraph mObjectGraph;

    @Override
    public void onCreate() {
        super.onCreate();

        mObjectGraph = ObjectGraph.create(new RedditModule(this));

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    public <T> void inject(T object) {
        mObjectGraph.inject(object);
    }
}
