package se.joelpet.android.toyreaderforreddit.storage;

import rx.Observable;
import se.joelpet.android.toyreaderforreddit.domain.Me;

public interface LocalDataStore {

    /** Returns a hot observable source of Me values. */
    Observable<Me> observeMe();

    /** Stores the given Me object. */
    Observable<Me> putMe(Me me);

    /** Deletes the current Me object, if exists, before completion. */
    Observable<Void> deleteMe();

}
