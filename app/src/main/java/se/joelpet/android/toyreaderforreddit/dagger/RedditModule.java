package se.joelpet.android.toyreaderforreddit.dagger;

import com.android.volley.toolbox.ImageLoader;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import se.joelpet.android.toyreaderforreddit.AppConnectWebViewClient;
import se.joelpet.android.toyreaderforreddit.Preferences;
import se.joelpet.android.toyreaderforreddit.RedditApp;
import se.joelpet.android.toyreaderforreddit.VolleySingleton;
import se.joelpet.android.toyreaderforreddit.activities.LoginActivity;
import se.joelpet.android.toyreaderforreddit.activities.MainActivity;
import se.joelpet.android.toyreaderforreddit.adapters.LinkListingRecyclerViewAdapter;
import se.joelpet.android.toyreaderforreddit.fragments.LinkListingFragment;
import se.joelpet.android.toyreaderforreddit.net.FakeRedditApi;
import se.joelpet.android.toyreaderforreddit.net.RealRedditApi;
import se.joelpet.android.toyreaderforreddit.net.RedditApi;
import se.joelpet.android.toyreaderforreddit.storage.DefaultLocalDataStore;
import se.joelpet.android.toyreaderforreddit.storage.LocalDataStore;

@Module(
        injects = {
                DefaultLocalDataStore.class,
                FakeRedditApi.class,
                LinkListingFragment.class,
                LinkListingRecyclerViewAdapter.class,
                LoginActivity.class,
                MainActivity.class,
                RealRedditApi.class,
        }
)
public class RedditModule {

    private final RedditApp mRedditApp;

    public RedditModule(RedditApp redditApp) {
        mRedditApp = redditApp;
    }

    /**
     * Allow the application context to be injected but require that it be annotated with
     * {@link ForApplication @Annotation} to explicitly differentiate it from an activity context.
     */
    @Provides
    @Singleton
    @ForApplication
    Context provideApplicationContext() {
        return mRedditApp;
    }

    @Provides
    @Singleton
    VolleySingleton provideVolleySingleton() {
        return VolleySingleton.getInstance(mRedditApp);
    }

    @Provides
    @Singleton
    ImageLoader provideImageLoader(VolleySingleton volleySingleton) {
        return volleySingleton.getImageLoader();
    }

    @Provides
    @Singleton
    RedditApi provideRedditApi(VolleySingleton volleySingleton, LocalDataStore localDataStore) {
        // TODO: Check BuildConfig for test build.
        return true ? new RealRedditApi(volleySingleton, localDataStore) : new FakeRedditApi();
    }

    @Provides
    @Singleton
    LocalDataStore provideLocalStorage(Preferences preferences) {
        return new DefaultLocalDataStore(preferences);
    }

    @Provides
    AppConnectWebViewClient provideAppConnectWebViewClient() {
        return new AppConnectWebViewClient();
    }

    @Provides
    @Singleton
    Preferences providePreferences(@ForApplication Context context) {
        return new Preferences(context);
    }

}
