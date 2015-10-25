package se.joelpet.android.toyreaderforreddit.rx.transformers;

import rx.Observable;

public class CacheAndSubscribeTransformer<T> implements Observable.Transformer<T, T> {

    private static CacheAndSubscribeTransformer<?> instance;

    public synchronized static <T> CacheAndSubscribeTransformer<T> getInstance() {
        if (instance == null) {
            instance = new CacheAndSubscribeTransformer<T>();
        }
        return (CacheAndSubscribeTransformer<T>) instance;
    }

    @Override
    public Observable<T> call(Observable<T> observable) {
        Observable<T> cached = observable.cache(1);
        cached.subscribe();
        return cached;
    }
}
