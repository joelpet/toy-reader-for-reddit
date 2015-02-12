package se.joelpet.android.toyredditreader.volley;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import timber.log.Timber;

public class ResponseListener<T> implements Response.Listener<T>, Response.ErrorListener {

    @Override
    public void onResponse(T response) {
        Timber.i("onResponse(%s)", response);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        Timber.e(volleyError, "Request failed");
    }
}
