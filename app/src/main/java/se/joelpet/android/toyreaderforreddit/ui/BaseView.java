package se.joelpet.android.toyreaderforreddit.ui;

import android.support.annotation.NonNull;

public interface BaseView<T> {

    void setPresenter(@NonNull T presenter);

}
