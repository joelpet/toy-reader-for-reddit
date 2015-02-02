package se.joelpet.android.toyredditreader.dagger;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import se.joelpet.android.toyredditreader.RedditApp;
import se.joelpet.android.toyredditreader.activities.SubredditActivity;
import se.joelpet.android.toyredditreader.adapters.SubredditRecyclerViewAdapter;
import se.joelpet.android.toyredditreader.fragments.SubredditListingFragment;
import se.joelpet.android.toyredditreader.net.RealRedditApi;
import se.joelpet.android.toyredditreader.net.RedditApi;

@Module(
        injects = {
                SubredditActivity.class,
                SubredditListingFragment.class,
                RealRedditApi.class,
                SubredditRecyclerViewAdapter.class
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

    /*
    @Provides
    @Singleton
    VolleySingleton provideVolleySingleton(Context context) {
        return VolleySingleton.getInstance(context);
    }
    */

    @Provides
    RedditApi provideRedditApi() {
        return new RealRedditApi();
    }

}
