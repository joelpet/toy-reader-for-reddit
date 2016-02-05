package se.joelpet.android.toyreaderforreddit.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;
import rx.Observable;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.subscriptions.CompositeSubscription;
import se.joelpet.android.toyreaderforreddit.RedditApplication;
import se.joelpet.android.toyreaderforreddit.dagger.ActivityModule;

public abstract class BaseActivity extends AppCompatActivity {

    private CompositeSubscription mCompositeSubscription;

    private ObjectGraph mActivityGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityGraph = ((RedditApplication) getApplication()).getApplicationGraph()
                .plus(getModules().toArray());
        mActivityGraph.inject(this);

        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    protected void onDestroy() {
        mCompositeSubscription.unsubscribe();
        mCompositeSubscription = null;
        mActivityGraph = null;

        super.onDestroy();
    }

    protected List<Object> getModules() {
        return Arrays.<Object>asList(new ActivityModule(this));
    }

    public <T> void inject(T object) {
        mActivityGraph.inject(object);
    }

    protected <T> Observable<T> bindToActivity(Observable<T> source) {
        return AndroidObservable.bindActivity(this, source);
    }

    protected void addSubscription(Subscription subscription) {
        mCompositeSubscription.add(subscription);
    }
}
