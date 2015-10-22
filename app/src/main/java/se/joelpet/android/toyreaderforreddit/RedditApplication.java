package se.joelpet.android.toyreaderforreddit;

import net.danlew.android.joda.JodaTimeAndroid;

import android.app.Application;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;
import se.joelpet.android.toyreaderforreddit.dagger.ApplicationModule;
import timber.log.Timber;

public class RedditApplication extends Application {

    private ObjectGraph mApplicationGraph;

    @Override
    public void onCreate() {
        super.onCreate();

        mApplicationGraph = ObjectGraph.create(getModules().toArray());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        JodaTimeAndroid.init(this);
    }

    protected List<Object> getModules() {
        return Arrays.<Object>asList(new ApplicationModule(this));
    }

    public ObjectGraph getApplicationGraph() {
        return mApplicationGraph;
    }

}
