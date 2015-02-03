package se.joelpet.android.toyredditreader.dagger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import se.joelpet.android.toyredditreader.RedditApp;
import se.joelpet.android.toyredditreader.VolleySingleton;
import se.joelpet.android.toyredditreader.adapters.SubredditRecyclerViewAdapter;
import se.joelpet.android.toyredditreader.fragments.SubredditListingFragment;
import se.joelpet.android.toyredditreader.net.RealRedditApi;
import se.joelpet.android.toyredditreader.net.RedditApi;

@Module(
        injects = {
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
     *
     * @Provides
     * @Singleton
     * @ForApplication Context provideApplicationContext() {
     * return mRedditApp;
     * }
     */

    @Provides
    @Singleton
    VolleySingleton provideVolleySingleton() {
        return VolleySingleton.getInstance(mRedditApp);
    }

    @Provides
    RedditApi provideRedditApi(VolleySingleton volleySingleton) {
        return new RealRedditApi(volleySingleton);
    }

}
