package se.joelpet.android.toyreaderforreddit.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import rx.Observable;
import rx.Subscriber;
import se.joelpet.android.toyreaderforreddit.rx.transformers.WorkOnIoAndOnNotifyOnMainTransformer;

public class BitmapUtils {

    @NonNull
    public static Observable<Bitmap> decodeBitmapResource(final Resources res,
                                                          @DrawableRes final int id) {
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Bitmap bitmap = BitmapFactory.decodeResource(res, id);
                if (bitmap != null) subscriber.onNext(bitmap);
                subscriber.onCompleted();
            }
        }).compose(WorkOnIoAndOnNotifyOnMainTransformer.<Bitmap>getInstance());
    }
}
