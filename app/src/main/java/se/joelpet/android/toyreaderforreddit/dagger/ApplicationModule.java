package se.joelpet.android.toyreaderforreddit.dagger;

import com.android.volley.toolbox.ImageLoader;

import android.accounts.AccountManager;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import se.joelpet.android.toyreaderforreddit.Preferences;
import se.joelpet.android.toyreaderforreddit.RedditApplication;
import se.joelpet.android.toyreaderforreddit.VolleySingleton;
import se.joelpet.android.toyreaderforreddit.accounts.AccountAuthenticator;
import se.joelpet.android.toyreaderforreddit.accounts.AccountManagerHelper;
import se.joelpet.android.toyreaderforreddit.net.FakeRedditApi;
import se.joelpet.android.toyreaderforreddit.net.RealRedditApi;
import se.joelpet.android.toyreaderforreddit.net.RedditApi;
import se.joelpet.android.toyreaderforreddit.services.LoginAccountsUpdatedIntentService;
import se.joelpet.android.toyreaderforreddit.services.RedditOAuthAuthenticatorService;
import se.joelpet.android.toyreaderforreddit.storage.DefaultLocalDataStore;
import se.joelpet.android.toyreaderforreddit.storage.LocalDataStore;

@Module(
        injects = {
                DefaultLocalDataStore.class,
                FakeRedditApi.class,
                RealRedditApi.class,
                LoginAccountsUpdatedIntentService.class,
                RedditOAuthAuthenticatorService.class,
        },
        library = true
)
public class ApplicationModule {

    private final RedditApplication mRedditApplication;

    public ApplicationModule(RedditApplication redditApplication) {
        mRedditApplication = redditApplication;
    }

    /**
     * Allow the application context to be injected but require that it be annotated with
     * {@link ForApplication @Annotation} to explicitly differentiate it from an activity context.
     */
    @Provides
    @Singleton
    @ForApplication
    Context provideApplicationContext() {
        return mRedditApplication;
    }

    @Provides
    @Singleton
    VolleySingleton provideVolleySingleton() {
        return VolleySingleton.getInstance(mRedditApplication);
    }

    @Provides
    @Singleton
    ImageLoader provideImageLoader(VolleySingleton volleySingleton) {
        return volleySingleton.getImageLoader();
    }

    @Provides
    @Singleton
    AccountManager provideAccountManager(@ForApplication Context context) {
        return AccountManager.get(context);
    }

    @Provides
    @Singleton
    AccountManagerHelper provideAccountManagerHelper(@ForApplication Context context, AccountManager
            accountManager) {
        return new AccountManagerHelper(context, accountManager);
    }

    @Provides
    AccountAuthenticator provideAccountAuthenticator(@ForApplication Context context,
                                                     AccountManagerHelper accountManagerHelper,
                                                     RedditApi redditApi) {
        return new AccountAuthenticator(context, accountManagerHelper, redditApi);
    }

    @Provides
    @Singleton
    RedditApi provideRedditApi(VolleySingleton volleySingleton, LocalDataStore localDataStore,
                               AccountManagerHelper accountManagerHelper) {
        // TODO: Check BuildConfig for test build.
        return true ? new RealRedditApi(volleySingleton, accountManagerHelper) :
                new FakeRedditApi();
    }

    @Provides
    @Singleton
    LocalDataStore provideLocalStorage(Preferences preferences) {
        return new DefaultLocalDataStore(preferences);
    }

    @Provides
    @Singleton
    Preferences providePreferences(@ForApplication Context context) {
        return new Preferences(context);
    }
}
