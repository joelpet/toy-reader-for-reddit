package se.joelpet.android.toyreaderforreddit.rx.transformers;

import rx.Observable;
import rx.schedulers.Schedulers;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class WorkOnIoAndOnNotifyOnMainTransformer<T> implements Observable.Transformer<T, T> {

    private static WorkOnIoAndOnNotifyOnMainTransformer<?> instance;

    public synchronized static <T> WorkOnIoAndOnNotifyOnMainTransformer<T> getInstance() {
        if (instance == null) {
            instance = new WorkOnIoAndOnNotifyOnMainTransformer<>();
        }
        return (WorkOnIoAndOnNotifyOnMainTransformer<T>) instance;
    }

    @Override
    public Observable<T> call(Observable<T> observable) {
        return observable.subscribeOn(Schedulers.io()).observeOn(mainThread());
    }
}
