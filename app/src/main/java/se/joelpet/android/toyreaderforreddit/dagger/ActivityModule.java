package se.joelpet.android.toyreaderforreddit.dagger;

import android.app.Activity;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import se.joelpet.android.toyreaderforreddit.ui.linklisting.LinkListingActivity;
import se.joelpet.android.toyreaderforreddit.ui.linklisting.LinkListingFragment;
import se.joelpet.android.toyreaderforreddit.ui.linklisting.LinkListingRecyclerViewAdapter;
import se.joelpet.android.toyreaderforreddit.ui.login.AppConnectWebViewClient;
import se.joelpet.android.toyreaderforreddit.ui.login.LoginActivity;
import se.joelpet.android.toyreaderforreddit.ui.main.MainActivity;
import se.joelpet.android.toyreaderforreddit.ui.web.WebActivity;
import se.joelpet.android.toyreaderforreddit.ui.web.WebFragment;

@Module(
        injects = {
                LinkListingActivity.class,
                LinkListingFragment.class,
                LinkListingRecyclerViewAdapter.class,
                LoginActivity.class,
                MainActivity.class,
                WebActivity.class,
                WebFragment.class,
        },
        addsTo = ApplicationModule.class
)
public class ActivityModule {

    private final Activity mActivity;
    private final AppCompatCallback mAppCompatCallback;

    public ActivityModule(Activity activity) {
        this(activity, null);
    }

    public ActivityModule(Activity activity, AppCompatCallback appCompatCallback) {
        mActivity = activity;
        mAppCompatCallback = appCompatCallback;
    }

    @Provides
    @Singleton
    Activity provideActivity() {
        return mActivity;
    }

    @Provides
    @Singleton
    AppCompatCallback provideAppCompatCallback() {
        return mAppCompatCallback;
    }

    @Provides
    AppCompatDelegate provideAppCompatDelegate(Activity activity, AppCompatCallback callback) {
        return AppCompatDelegate.create(activity, callback);
    }

    @Provides
    AppConnectWebViewClient provideAppConnectWebViewClient() {
        return new AppConnectWebViewClient();
    }
}
