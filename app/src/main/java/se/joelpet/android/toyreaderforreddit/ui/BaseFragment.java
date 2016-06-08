package se.joelpet.android.toyreaderforreddit.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public abstract class BaseFragment extends Fragment {

    /** Composite subscription to keep track of all subscription registrations in this Fragment. */
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("%s###onCreate(%s)", this, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((BaseActivity) this.getActivity()).inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.v("%s###onStart()", this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.v("%s###onStop()", this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.v("%s###onDestroy()", this);
    }

    protected void addSubscription(Subscription subscription) {
        mCompositeSubscription.add(subscription);
    }

    protected void unsubscribeFromAll() {
        mCompositeSubscription.unsubscribe();
    }
}
