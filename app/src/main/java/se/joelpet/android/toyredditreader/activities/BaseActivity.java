package se.joelpet.android.toyredditreader.activities;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import rx.Observable;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.subscriptions.CompositeSubscription;
import se.joelpet.android.toyredditreader.RedditApp;

public abstract class BaseActivity extends AppCompatActivity {

    /** Composite subscription to keep track of all subscription registrations in this Activity. */
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    /**
     * Injects any dependencies into the given activity.
     */
    protected static void inject(Activity activity) {
        ((RedditApp) activity.getApplication()).inject(activity);
    }

    protected <T> Observable<T> bindToActivity(Observable<T> source) {
        return AndroidObservable.bindActivity(this, source);
    }

    protected void addSubscription(Subscription subscription) {
        mCompositeSubscription.add(subscription);
    }

    protected void unsubscribeFromAll() {
        mCompositeSubscription.unsubscribe();
    }
}
