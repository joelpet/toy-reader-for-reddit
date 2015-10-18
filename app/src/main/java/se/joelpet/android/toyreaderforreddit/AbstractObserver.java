package se.joelpet.android.toyreaderforreddit;

import rx.Observer;
import timber.log.Timber;

public abstract class AbstractObserver<T> implements Observer<T> {
    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
        Timber.e(e, "Observable encountered an error");
    }

    @Override
    public void onNext(T t) {
    }
}
